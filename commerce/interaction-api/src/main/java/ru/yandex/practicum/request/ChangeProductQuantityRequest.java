package ru.yandex.practicum.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ChangeProductQuantityRequest {
    @NotNull
    private UUID productId;

    @NotNull
    private Integer newQuantity;
}
