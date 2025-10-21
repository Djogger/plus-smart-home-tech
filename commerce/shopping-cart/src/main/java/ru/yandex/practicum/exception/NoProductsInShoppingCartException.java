package ru.yandex.practicum.exception;

public class NoProductsInShoppingCartException extends RuntimeException {
    public NoProductsInShoppingCartException(String ex) {
        super(ex);
    }
}
