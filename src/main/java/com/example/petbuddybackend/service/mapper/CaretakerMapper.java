package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerComplexInfoDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CreateCaretakerDTO;
import com.example.petbuddybackend.dto.user.UpdateCaretakerDTO;
import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.photo.PhotoLink;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;

@Mapper(uses = {OfferMapper.class, AddressMapper.class, UserMapper.class})
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);
    UserMapper userMapper = UserMapper.INSTANCE;

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    @Mapping(target = "accountData", expression = "java(userMapper.mapToAccountDataDTO(caretaker.getAccountData(), profilePicture))")
    CaretakerComplexInfoDTO mapToCaretakerComplexInfoDTO(Caretaker caretaker, PhotoLink profilePicture);

    @Mapping(target = "accountData", source = "accountData")
    Caretaker mapToCaretaker(CreateCaretakerDTO caretakerDTO, AppUser accountData);

    @Mapping(target = "animals", source = "caretaker.offers", qualifiedByName = "mapAnimalFromOffer")
    @Mapping(target = "accountData", expression = "java(userMapper.mapToAccountDataDTO(caretaker.getAccountData(), profilePicture))")
    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker, PhotoLink profilePicture);

    default void updateCaretakerFromDTO(UpdateCaretakerDTO caretakerDTO, @MappingTarget Caretaker caretaker) {
        if (StringUtils.hasText(caretakerDTO.phoneNumber())) {
            caretaker.setPhoneNumber(caretakerDTO.phoneNumber());
        }
        if (StringUtils.hasText(caretakerDTO.description())) {
            caretaker.setDescription(caretakerDTO.description());
        }
        if (caretakerDTO.address() != null) {
            if(caretaker.getAddress() == null) {
                caretaker.setAddress(new Address());
            }
            AddressMapper.INSTANCE.updateAddressFromDTO(caretakerDTO.address(), caretaker.getAddress());
        }
    }

    @Named("mapAnimalFromOffer")
    default String mapAnimalFromOffer(Offer offer) {
        return offer.getAnimal().getAnimalType();
    }

}
