package ru.yandex.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductAlreadyInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exception.ProductNotFoundInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Warehouse;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.request.NewProductInWarehouseRequest;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final ShoppingStoreClient shoppingStoreClient;

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest newProductInWarehouseRequest) {
        warehouseRepository.findById(newProductInWarehouseRequest.getProductId()).ifPresent(warehouse -> {
            throw new ProductAlreadyInWarehouseException("Дублирование описания товара на складе.");
        });

        Warehouse warehouse = warehouseMapper.toWarehouse(newProductInWarehouseRequest);
        warehouseRepository.save(warehouse);
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest addProductToWarehouseRequest) {
        Warehouse warehouse = warehouseRepository.findById(addProductToWarehouseRequest.getProductId()).orElseThrow(
                () -> new NoSpecifiedProductInWarehouseException("Отсутствует информация о товаре.")
        );

        warehouse.setQuantity(warehouse.getQuantity() + addProductToWarehouseRequest.getQuantity());
        updateProductQuantityInShoppingStore(warehouse);
    }

    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Integer> products = shoppingCartDto.getProducts();
        Set<UUID> cartProductIds = products.keySet();

        Map<UUID, Warehouse> warehouseProducts = warehouseRepository.findAllById(cartProductIds)
                .stream()
                .collect(Collectors.toMap(Warehouse::getProductId, Function.identity()));

        Set<UUID> productIds = warehouseProducts.keySet();

        cartProductIds.forEach(id -> {
            if (!productIds.contains(id)) {
                throw new ProductNotFoundInWarehouseException("Товар отсутствует на складе.");
            }
        });

        products.forEach((key, value) -> {
            if (warehouseProducts.get(key).getQuantity() < value) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        "На складе недостаточно товара.");
            }
        });

        return getBookedProducts(warehouseProducts.values(), products);
    }

    @Override
    public AddressDto getAddress() {
        String address = Address.CURRENT_ADDRESS;

        return AddressDto.builder()
                .country(address)
                .city(address)
                .street(address)
                .house(address)
                .flat(address)
                .build();
    }

    private double calculateTotalWeight(Collection<Warehouse> productList, Map<UUID, Integer> cartProducts) {
        return productList.stream()
                .mapToDouble(prod -> multiply(prod.getWeight(), getQuantity(prod, cartProducts)))
                .sum();
    }

    private double calculateTotalVolume(Collection<Warehouse> productList, Map<UUID, Integer> cartProducts) {
        return productList.stream()
                .mapToDouble(prod -> {
                    double width = getValue(prod.getWidth());
                    double height = getValue(prod.getHeight());
                    double depth = getValue(prod.getDepth());
                    int quantity = getQuantity(prod, cartProducts);
                    return width * height * depth * quantity;
                })
                .sum();
    }

    private void updateProductQuantityInShoppingStore(Warehouse product) {
        UUID productId = product.getProductId();
        QuantityState quantityState;
        int quantity = product.getQuantity();

        if (quantity == 0) {
            quantityState = QuantityState.ENDED;
        } else if (quantity < 10) {
            quantityState = QuantityState.ENOUGH;
        } else if (quantity < 100) {
            quantityState = QuantityState.FEW;
        } else {
            quantityState = QuantityState.MANY;
        }
        
        shoppingStoreClient.setProductQuantityState(productId, quantityState);
    }

    private BookedProductsDto getBookedProducts(Collection<Warehouse> productList, Map<UUID, Integer> cartProducts) {
        return BookedProductsDto.builder()
                .fragile(productList.stream().anyMatch(Warehouse::getFragile))
                .deliveryWeight(calculateTotalWeight(productList, cartProducts))
                .deliveryVolume(calculateTotalVolume(productList, cartProducts))
                .build();
    }

    private double getValue(Double value) {
        return value != null ? value : 0.0;
    }

    private int getQuantity(Warehouse prod, Map<UUID, Integer> cartProducts) {
        return cartProducts.getOrDefault(prod.getProductId(), 0);
    }

    private double multiply(Double value, int multiplier) {
        return value != null ? value * multiplier : 0;
    }

}
