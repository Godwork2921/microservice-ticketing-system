package com.yeab.ticketing.user.service;

import com.yeab.ticketing.user.dto.request.RegisterUserRequest;
import com.yeab.ticketing.user.dto.request.UpdateUserRequest;
import com.yeab.ticketing.user.dto.response.UserResponse;
import com.yeab.ticketing.user.entity.UserEntity;
import com.yeab.ticketing.user.exception.EmailAlreadyRegisteredException;
import com.yeab.ticketing.user.exception.UserNotFoundException;
import com.yeab.ticketing.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserResponse create(RegisterUserRequest request) {
        String email = normalize(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        UserEntity user = new UserEntity(request.id(), email, request.firstName().trim(),
                blankToNull(request.lastName()), blankToNull(request.phone()), blankToNull(request.locale()));
        try {
            return toResponse(repository.save(user));
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyRegisteredException(email);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        UserEntity user = repository.findByEmailIgnoreCase(normalize(email))
                .orElseThrow(() -> new UserNotFoundException(normalize(email)));
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        UserEntity user = find(id);
        if (request.email() != null && !request.email().isBlank()) {
            String email = normalize(request.email());
            repository.findByEmailIgnoreCase(email)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new EmailAlreadyRegisteredException(email);
                    });
            user.setEmail(email);
        }
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(blankToNull(request.lastName()));
        }
        if (request.phone() != null) {
            user.setPhone(blankToNull(request.phone()));
        }
        if (request.locale() != null) {
            user.setLocale(blankToNull(request.locale()));
        }
        return toResponse(repository.save(user));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserEntity user = find(id);
        repository.delete(user);
    }

    private UserEntity find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getLocale(), user.getCreatedAt(), user.getUpdatedAt());
    }
}