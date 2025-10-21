package ru.yandex.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;
import ru.yandex.practicum.request.SetProductQuantityStateRequest;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {
    private final ShoppingStoreRepository shoppingStoreRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        Product product = productMapper.productDtoToProduct(productDto);
        return productMapper.productToProductDto(shoppingStoreRepository.save(product));
    }

    @Override
    public ProductDto updateProduct(ProductDto newProductDtoInfo) {
        Product product = shoppingStoreRepository.findById(newProductDtoInfo.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Товар с id: " + newProductDtoInfo.getProductId() + " не найден."));

        Product updatedProduct = productMapper.productDtoToProduct(newProductDtoInfo);
        updatedProduct.setProductId(product.getProductId());

        return productMapper.productToProductDto(shoppingStoreRepository.save(updatedProduct));
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        Product product = shoppingStoreRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар с id: " + productId + " не найден."));

        product.setProductState(ProductState.DEACTIVATE);

        shoppingStoreRepository.save(product);

        return true;
    }

    @Override
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = shoppingStoreRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Товар с id: " + request.getProductId() + " не найден."));

        if (shoppingStoreRepository.findById(request.getProductId()).isEmpty()) {
            return false;
        }

        if (!product.getQuantityState().equals(request.getQuantityState())) {
            product.setQuantityState(request.getQuantityState());
            shoppingStoreRepository.save(product);
        }

        return true;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory productCategory, Pageable pageable) {
        Sort sort = pageable.getSort();

        if (sort.isEmpty()) {
            sort = Sort.by(Sort.Direction.ASC, "productName");
        }

        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<Product> products = shoppingStoreRepository.findAllByProductCategory(
                productCategory,
                pageRequest
        );

        return products.map(productMapper::productToProductDto);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        Product product = shoppingStoreRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар с id: " + productId + " не найден."));

        return productMapper.productToProductDto(product);
    }

}
