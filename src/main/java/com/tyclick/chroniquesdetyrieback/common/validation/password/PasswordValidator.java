package com.tyclick.chroniquesdetyrieback.common.validation.password;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    private static final Pattern UPPERCASE_PATTERN
            = Pattern.compile("[A-Z]");

    private static final Pattern LOWERCASE_PATTERN
            = Pattern.compile("[a-z]");

    private static final Pattern DIGIT_PATTERN
            = Pattern.compile("[0-9]");

    private static final Pattern SPECIAL_CHARACTER_PATTERN
            = Pattern.compile("[^A-Za-z0-9]");

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null) {
            return true;
        }

        if (password.length() < MIN_LENGTH
                || password.length() > MAX_LENGTH) {
            return addViolation(
                    context,
                    "Password must be between 8 and 100 characters"
            );
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return addViolation(
                    context,
                    "Password must contain at least one uppercase letter"
            );
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return addViolation(
                    context,
                    "Password must contain at least one lowercase letter"
            );
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            return addViolation(
                    context,
                    "Password must contain at least one digit"
            );
        }

        if (!SPECIAL_CHARACTER_PATTERN.matcher(password).find()) {
            return addViolation(
                    context,
                    "Password must contain at least one special character"
            );
        }

        return true;
    }

    private boolean addViolation(
            ConstraintValidatorContext context,
            String message
    ) {
        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();

        return false;
    }
}
