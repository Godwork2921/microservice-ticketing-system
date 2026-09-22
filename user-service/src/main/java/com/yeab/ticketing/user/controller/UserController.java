package com.yeab.ticketing.user.controller;

import com.yeab.ticketing.user.dto.request.RegisterUserRequest;
import com.yeab.ticketing.user.dto.request.UpdateUserRequest;
import com.yeab.ticketing.user.dto.response.UserResponse;
import com.yeab.ticketing.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Users", description = "Customer profile registry (auth lives in Keycloak)")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Find user by id")
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @Operation(summary = "Find user by exact email")
    @GetMapping
    public Object findByEmailOrList(@RequestParam(required = false) String email,
                                    @PageableDefault(size = 20) Pageable pageable) {
        if (email != null && !email.isBlank()) {
            return service.getByEmail(email);
        }
        return service.list(pageable);
    }

    @Operation(summary = "Register a user profile")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody RegisterUserRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Update a user profile")
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a user profile")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}