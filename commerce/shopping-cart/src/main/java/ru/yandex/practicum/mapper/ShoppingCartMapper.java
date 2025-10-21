package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ShoppingCartMapper {
    ShoppingCart shoppingCartDtoToShoppingCart(ShoppingCartDto shoppingCartDto);

    ShoppingCartDto shoppingCartToShoppingCartDto(ShoppingCart shoppingCart);

}
