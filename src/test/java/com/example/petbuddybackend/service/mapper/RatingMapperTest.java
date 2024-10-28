package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.rating.RatingResponse;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.testutils.ValidationUtils;
import com.example.petbuddybackend.testutils.mock.MockRatingProvider;
import org.junit.jupiter.api.Test;

import static com.example.petbuddybackend.testutils.mock.MockAnimalProvider.createMockAnimal;
import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockCaretaker;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.createMockClient;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RatingMapperTest {

    private final RatingMapper mapper = RatingMapper.INSTANCE;


    @Test
    void mapToCaretakerDTO_shouldNotLeaveNullFields() throws IllegalAccessException {
        Client client = createMockClient();
        Caretaker caretaker = createMockCaretaker();
        Care care = createMockCare(caretaker, client, createMockAnimal("DOG"));

        Rating rating = MockRatingProvider.createMockRating(caretaker, client, care);

        RatingResponse ratingResponse = mapper.mapToRatingResponse(rating);
        assertTrue(ValidationUtils.fieldsNotNullRecursive(ratingResponse));
    }
}
