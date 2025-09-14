package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.DeviceAction;

import java.util.List;

@Component
public class DeviceActionMapper {
    public DeviceActionAvro mapToAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }

    public List<DeviceActionAvro> mapToAvro(List<DeviceAction> actions) {
        return actions.stream().map(this::mapToAvro).toList();
    }
}
