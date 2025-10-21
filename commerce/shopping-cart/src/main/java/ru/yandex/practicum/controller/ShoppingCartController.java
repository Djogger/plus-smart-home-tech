package ru.yandex.practicum.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.request.ChangeProductQuantityRequest;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam @NotBlank String userName, @RequestBody Map<UUID, Integer> request) {
        return shoppingCartService.addProduct(userName, request);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam @NotBlank String userName, @RequestBody List<UUID> productsId) {
        return shoppingCartService.removeFromShoppingCart(userName, productsId);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam @NotBlank String userName, @RequestBody ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(userName, request);
    }

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String userName) {
        return  shoppingCartService.getShoppingCart(userName);
    }

    @DeleteMapping
    public void deactivateShoppingCart(@RequestParam @NotBlank String userName) {
        shoppingCartService.deactivateShoppingCart(userName);
    }

}
