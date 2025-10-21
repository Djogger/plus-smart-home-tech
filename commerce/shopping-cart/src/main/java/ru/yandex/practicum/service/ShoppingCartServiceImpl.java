package ru.yandex.practicum.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.CartState;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    @Override
    public ShoppingCartDto addProduct(String username, Map<UUID, Integer> request) {
        checkUsername(username);

        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Request null, либо пустой.");
        }
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        shoppingCart.getProducts().putAll(request);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartMapper.shoppingCartToShoppingCartDto(shoppingCart));

        shoppingCart = shoppingCartRepository.save(shoppingCart);

        log.info("Сохранение корзины: {}", shoppingCart);

        return shoppingCartMapper.shoppingCartToShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productsId) {
        checkUsername(username);
        if (productsId == null || productsId.isEmpty()) {
            throw new IllegalArgumentException("Список удаляемых продуктов null, либо пустой.");
        }
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        if (shoppingCart.getProducts().isEmpty()) {
            throw new NoProductsInShoppingCartException("Корзина пуста.");
        }
        for (UUID id : productsId) {
            shoppingCart.getProducts().remove(id);
        }
        shoppingCart = shoppingCartRepository.save(shoppingCart);

        log.info("Обновлена корзина: {}", shoppingCart);

        return shoppingCartMapper.shoppingCartToShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {

        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        if (!shoppingCart.getProducts().containsKey(request.getProductId())) {
            throw new NoProductsInShoppingCartException("В корзине не найден товар с id: " + request.getProductId());
        }

        shoppingCart.getProducts().put(request.getProductId(), request.getNewQuantity());

        warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartMapper.shoppingCartToShoppingCartDto(shoppingCart));

        shoppingCart = shoppingCartRepository.save(shoppingCart);

        log.info("Обновлена корзина: {}", shoppingCart);

        return shoppingCartMapper.shoppingCartToShoppingCartDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = getActiveShoppingCartByUserName(username);
        return shoppingCartMapper.shoppingCartToShoppingCartDto(shoppingCart);
    }

    @Override
    public void deactivateShoppingCart(String username) {
        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserName(username);
        shoppingCart.setCartState(CartState.DEACTIVATE);
    }

    private void checkUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя пустое.");
        }
    }

    private ShoppingCart getActiveShoppingCartByUserName(String username) {
        Optional<ShoppingCart> shoppingCartOpt = shoppingCartRepository.findByUserNameAndCartStateAllIgnoreCase(username, CartState.ACTIVE);
        ShoppingCart shoppingCart;
        if (shoppingCartOpt.isEmpty()) {
            log.info("У пользователя: {} - деактивированная корзина", username);

            shoppingCart = new ShoppingCart();
            shoppingCart.setUsername(username);
            shoppingCart.setCartState(CartState.ACTIVE);
            shoppingCart.setProducts(new HashMap<>());

            shoppingCart = shoppingCartRepository.save(shoppingCart);
            shoppingCartRepository.flush();

            log.info("Создана новая корзина: {}", shoppingCart);
        } else {
            shoppingCart = shoppingCartOpt.get();
            log.info("Корзина пользователя: {} - {}", username, shoppingCart);
        }

        return shoppingCart;
    }

}
