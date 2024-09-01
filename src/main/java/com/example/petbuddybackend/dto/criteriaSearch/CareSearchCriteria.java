package com.example.petbuddybackend.dto.criteriaSearch;

import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.utils.annotations.validation.DateRange;
import com.example.petbuddybackend.utils.time.TimeUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

@DateRange(startDateField = "minCreatedTime", endDateField = "maxCreatedTime", message = "Max created time must be after min created time")
public record CareSearchCriteria(
        @Schema(
                description = "Filters by animal types in cares",
                examples = {"DOG", "CAT", "BIRD", "SMALL_PET", "FISH", "REPTILE", "HORSE"},
                type = "array"
        )
        Set<String> animalTypes,

        @Schema(
                description = "Filters by caretaker statuses",
                examples = {"PENDING", "ACCEPTED", "AWAITING_PAYMENT", "PAID", "CANCELLED", "OUTDATED"},
                type = "array"
        )
        Set<CareStatus> caretakerStatuses,

        @Schema(
                description = "Filters by client statuses",
                examples = {"PENDING", "ACCEPTED", "AWAITING_PAYMENT", "PAID", "CANCELLED", "OUTDATED"},
                type = "array"
        )
        Set<CareStatus> clientStatuses,

        @Schema(
                description = "Filters by min created time",
                example = "2020-01-03 12:00:05.123 +0100",
                type = "string"
        )
        ZonedDateTime minCreatedTime,

        @Schema(
                description = "Filters by max created time",
                example = "2020-01-07 12:05:05.123 +0100",
                type = "string"
        )
        ZonedDateTime maxCreatedTime,

        @Schema(
                description = "Filters by min daily price",
                example = "10.00",
                type = "number"
        )
        BigDecimal minDailyPrice,

        @Schema(
                description = "Filters by max daily price",
                example = "30.00",
                type = "number"
        )
        BigDecimal maxDailyPrice
) {
}
