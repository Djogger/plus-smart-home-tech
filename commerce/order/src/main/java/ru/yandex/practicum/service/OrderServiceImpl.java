package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feign.DeliveryClient;
import ru.yandex.practicum.feign.PaymentClient;
import ru.yandex.practicum.feign.ShoppingCartClient;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ShoppingCartClient shoppingCartClient;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    @Override
    public OrderDto createOrder(CreateNewOrderRequest newOrderRequest) {
        Order order = Order.builder()
                .shoppingCartId(newOrderRequest.getShoppingCartDto().getShoppingCartId())
                .products(
                        newOrderRequest.getShoppingCartDto().getProducts()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().intValue()
                                ))
                )
                .state(OrderState.NEW)
                .build();
        Order newOrder = orderRepository.save(order);

        BookedProductsDto bookedProducts = warehouseClient.assemblyProductForOrder(
                new AssemblyProductsForOrderRequest(
                        newOrderRequest.getShoppingCartDto().getShoppingCartId(),
                        newOrder.getOrderId()
                ));

        newOrder.setFragile(bookedProducts.getFragile());
        newOrder.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        newOrder.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        newOrder.setProductPrice(paymentClient.productCost(orderMapper.toOrderDto(newOrder)));

        DeliveryDto deliveryDto = DeliveryDto.builder()
                .orderId(newOrder.getOrderId())
                .fromAddress(warehouseClient.getAddress())
                .toAddress(newOrderRequest.getAddressDto())
                .build();
        newOrder.setDeliveryId(deliveryClient.planDelivery(deliveryDto).getDeliveryId());

        paymentClient.createPayment(orderMapper.toOrderDto(newOrder));

        return orderMapper.toOrderDto(newOrder);
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest returnRequest) {
        Order order = getOrder(returnRequest.getOrderId());
        warehouseClient.acceptReturn(returnRequest.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAID);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto failedPayment(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto failedDelivery(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto completeOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.COMPLETED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        Order order = getOrder(orderId);
        order.setTotalPrice(paymentClient.getTotalCost(orderMapper.toOrderDto(order)));

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        Order order = getOrder(orderId);
        order.setDeliveryPrice(deliveryClient.deliveryCost(orderMapper.toOrderDto(order)));

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public OrderDto failedAssembly(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);

        return orderMapper.toOrderDto(order);
    }

    @Override
    public List<OrderDto> getOrders(String username, Integer page, Integer size) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым.");
        }

        ShoppingCartDto shoppingCart = shoppingCartClient.getShoppingCart(username);

        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");

        PageRequest pageRequest = PageRequest.of(page, size, sortByCreated);

        Page<Order> orders = orderRepository.findByShoppingCartId(shoppingCart.getShoppingCartId(), pageRequest);

        return orderMapper.toOrdersDto(orders.getContent());
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ с id: " + orderId + " не был найден."));
    }
    
}
