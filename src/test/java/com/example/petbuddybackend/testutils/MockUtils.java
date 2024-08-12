package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.animal.AnimalType;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;

import java.util.Arrays;
import java.util.List;

import static com.example.petbuddybackend.entity.animal.AnimalType.*;

public final class MockUtils {
    
    private MockUtils() {
    }


    public static Address createMockAddress(Voivodeship voivodeship, String city) {
        return Address.builder()
                .city(city)
                .voivodeship(voivodeship)
                .street("street")
                .zipCode("12-123")
                .buildingNumber("5A")
                .apartmentNumber("10")
                .build();
    }

    public static Address createMockAddress() {
        return createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa");
    }

    public static Caretaker createMockCaretaker(String name, String surname, String email, List<Offer> animals, Address address) {
        AppUser accountData = AppUser.builder()
                .email(email)
                .name(name)
                .surname(surname)
                .build();

        Caretaker caretaker = Caretaker.builder()
                .email(email)
                .accountData(accountData)
                .address(address)
                .description("description")
                .phoneNumber("number")
                .avgRating(4.5f)
                .build();

        animals = animals.stream()
                .map(animal -> {
                    animal.setCaretaker(caretaker);
                    return animal;
                })
                .toList();

        caretaker.setAnimalsTakenCareOf(animals);
        return caretaker;
    }

    public static Caretaker createMockCaretaker() {
        return createMockCaretaker(
                "name",
                "surname",
                "email",
                animalsOfTypes(CAT, DOG),
                createMockAddress()
        );
    }

    public static List<Caretaker> createMockCaretakers() {
        return List.of(
                MockUtils.createMockCaretaker("John", "Doe", "testmail@mail.com", animalsOfTypes(DOG, CAT, BIRD),
                        createMockAddress(Voivodeship.SLASKIE, "Katowice")),
                MockUtils.createMockCaretaker("Jane", "Doe", "another@mail.com", animalsOfTypes(DOG),
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa")),
                MockUtils.createMockCaretaker("John", "Smith", "onceagain@mail.com", animalsOfTypes(REPTILE),
                        createMockAddress(Voivodeship.MAZOWIECKIE, "Warszawa"))
        );
    }

    public static Offer animalOfType(AnimalType animalType) {
        return Offer.builder()
                .animalType(animalType)
                .build();
    }

    public static List<Offer> animalsOfTypes(AnimalType... animalTypes) {
        return Arrays.stream(animalTypes)
                .map(MockUtils::animalOfType)
                .toList();
    }

    public static Client createMockClient() {
        String email = "clientEmail";

        return Client.builder()
                .email(email)
                .accountData(AppUser.builder()
                        .email("clientEmail")
                        .name("clientName")
                        .surname("clientSurname")
                        .build())
                .build();
    }

    public static Rating createMockRating(Caretaker caretaker, Client client) {
        return Rating.builder()
                .caretakerEmail(caretaker.getEmail())
                .clientEmail(client.getEmail())
                .caretaker(caretaker)
                .client(client)
                .rating(5)
                .comment("comment")
                .build();
    }
}
