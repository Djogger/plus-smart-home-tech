package ru.yandex.practicum.exception;

public class ProductInShoppingCartLowQuantityInWarehouseException extends RuntimeException {
    public ProductInShoppingCartLowQuantityInWarehouseException(String ex) {
        super(ex);
    }
}
