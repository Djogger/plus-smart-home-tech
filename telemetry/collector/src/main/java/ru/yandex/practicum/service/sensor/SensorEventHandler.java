package ru.yandex.practicum.service.sensor;

import ru.yandex.practicum.enums.sensor.SensorEventType;
import ru.yandex.practicum.model.sensor.SensorEvent;

public interface SensorEventHandler {
    SensorEventType getMessageType();

    void handle(SensorEvent event);
}
