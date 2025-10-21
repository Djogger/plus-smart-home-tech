package ru.yandex.practicum.exception;

public class ProductNotFoundInWarehouseException extends RuntimeException {
    public ProductNotFoundInWarehouseException(String ex) {
        super(ex);
    }
}
