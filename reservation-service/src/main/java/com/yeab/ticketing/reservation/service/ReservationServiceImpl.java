package com.yeab.ticketing.reservation.service;

import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.reservation.client.EventClient;
import com.yeab.ticketing.reservation.client.EventDetails;
import com.yeab.ticketing.reservation.client.SeatClient;
import com.yeab.ticketing.reservation.client.SeatDetails;
import com.yeab.ticketing.reservation.config.ReservationProperties;
import com.yeab.ticketing.reservation.dto.HoldReservationRequest;
import com.yeab.ticketing.reservation.dto.ReservationResponse;
import com.yeab.ticketing.reservation.entity.ReservationEntity;
import com.yeab.ticketing.reservation.entity.ReservationSeatEntity;
import com.yeab.ticketing.reservation.enums.ReservationStatus;
import com.yeab.ticketing.reservation.exception.EventNotBookableException;
import com.yeab.ticketing.reservation.exception.InvalidReservationStateException;
import com.yeab.ticketing.reservation.exception.ReservationExpiredException;
import com.yeab.ticketing.reservation.exception.ReservationNotFoundException;
import com.yeab.ticketing.reservation.exception.SeatAlreadyReservedException;
import com.yeab.ticketing.reservation.mapper.ReservationMapper;
import com.yeab.ticketing.reservation.messaging.ReservationEventOutbox;
import com.yeab.ticketing.reservation.repository.ReservationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventClient eventClient;
    private final SeatClient seatClient;
    private final PricingService pricingService;
    private final DiscountService discountService;
    private final ReservationEventOutbox eventOutbox;
    private final ReservationProperties properties;
    private final Clock clock;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  EventClient eventClient,
                                  SeatClient seatClient,
                                  PricingService pricingService,
                                  DiscountService discountService,
                                  ReservationEventOutbox eventOutbox,
                                  ReservationProperties properties,
                                  Clock clock) {
        this.reservationRepository = reservationRepository;
        this.eventClient = eventClient;
        this.seatClient = seatClient;
        this.pricingService = pricingService;
        this.discountService = discountService;
        this.eventOutbox = eventOutbox;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ReservationResponse hold(HoldReservationRequest request, String idempotencyKey) {
        String holdKey = normalize(idempotencyKey);
        if (holdKey != null) {
            ReservationEntity existing = reservationRepository.findByHoldKey(holdKey).orElse(null);
            if (existing != null) {
                return ReservationMapper.toResponse(existing);
            }
        }

        EventDetails event = eventClient.getEvent(request.eventId());
        if (!"PUBLISHED".equals(event.status())) {
            throw new EventNotBookableException("Event " + request.eventId() + " is not bookable (status=" + event.status() + ")");
        }

        List<UUID> distinctSeatIds = request.seatIds().stream().distinct().toList();
        if (distinctSeatIds.size() != request.seatIds().size()) {
            throw new EventNotBookableException("Duplicate seats in request");
        }

        Map<UUID, SeatDetails> seatsById = SeatDetails.indexBySeatId(
                seatClient.getSeats(distinctSeatIds));
        if (seatsById.size() != distinctSeatIds.size()) {
            throw new EventNotBookableException("One or more selected seats do not exist");
        }

        Instant now = clock.instant();
        BigDecimal subtotal = BigDecimal.ZERO;
        List<ReservationSeatEntity> seatEntities = new ArrayList<>();
        for (UUID seatId : distinctSeatIds) {
            SeatDetails seat = seatsById.get(seatId);
            BigDecimal price = pricingService.priceFor(seat.section(), event.pricingRules());
            subtotal = subtotal.add(price);
            seatEntities.add(new ReservationSeatEntity(request.eventId(), seatId, price));
        }

        Duration holdDuration = properties.getHoldDuration();
        ReservationEntity reservation = new ReservationEntity(
                request.eventId(), currentCustomerId(), currentCustomerEmail(),
                subtotal, request.currency(), now.plus(holdDuration));
        if (holdKey != null) {
            reservation.setHoldKey(holdKey);
        }
        reservation.applyDiscount(request.discountCode(), discountService.apply(request.discountCode(), subtotal));
        seatEntities.forEach(seat -> {
            seat.setReservation(reservation);
            reservation.getSeats().add(seat);
        });

        try {
            reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException ex) {
            throw new SeatAlreadyReservedException(
                    "One or more selected seats were just reserved by another customer");
        }

        eventOutbox.recordReservationCreated(
                reservation.getId(), request.eventId(), reservation.getCustomerId(), reservation.getCustomerEmail(),
                reservation.getCurrency(), reservation.getTotalAmount(), reservation.getSeats().size(),
                reservation.getHoldExpiresAt());

        return ReservationMapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getById(UUID reservationId) {
        return ReservationMapper.toResponse(load(reservationId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservationResponse> list(String customerId, Pageable pageable) {
        String resolvedCustomerId = currentCustomerId();
        String effective = (customerId != null && !customerId.isBlank()) ? customerId : resolvedCustomerId;
        return reservationRepository.findByCustomerIdOrderByCreatedAtDesc(effective, pageable)
                .map(ReservationMapper::toResponse);
    }

    @Override
    @Transactional
    public ReservationResponse confirm(UUID reservationId) {
        ReservationEntity reservation = load(reservationId);
        int updated = reservationRepository.transitionToConfirmed(reservationId, clock.instant());
        if (updated != 1) {
            ReservationEntity fresh = reservationRepository.findById(reservationId).orElseThrow();
            if (fresh.getStatus() == ReservationStatus.EXPIRED
                    || (fresh.getStatus() == ReservationStatus.HOLD
                        && fresh.getHoldExpiresAt() != null
                        && fresh.getHoldExpiresAt().isBefore(clock.instant()))) {
                throw new ReservationExpiredException(
                        "Reservation " + reservationId + " has expired and cannot be confirmed");
            }
            throw new InvalidReservationStateException(
                    "Reservation " + reservationId + " is in state " + fresh.getStatus()
                            + " and cannot be confirmed");
        }
        ReservationEntity confirmed = reservationRepository.findById(reservationId).orElseThrow();
        eventOutbox.recordReservationStateChanged(
                EventTypes.RESERVATION_CONFIRMED, confirmed.getId(), confirmed.getEventId(),
                confirmed.getCustomerId(), confirmed.getCustomerEmail(), confirmed.getCurrency(),
                confirmed.getTotalAmount(), confirmed.getSeats().size());
        return ReservationMapper.toResponse(confirmed);
    }

    @Override
    @Transactional
    public ReservationResponse cancel(UUID reservationId) {
        load(reservationId);
        int updated = reservationRepository.transitionToCancelled(reservationId);
        if (updated != 1) {
            throw new InvalidReservationStateException(
                    "Reservation " + reservationId + " cannot be cancelled in its current state");
        }
        ReservationEntity cancelled = reservationRepository.findById(reservationId).orElseThrow();
        eventOutbox.recordReservationStateChanged(
                EventTypes.RESERVATION_CANCELLED, cancelled.getId(), cancelled.getEventId(),
                cancelled.getCustomerId(), cancelled.getCustomerEmail(), cancelled.getCurrency(),
                cancelled.getTotalAmount(), cancelled.getSeats().size());
        return ReservationMapper.toResponse(cancelled);
    }

    private ReservationEntity load(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation " + reservationId + " not found"));
    }

    private String normalize(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
    }

    private String currentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "guest";
        }
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object preferred = jwt.getTokenAttributes().get("preferred_username");
            return preferred != null ? preferred.toString() : auth.getName();
        }
        return auth.getName();
    }

    private String currentCustomerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object email = jwt.getTokenAttributes().get("email");
            if (email != null) {
                return email.toString();
            }
        }
        return currentCustomerId() + "@ticketing.local";
    }
}