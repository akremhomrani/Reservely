package tn.reservely.backend.features.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reservely.backend.domain.user.AuthProvider;
import tn.reservely.backend.domain.user.User;
import tn.reservely.backend.domain.user.UserRepository;
import tn.reservely.backend.domain.user.UserRole;
import tn.reservely.backend.features.auth.dto.AuthResponse;
import tn.reservely.backend.features.auth.dto.LoginRequest;
import tn.reservely.backend.features.auth.dto.RegisterRequest;
import tn.reservely.backend.security.jwt.JwtTokenProvider;
import tn.reservely.backend.shared.exception.ConflictException;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already in use");
        }

        UserRole role = UserRole.CUSTOMER;
        if ("BUSINESS_OWNER".equalsIgnoreCase(req.role())) {
            role = UserRole.BUSINESS_OWNER;
        }

        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .name(req.name())
                .phone(req.phone())
                .city(req.city())
                .role(role)
                .provider(AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.getPhone(), null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null) {
            throw new BadCredentialsException("This account uses Google sign-in. Please login with Google.");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.getPhone(), null);
    }

    @Transactional
    public AuthResponse loginWithGoogleIdToken(String idToken) {
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);

        String googleSub        = payload.getSubject();
        String email            = payload.getEmail();
        String rawName          = (String) payload.get("name");
        final String name       = (rawName == null || rawName.isBlank()) ? email.split("@")[0] : rawName;
        final String photoUrl   = (String) payload.get("picture");

        User user = userRepository
                .findByProviderAndProviderId(AuthProvider.GOOGLE, googleSub)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> {
                            existing.setProvider(AuthProvider.GOOGLE);
                            existing.setProviderId(googleSub);
                            return userRepository.save(existing);
                        })
                        .orElseGet(() -> userRepository.save(User.builder()
                                .email(email)
                                .name(name)
                                .provider(AuthProvider.GOOGLE)
                                .providerId(googleSub)
                                .build())));

        log.info("Google sign-in: {}", user.getEmail());
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name(), user.getPhone(), photoUrl);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) throw new BadCredentialsException("Invalid Google token");
            return token.getPayload();
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new BadCredentialsException("Google token verification failed");
        }
    }
}
