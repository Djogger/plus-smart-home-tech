package ru.yandex.practicum.error;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ErrorResponse {
    private Throwable cause;
    private List<StackTraceElement> stackTrace;
    String httpStatus;
    String userMessage;
    String message;
    List<Throwable> suppressed;
    String localizedMessage;
}
