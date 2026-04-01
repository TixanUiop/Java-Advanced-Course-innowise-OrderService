package com.evgeny.orderservice.mapper.item;

import com.evgeny.orderservice.dto.item.CreateItemsDTO;
import com.evgeny.orderservice.dto.item.FullItemsDTO;
import com.evgeny.orderservice.entity.ItemsEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemsEntity toItemsEntityFromFullItemsDTO(FullItemsDTO dto);

    ItemsEntity toItemsEntityCreateItemsDTO(CreateItemsDTO dto);


    @Mapping(target = "id", source = "id")
    FullItemsDTO toFullItemsDTOFromItemsEntity(ItemsEntity dto);

    CreateItemsDTO toCreateItemsDTOFromItemsEntity(ItemsEntity dto);

}
