package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;

import java.util.UUID;

public interface DeliveryService {
    DeliveryDto planDelivery(DeliveryDto deliveryDto);

    void deliverySuccess(UUID deliveryId);

    void deliveryPicked(UUID deliveryId);

    void failedDelivery(UUID deliveryId);

    Double deliveryCost(OrderDto orderDto);
}