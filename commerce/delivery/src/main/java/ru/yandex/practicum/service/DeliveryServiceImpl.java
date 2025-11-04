package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.enums.DeliveryState;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;
import ru.yandex.practicum.request.ShippedToDeliveryRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryServiceImpl implements DeliveryService {
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;

    private static final String ADDRESS1 = "ADDRESS_1";
    private static final String ADDRESS2 = "ADDRESS_2";

    private final double baseCost = 5.;

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.toDelivery(deliveryDto);
        delivery.setDeliveryState(DeliveryState.CREATED);
        return deliveryMapper.toDeliveryDto(deliveryRepository.save(delivery));
    }

    @Override
    public void deliverySuccess(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new NoDeliveryFoundException("Не найдена доставка."));
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        orderClient.completeOrder(delivery.getOrderId());
    }

    @Override
    public void deliveryPicked(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new NoDeliveryFoundException("Не найдена доставка для выдачи."));
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        orderClient.assembly(delivery.getOrderId());
        ShippedToDeliveryRequest deliveryRequest = new ShippedToDeliveryRequest(
                delivery.getOrderId(), delivery.getDeliveryId());
        warehouseClient.loadedToDelivery(deliveryRequest);
    }

    @Override
    public void failedDelivery(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow(
                () -> new NoDeliveryFoundException("Не найдена доставка."));
        delivery.setDeliveryState(DeliveryState.FAILED);
        orderClient.failedAssembly(delivery.getOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public Double deliveryCost(OrderDto orderDto) {
        UUID orderId = orderDto.getOrderId();

        Delivery delivery = getDelivery(orderId);
        AddressDto warehouseAddress = getWarehouseAddress();

        double base = calculateBaseCost(warehouseAddress);
        double fragileCost = calculateFragileCost(orderDto, base);
        double weightCost = calculateWeightCost(orderDto);
        double volumeCost = calculateVolumeCost(orderDto);
        double streetCost = calculateStreetCost(delivery, warehouseAddress);

        return base + fragileCost + weightCost + volumeCost + streetCost;
    }

    private Delivery getDelivery(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Заказа с id: " + orderId + " не найдено."));
    }

    private AddressDto getWarehouseAddress() {
        return warehouseClient.getAddress();
    }

    private double calculateBaseCost(AddressDto warehouseAddress) {
        double warehouseAddressRatio = 2.;
        double addressCost = switch (warehouseAddress.getCity()) {
            case ADDRESS1 -> baseCost;
            case ADDRESS2 -> baseCost * warehouseAddressRatio;
            default -> throw new IllegalStateException("Неизвестный адрес доставки: " + warehouseAddress.getCity());
        };

        return baseCost + addressCost;
    }

    private double calculateFragileCost(OrderDto orderDto, double base) {
        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            double fragileRatio = 0.2;
            return base * fragileRatio;
        }
        return 0.0;
    }

    private double calculateWeightCost(OrderDto orderDto) {
        double weightRatio = 0.3;
        return orderDto.getDeliveryWeight() * weightRatio;
    }

    private double calculateVolumeCost(OrderDto orderDto) {
        double volumeRatio = 0.2;
        return orderDto.getDeliveryVolume() * volumeRatio;
    }

    private double calculateStreetCost(Delivery delivery, AddressDto warehouseAddress) {
        if (!warehouseAddress.getStreet().equals(delivery.getToAddress().getStreet())) {
            double deliveryAddressRatio = 0.2;
            return baseCost * deliveryAddressRatio;
        }
        return 0.0;
    }
}
