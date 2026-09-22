package com.yeab.ticketing.venue.service;

import com.yeab.ticketing.venue.dto.request.CreateVenueRequest;
import com.yeab.ticketing.venue.dto.request.SeatRequest;
import com.yeab.ticketing.venue.entity.VenueEntity;
import com.yeab.ticketing.venue.exception.InvalidSeatAttributesException;
import com.yeab.ticketing.venue.repository.SeatRepository;
import com.yeab.ticketing.venue.repository.VenueRepository;
import com.yeab.ticketing.venue.service.impl.SeatServiceImpl;
import com.yeab.ticketing.venue.service.impl.VenueServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;
    @Mock
    private SeatRepository seatRepository;

    @Test
    void createsVenueWithTrimmedValues() {
        when(venueRepository.save(any(VenueEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        VenueService service = new VenueServiceImpl(venueRepository, seatRepository);

        var response = service.create(new CreateVenueRequest("  Arena ", "  Main Street ", " Africa/Addis_Ababa ", 100));

        org.assertj.core.api.Assertions.assertThat(response.name()).isEqualTo("Arena");
        org.assertj.core.api.Assertions.assertThat(response.address()).isEqualTo("Main Street");
        org.assertj.core.api.Assertions.assertThat(response.timezone()).isEqualTo("Africa/Addis_Ababa");
    }

    @Test
    void rejectsInvalidSeatAttributesJson() {
        UUID venueId = UUID.randomUUID();
        VenueEntity venue = new VenueEntity("Arena", "Address", "UTC", 10);
        when(venueRepository.findById(venueId)).thenReturn(java.util.Optional.of(venue));
        when(seatRepository.countByVenueId(venueId)).thenReturn(0L);
        SeatService service = new SeatServiceImpl(venueRepository, seatRepository, new ObjectMapper());

        assertThatThrownBy(() -> service.importSeats(venueId,
                List.of(new SeatRequest("A", "1", 1, "not-json"))))
                .isInstanceOf(InvalidSeatAttributesException.class);
    }
}
