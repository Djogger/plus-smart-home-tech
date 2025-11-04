package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.PaymentState;
import ru.yandex.practicum.feign.OrderClient;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.exception.NoPaymentFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto createPayment(OrderDto orderDto) {
        checkOrder(orderDto);
        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .totalPayment(orderDto.getTotalPrice())
                .deliveryTotal(orderDto.getDeliveryPrice())
                .productsTotal(orderDto.getProductPrice())
                .feeTotal(getTax(orderDto.getTotalPrice()))
                .status(PaymentState.PENDING)
                .build();

        return paymentMapper.toPaymentDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalCost(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствует информация для расчёта.");
        }

        return orderDto.getProductPrice() + getTax(orderDto.getProductPrice()) + orderDto.getDeliveryPrice();
    }

    @Override
    public void paymentSuccess(UUID uuid) {
        Payment payment = getPayment(uuid);
        payment.setStatus(PaymentState.SUCCESS);
        orderClient.payment(payment.getOrderId());
    }

    @Override
    @Transactional(readOnly = true)
    public Double productCost(OrderDto orderDto) {
        double productCost = 0.0;
        Map<UUID, Long> products = orderDto.getProducts();

        if (products == null) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствует информация для расчёта.");
        }

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ProductDto product = shoppingStoreClient.getProduct(entry.getKey());
            productCost += product.getPrice() * entry.getValue();
        }

        return productCost;
    }

    @Override
    public void paymentFailed(UUID uuid) {
        Payment payment = getPayment(uuid);
        payment.setStatus(PaymentState.FAILED);
        orderClient.failedPayment(payment.getOrderId());
    }

    private Payment getPayment(UUID uuid) {
        return paymentRepository.findById(uuid)
                .orElseThrow(() -> new NoPaymentFoundException("Оплата заказа с id: " + uuid + " не найдена."));
    }

    private void checkOrder(OrderDto orderDto) {
        if (orderDto.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствует информация для расчёта.");
        }
        if (orderDto.getProductPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствует информация для расчёта.");
        }
        if (orderDto.getTotalPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствует информация для расчёта.");
        }
    }

    private double getTax(double totalPrice) {
        double feeRatio = 0.1;
        return totalPrice * feeRatio;
    }

}