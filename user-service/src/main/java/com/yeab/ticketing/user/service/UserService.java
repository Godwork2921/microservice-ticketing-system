package com.yeab.ticketing.user.service;

import com.yeab.ticketing.user.dto.request.RegisterUserRequest;
import com.yeab.ticketing.user.dto.request.UpdateUserRequest;
import com.yeab.ticketing.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse create(RegisterUserRequest request);

    UserResponse getById(UUID id);

    UserResponse getByEmail(String email);

    Page<UserResponse> list(Pageable pageable);

    UserResponse update(UUID id, UpdateUserRequest request);

    void delete(UUID id);
}