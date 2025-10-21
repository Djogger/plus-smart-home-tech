package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.CartState;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> request) {
        checkUsername(username);

        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Request null, либо пустой.");
        }

        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        shoppingCart.getProducts().putAll(request);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartMapper.toShoppingCartDto(shoppingCart));

        shoppingCart = cartRepository.save(shoppingCart);

        log.info("Сохранение корзины: {}", shoppingCart);

        return shoppingCartMapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productsId) {
        checkUsername(username);

        if (productsId == null || productsId.isEmpty()) {
            throw new IllegalArgumentException("Список удаляемых продуктов null, либо пустой.");
        }

        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);

        if (shoppingCart.getProducts().isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина уже пуста.");
        }

        for (UUID id : productsId) {
            shoppingCart.getProducts().remove(id);
        }

        shoppingCart = cartRepository.save(shoppingCart);

        log.info("Корзина обновлена: {}", shoppingCart);

        return shoppingCartMapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest requestDto) {
        checkUsername(username);

        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);

        if (!shoppingCart.getProducts().containsKey(requestDto.getProductId())) {
            throw new NoProductsInShoppingCartException("В корзине отсутствует товар с id: " + requestDto.getProductId());
        }

        shoppingCart.getProducts().put(requestDto.getProductId(), requestDto.getNewQuantity());

        warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartMapper.toShoppingCartDto(shoppingCart));

        shoppingCart = cartRepository.save(shoppingCart);

        log.info("Обновлена корзина: {}", shoppingCart);

        return shoppingCartMapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        return shoppingCartMapper.toShoppingCartDto(shoppingCart);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = cartRepository.findByUsername(username);
        shoppingCart.setCartState(CartState.DEACTIVATE);
    }

    private ShoppingCart getActiveShoppingCartByUserName(String username) {
        Optional<ShoppingCart> shoppingCartOpt = cartRepository.findByUsernameAndCartStateAllIgnoreCase(username, CartState.ACTIVE);

        ShoppingCart shoppingCart;

        if (shoppingCartOpt.isEmpty()) {
            log.info("У пользователя: {} - деактивированная корзина", username);
            shoppingCart = new ShoppingCart();
            shoppingCart.setUsername(username);
            shoppingCart.setCartState(CartState.ACTIVE);
            shoppingCart.setProducts(new HashMap<>());
            shoppingCart = cartRepository.save(shoppingCart);
            cartRepository.flush();

            log.info("Создана новая корзина: {}", shoppingCart);
        } else {
            shoppingCart = shoppingCartOpt.get();
            log.info("Корзина пользователя: {} - {}", username, shoppingCart);
        }

        return shoppingCart;
    }

    private void checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя пустое.");
        }
    }

}