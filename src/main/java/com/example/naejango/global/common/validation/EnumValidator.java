package com.example.naejango.global.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<EnumConstraint, Enum<?>> {

    private String defaultValue;

    @Override
    public void initialize(EnumConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        defaultValue = constraintAnnotation.defaultValue();
    }

    @Override
    public boolean isValid(Enum enumValue, ConstraintValidatorContext context) {
        if (enumValue != null) return true;
        return !defaultValue.isEmpty();
    }
}

