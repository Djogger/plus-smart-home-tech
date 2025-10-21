package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto addProduct(String userName, Map<UUID, Integer> request);

    ShoppingCartDto removeFromShoppingCart(String userName, List<UUID> productsId);

    ShoppingCartDto changeProductQuantity(String userName, ChangeProductQuantityRequest request);

    ShoppingCartDto getShoppingCart(String userName);

    void deactivateShoppingCart(String userName);

}
