package ru.yandex.practicum.exception;

public class ProductAlreadyInWarehouseException extends RuntimeException {
    public ProductAlreadyInWarehouseException(String ex) {
        super(ex);
    }

}
