package com.example.petbuddybackend.service.mapper;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.entity.user.Caretaker;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CaretakerMapper {

    CaretakerMapper INSTANCE = Mappers.getMapper(CaretakerMapper.class);

    CaretakerDTO mapToCaretakerDTO(Caretaker caretaker);
}