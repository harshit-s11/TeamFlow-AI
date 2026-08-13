package com.teamflow.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
    }

    @Test
    void encode_returnsHashedPasswordNotEqualToRawPassword() {
        String rawPassword = "SecretPassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$12$");
    }

    @Test
    void matches_returnsTrueForMatchingRawAndEncodedPassword() {
        String rawPassword = "SecretPassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        assertThat(matches).isTrue();
    }

    @Test
    void matches_returnsFalseForDifferentPassword() {
        String rawPassword = "SecretPassword123!";
        String wrongPassword = "WrongPassword123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        assertThat(matches).isFalse();
    }
}
