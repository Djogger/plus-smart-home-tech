package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.request.SetProductQuantityStateRequest;

import java.util.UUID;

public interface ShoppingStoreService {
    public ProductDto createNewProduct(ProductDto productDto);

    public ProductDto updateProduct(ProductDto productDto);

    public Boolean removeProductFromStore(UUID productId);

    public Boolean setProductQuantityState(SetProductQuantityStateRequest productQuantityStateRequest);

    public Page<ProductDto> getProducts(ProductCategory productCategory, Pageable pageable);

    public ProductDto getProduct(UUID productId);

}