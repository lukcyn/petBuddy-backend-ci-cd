package com.example.petbuddybackend.service.offer;

import com.example.petbuddybackend.dto.availability.AvailabilityRangeDTO;
import com.example.petbuddybackend.dto.availability.CreateOffersAvailabilityDTO;
import com.example.petbuddybackend.dto.offer.OfferConfigurationDTO;
import com.example.petbuddybackend.dto.offer.OfferDTO;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.offer.OfferOption;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.service.animal.AnimalService;
import com.example.petbuddybackend.service.mapper.OfferConfigurationMapper;
import com.example.petbuddybackend.service.mapper.OfferMapper;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.exception.throweable.offer.AnimalAmenityDuplicatedInOfferException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.offer.OfferConfigurationDuplicatedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.common.util.CollectionUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final CaretakerService caretakerService;
    private final OfferRepository offerRepository;
    private final OfferConfigurationRepository offerConfigurationRepository;
    private final AnimalService animalService;
    private final OfferMapper offerMapper = OfferMapper.INSTANCE;
    private final OfferConfigurationMapper offerConfigurationMapper = OfferConfigurationMapper.INSTANCE;

    public OfferDTO addOrEditOffer(OfferDTO offer, String caretakerEmail) {
        Caretaker caretaker = caretakerService.getCaretakerByEmail(caretakerEmail);

        Offer modifiyngOffer = getOrCreateOffer(caretakerEmail, offer.animal().animalType(),
                caretaker, offer.description());

        if(StringUtils.hasText(offer.description())) {
            modifiyngOffer.setDescription(offer.description());
        }

        setOfferConfigurations(offer, modifiyngOffer);
        setOfferAnimalAmenities(offer, modifiyngOffer);

        return offerMapper.mapToOfferDTO(offerRepository.save(modifiyngOffer));

    }

    private void setOfferConfigurations(OfferDTO offer, Offer modifiyngOffer) {

        if(CollectionUtil.isNotEmpty(offer.offerConfigurations())) {
            List<OfferConfiguration> offerConfigurations = createConfigurationsForOffer(offer.offerConfigurations(), modifiyngOffer);
            if(CollectionUtil.isNotEmpty(modifiyngOffer.getOfferConfigurations())) {
                modifiyngOffer.getOfferConfigurations().addAll(offerConfigurations);
            } else {
                modifiyngOffer.setOfferConfigurations(offerConfigurations);
            }
        }

    }

    private void setOfferAnimalAmenities(OfferDTO offer, Offer modifiyngOffer) {

        if(CollectionUtil.isNotEmpty(offer.animalAmenities())) {
            Set<AnimalAmenity> animalAmenities = createAnimalAmenitiesForOffer(offer.animalAmenities(), modifiyngOffer);
            if(CollectionUtil.isNotEmpty(modifiyngOffer.getAnimalAmenities())) {
                modifiyngOffer.getAnimalAmenities().addAll(animalAmenities);
            } else {
                modifiyngOffer.setAnimalAmenities(animalAmenities);
            }

        }

    }

    private Set<AnimalAmenity> createAnimalAmenitiesForOffer(List<String> animalAmenities, Offer modifiyngOffer) {

        List<AnimalAmenity> newAnimalAmenities = new ArrayList<>();
        for(String animalAmenity : animalAmenities) {
            AnimalAmenity newAnimalAmenity = animalService.getAnimalAmenity(animalAmenity, modifiyngOffer.getAnimal().getAnimalType());
            checkDuplicateForAnimalAmenity(
                    Stream.concat(
                            Optional.ofNullable(modifiyngOffer.getAnimalAmenities()).orElse(Collections.emptySet()).stream(),
                            newAnimalAmenities.stream()
                    ).toList(), newAnimalAmenity
            );
            newAnimalAmenities.add(newAnimalAmenity);
        }

        return new HashSet<>(newAnimalAmenities);

    }

    private void checkDuplicateForAnimalAmenity(List<AnimalAmenity> oldAnimalAmenities, AnimalAmenity animalAmenity) {
        if(oldAnimalAmenities.stream().anyMatch(oldAnimalAmenity -> oldAnimalAmenity.equals(animalAmenity))) {
            throw new AnimalAmenityDuplicatedInOfferException(MessageFormat.format(
                    "Animal amenity with name {0} already exists in offer",
                    animalAmenity.getAmenity().getName()
            ));
        }
    }

    private List<OfferConfiguration> createConfigurationsForOffer(List<OfferConfigurationDTO> offerConfigurations,
                                                                  Offer offer) {
        List<OfferConfiguration> newOfferConfigurations = new ArrayList<>();
        for(OfferConfigurationDTO offerConfiguration : offerConfigurations) {
            OfferConfiguration configuration = createConfiguration(offerConfiguration, offer);
            checkForDuplicateConfiguration(Stream.concat(
                    Optional.ofNullable(offer.getOfferConfigurations()).orElse(Collections.emptyList()).stream(),
                    newOfferConfigurations.stream()
            ).toList(), configuration);

            newOfferConfigurations.add(configuration);
        }
        return newOfferConfigurations;

    }

    private void checkForDuplicateConfiguration(List<OfferConfiguration> oldConfigurations, OfferConfiguration configuration) {

        List<List<AnimalAttribute>> animalAttributesLists =
                oldConfigurations.stream()
                .map(OfferConfiguration::getOfferOptions)
                .map(offerOptions -> offerOptions.stream()
                        .map(OfferOption::getAnimalAttribute)
                        .sorted(Comparator.comparingLong(AnimalAttribute::getId))
                        .collect(Collectors.toList()))
                .toList();

        List<AnimalAttribute> newAnimalAttributes = configuration.getOfferOptions().stream()
                .map(OfferOption::getAnimalAttribute)
                .sorted(Comparator.comparingLong(AnimalAttribute::getId))
                .toList();

        if(animalAttributesLists.stream().anyMatch(animalAttributes -> animalAttributes.equals(newAnimalAttributes))) {
            throw new OfferConfigurationDuplicatedException(MessageFormat.format(
                            "Offer configuration with animal attributes {0} already exists",
                            newAnimalAttributes.stream()
                                    .map(AnimalAttribute::toString)
                                    .collect(Collectors.joining(", "))
                    ));
        }

    }

    private OfferConfiguration createConfiguration(OfferConfigurationDTO offerConfiguration, Offer offer) {
        OfferConfiguration newOfferConfiguration = OfferConfiguration.builder()
                .dailyPrice(offerConfiguration.dailyPrice())
                .description(offerConfiguration.description())
                .offer(offer)
                .build();

        List<OfferOption> offerOptions =
                createOfferOptionsForConfiguration(offerConfiguration.selectedOptions(), newOfferConfiguration);

        newOfferConfiguration.setOfferOptions(offerOptions);
        return newOfferConfiguration;
    }

    private List<OfferOption> createOfferOptionsForConfiguration(Map<String, List<String>> selectedOptions,
                                                                 OfferConfiguration offerConfiguration) {

        List<OfferOption> offerOptions = new ArrayList<>();
        for(Map.Entry<String, List<String>> entry : selectedOptions.entrySet()) {
            String attributeName = entry.getKey();
            for(String attributeValue : entry.getValue()) {
                AnimalAttribute animalAttribute = animalService.getAnimalAttribute(offerConfiguration.getOffer().getAnimal().getAnimalType(),
                        attributeName, attributeValue);
                offerOptions.add(createOfferOption(animalAttribute, offerConfiguration));
            }
        }
        return offerOptions;

    }

    private OfferOption createOfferOption(AnimalAttribute animalAttribute, OfferConfiguration offerConfiguration) {
        return OfferOption.builder()
                .animalAttribute(animalAttribute)
                .offerConfiguration(offerConfiguration)
                .build();
    }

    private Offer getOrCreateOffer(String caretakerEmail, String animalType, Caretaker caretaker,
                                   String description) {
        return offerRepository.findByCaretaker_EmailAndAnimal_AnimalType(caretakerEmail, animalType)
                .orElse(Offer.builder()
                        .caretaker(caretaker)
                        .animal(animalService.getAnimal(animalType))
                        .description(description)
                        .build());

    }

    private Offer getOffer(Long offerId) {
        return offerRepository.findById(offerId)
                .orElseThrow(() -> new NotFoundException("Offer with id " + offerId + " not found"));
    }

    private OfferConfiguration getOfferConfiguration(Long id) {
        return offerConfigurationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer configuration with id " + id + " not found"));
    }

    public OfferDTO deleteConfiguration(Long configurationId) {

        OfferConfiguration offerConfiguration = getOfferConfiguration(configurationId);
        Offer offer = offerConfiguration.getOffer();
        offer.getOfferConfigurations().remove(offerConfiguration);
        return offerMapper.mapToOfferDTO(offerRepository.save(offer));

    }

    public OfferConfigurationDTO editConfiguration(Long configurationId, OfferConfigurationDTO configuration) {

        OfferConfiguration offerConfiguration = getOfferConfiguration(configurationId);

        List<OfferConfiguration> restOfferConfigurations = offerConfiguration.getOffer().getOfferConfigurations().stream()
                .filter(restOfferConfiguration -> !restOfferConfiguration.getId().equals(offerConfiguration.getId()))
                .toList();

        offerConfiguration.setDescription(configuration.description());
        offerConfiguration.setDailyPrice(configuration.dailyPrice());
        editConfigurationSelectedOptions(offerConfiguration, configuration);
        checkForDuplicateConfiguration(restOfferConfigurations, offerConfiguration);
        return offerConfigurationMapper.mapToOfferConfigurationDTO(offerConfigurationRepository.save(offerConfiguration));
    }

    private void editConfigurationSelectedOptions(OfferConfiguration editingConfiguration, OfferConfigurationDTO configuration) {
        List<OfferOption> offerOptions = editingConfiguration.getOfferOptions();

        offerOptions.removeIf(offerOption -> !configuration.selectedOptions().containsKey(offerOption.getAnimalAttribute().getAttributeName()) ||
                !configuration.selectedOptions().get(offerOption.getAnimalAttribute().getAttributeName()).contains(offerOption.getAnimalAttribute().getAttributeValue()));

        for(Map.Entry<String, List<String>> entry : configuration.selectedOptions().entrySet()) {
            String attributeName = entry.getKey();
            for(String attributeValue : entry.getValue()) {
                AnimalAttribute animalAttribute = animalService.getAnimalAttribute(editingConfiguration.getOffer().getAnimal().getAnimalType(),
                        attributeName, attributeValue);
                if(offerOptions.stream().noneMatch(offerOption -> offerOption.getAnimalAttribute().equals(animalAttribute))) {
                    offerOptions.add(createOfferOption(animalAttribute, editingConfiguration));
                }
            }
        }
    }

    public List<OfferDTO> setAvailabilityForOffers(CreateOffersAvailabilityDTO createOffersAvailability) {

        assertAvailabilityRangesNotOverlapping(createOffersAvailability.availabilityRanges());
        List<Offer> modifiedOffers = createOffersAvailability.offerIds()
                .stream()
                .map(offerId -> setAvailabilityForOffer(offerId, createOffersAvailability.availabilityRanges()))
                .toList();

        return offerRepository.saveAll(modifiedOffers)
                .stream()
                .map(offerMapper::mapToOfferDTO)
                .toList();
    }

    private Offer setAvailabilityForOffer(Long offerId, List<AvailabilityRangeDTO> availabilityRanges) {

        Offer offerToModify = getOffer(offerId);
        Set<Availability> availabilities = createAvailabilities(availabilityRanges, offerToModify);

        if(CollectionUtil.isNotEmpty(offerToModify.getAvailabilities())) {
            offerToModify.getAvailabilities().clear();
            offerToModify.getAvailabilities().addAll(availabilities);
        } else {
            offerToModify.setAvailabilities(availabilities);
        }

        return offerToModify;
    }

    private Set<Availability> createAvailabilities(List<AvailabilityRangeDTO> availabilityRanges, Offer offer) {
        return availabilityRanges.stream()
                .map(availabilityRange -> createAvailability(availabilityRange, offer))
                .collect(Collectors.toSet());
    }

    private Availability createAvailability(AvailabilityRangeDTO availabilityRange, Offer offer) {

        return Availability.builder()
                .availableFrom(availabilityRange.availableFrom())
                .availableTo(availabilityRange.availableTo())
                .offer(offer)
                .build();

    }

    private void assertAvailabilityRangesNotOverlapping(List<AvailabilityRangeDTO> availabilityRanges) {
        for(int i = 0; i < availabilityRanges.size(); i++) {
            for(int j = i + 1; j < availabilityRanges.size(); j++) {
                if(availabilityRanges.get(i).overlaps(availabilityRanges.get(j))) {
                    throw new IllegalArgumentException(
                        MessageFormat.format(
                            "Availability range {0} overlaps with availability range {1}",
                            availabilityRanges.get(i).toString(),
                            availabilityRanges.get(j).toString()
                        )
                    );
                }
            }
        }
    }

}
