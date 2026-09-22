package com.yeab.ticketing.user.service;

import com.yeab.ticketing.user.dto.request.RegisterUserRequest;
import com.yeab.ticketing.user.dto.request.UpdateUserRequest;
import com.yeab.ticketing.user.entity.UserEntity;
import com.yeab.ticketing.user.exception.EmailAlreadyRegisteredException;
import com.yeab.ticketing.user.exception.UserNotFoundException;
import com.yeab.ticketing.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private UserRepository repository;
    private UserServiceImpl service;

    @BeforeEach void setUp() { service = new UserServiceImpl(repository); }

    @Test void createNormalizesEmailAndBuildsProfile() {
        when(repository.existsByEmailIgnoreCase("abebe@example.com")).thenReturn(false);
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new RegisterUserRequest(UUID.randomUUID(), "  Abebe@Example.com ",
                " Abebe ", " Bekele ", null, " en ");
        service.create(request);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("abebe@example.com");
        assertThat(captor.getValue().getFirstName()).isEqualTo("Abebe");
        assertThat(captor.getValue().getLastName()).isEqualTo("Bekele");
        assertThat(captor.getValue().getLocale()).isEqualTo("en");
    }

    @Test void createRejectsDuplicateEmail() {
        when(repository.existsByEmailIgnoreCase("a@b.com")).thenReturn(true);
        var request = new RegisterUserRequest(UUID.randomUUID(), "a@b.com", "A", null, null, null);
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test void updateChangesEmailOnlyIfNotTaken() {
        UUID id = UUID.randomUUID();
        UserEntity user = new UserEntity(id, "a@b.com", "A", null, null, null);
        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.findByEmailIgnoreCase("c@d.com")).thenReturn(Optional.empty());
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.update(id, new UpdateUserRequest("c@d.com", null, "New", null, null));
        assertThat(result.email()).isEqualTo("c@d.com");
        assertThat(result.lastName()).isEqualTo("New");
    }

    @Test void getByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(id)).isInstanceOf(UserNotFoundException.class);
    }
}