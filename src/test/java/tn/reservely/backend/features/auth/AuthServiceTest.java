package tn.reservely.backend.features.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.reservely.backend.domain.user.AuthProvider;
import tn.reservely.backend.domain.user.User;
import tn.reservely.backend.domain.user.UserRepository;
import tn.reservely.backend.domain.user.UserRole;
import tn.reservely.backend.features.auth.dto.AuthResponse;
import tn.reservely.backend.features.auth.dto.LoginRequest;
import tn.reservely.backend.features.auth.dto.RegisterRequest;
import tn.reservely.backend.features.auth.service.AuthService;
import tn.reservely.backend.security.jwt.JwtTokenProvider;
import tn.reservely.backend.shared.exception.ConflictException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;

    @InjectMocks AuthService authService;

    private final UUID userId = UUID.randomUUID();
    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("$hashed")
                .name("Test User")
                .provider(AuthProvider.LOCAL)
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest req = new RegisterRequest(
                "new@example.com", "password123", "New User", null, null);

        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("$hashed");
        when(userRepository.save(any())).thenReturn(existingUser);
        when(tokenProvider.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowConflict_whenEmailExists() {
        RegisterRequest req = new RegisterRequest(
                "test@example.com", "password123", "Test", null, null);

        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        LoginRequest req = new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(req.password(), existingUser.getPasswordHash())).thenReturn(true);
        when(tokenProvider.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@x.com", "pass")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldThrow_whenPasswordWrong() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_shouldThrow_whenAccountIsGoogleOnly() {
        User googleUser = User.builder()
                .id(UUID.randomUUID())
                .email("google@example.com")
                .passwordHash(null)
                .name("Google User")
                .provider(AuthProvider.GOOGLE)
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(googleUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("google@example.com", "any")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Google sign-in");
    }
}
