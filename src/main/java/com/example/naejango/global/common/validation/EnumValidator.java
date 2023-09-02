package com.example.naejango.global.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumConstraint, Enum<?>> {

    @Override
    public void initialize(EnumConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Enum enumValue, ConstraintValidatorContext context) {
        return enumValue != null;
    }
}

