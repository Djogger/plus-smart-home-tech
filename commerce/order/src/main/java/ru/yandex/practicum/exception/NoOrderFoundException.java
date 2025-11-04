package ru.yandex.practicum.exception;

public class NoOrderFoundException extends RuntimeException {
    public NoOrderFoundException(String ex) {
        super(ex);
    }
}
