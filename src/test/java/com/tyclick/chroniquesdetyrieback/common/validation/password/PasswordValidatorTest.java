package com.tyclick.chroniquesdetyrieback.common.validation.password;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;

class PasswordValidatorTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidPasswordWithMinimumLength() {
        Set<ConstraintViolation<PasswordHolder>> violations
                = validate("Aa1!aaaa");

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldAcceptValidPasswordWithMaximumLength() {
        String password = "Aa1!" + "a".repeat(96);

        Set<ConstraintViolation<PasswordHolder>> violations
                = validate(password);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldRejectPasswordShorterThanMinimumLength() {
        assertViolationMessage(
                "Aa1!aaa",
                "Password must be between 8 and 100 characters"
        );
    }

    @Test
    void shouldRejectPasswordLongerThanMaximumLength() {
        String password = "Aa1!" + "a".repeat(97);

        assertViolationMessage(
                password,
                "Password must be between 8 and 100 characters"
        );
    }

    @Test
    void shouldRejectPasswordWithoutUppercaseLetter() {
        assertViolationMessage(
                "password1!",
                "Password must contain at least one uppercase letter"
        );
    }

    @Test
    void shouldRejectPasswordWithoutLowercaseLetter() {
        assertViolationMessage(
                "PASSWORD1!",
                "Password must contain at least one lowercase letter"
        );
    }

    @Test
    void shouldRejectPasswordWithoutDigit() {
        assertViolationMessage(
                "Password!",
                "Password must contain at least one digit"
        );
    }

    @Test
    void shouldRejectPasswordWithoutSpecialCharacter() {
        assertViolationMessage(
                "Password1",
                "Password must contain at least one special character"
        );
    }

    @Test
    void shouldRejectBlankPassword() {
        assertViolationMessage(
                "",
                "Password is required"
        );
    }

    private Set<ConstraintViolation<PasswordHolder>> validate(
            String password
    ) {
        return validator.validate(new PasswordHolder(password));
    }

    private void assertViolationMessage(
            String password,
            String expectedMessage
    ) {
        Set<ConstraintViolation<PasswordHolder>> violations
                = validate(password);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(expectedMessage);
    }

    private static class PasswordHolder {

        @NotBlank(message = "Password is required")
        @ValidPassword
        private final String password;

        private PasswordHolder(String password) {
            this.password = password;
        }
    }
}
