package ru.yandex.practicum.exception;

public class NoSpecifiedProductInWarehouseException extends  RuntimeException {
    public NoSpecifiedProductInWarehouseException(String ex) {
        super(ex);
    }
}
