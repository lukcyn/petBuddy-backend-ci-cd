package com.example.petbuddybackend.dto.offer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
public record OfferConfigurationFilterDTO(
        Map<String, List<String>> attributes,

        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 5, fraction = 2)
        BigDecimal minPrice,

        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 5, fraction = 2)
        BigDecimal maxPrice
) {
    public OfferConfigurationFilterDTO {
        minPrice = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        maxPrice = (maxPrice != null) ? maxPrice : BigDecimal.valueOf(Double.MAX_VALUE);
    }
}
