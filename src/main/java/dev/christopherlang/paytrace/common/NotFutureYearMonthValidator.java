package dev.christopherlang.paytrace.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;

public class NotFutureYearMonthValidator implements ConstraintValidator<NotFutureYearMonth, YearMonth> {

    @Override
    public void initialize(NotFutureYearMonth constraintAnnotation) {}

    @Override
    public boolean isValid(YearMonth value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !value.isAfter(YearMonth.now());
    }

}
