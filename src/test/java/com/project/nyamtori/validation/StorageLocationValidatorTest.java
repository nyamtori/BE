package com.project.nyamtori.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StorageLocationValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    static class Sample {
        @ValidStorageLocation
        String location;

        Sample(String location) {
            this.location = location;
        }
    }

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void allowsThreeValidLocations() {
        assertThat(validator.validate(new Sample("냉장"))).isEmpty();
        assertThat(validator.validate(new Sample("냉동"))).isEmpty();
        assertThat(validator.validate(new Sample("실온"))).isEmpty();
    }

    @Test
    void rejectsInvalidLocation() {
        Set<ConstraintViolation<Sample>> violations = validator.validate(new Sample("냉장고"));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void allowsNull_soOptionalUpdateFieldIsUnaffected() {
        assertThat(validator.validate(new Sample(null))).isEmpty();
    }
}
