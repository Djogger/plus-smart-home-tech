package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.enums.hub.HubEventType;
import ru.yandex.practicum.enums.sensor.SensorEventType;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.hub.HubHandler;
import ru.yandex.practicum.service.sensor.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping(value = "/events", consumes = MediaType.APPLICATION_JSON_VALUE)
public class CollectorController {
    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubHandler> hubEventHandlers;

    public CollectorController(List<SensorEventHandler> sensorEventHandlers, List<HubHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubHandler::getMessageType, Function.identity()));
    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        SensorEventHandler sensorEventHandler = sensorEventHandlers.get(event.getType());

        if (sensorEventHandler == null) {
            throw new IllegalArgumentException("Обработчик события для сенсора не был найден: " + event.getType());
        }

        sensorEventHandler.handle(event);
    }

    @PostMapping("/hubs")
    public void addOrDeleteFromHub(@Valid @RequestBody HubEvent event) {
        HubHandler hubEventHandler = hubEventHandlers.get(event.getType());

        if (hubEventHandler == null) {
            throw new IllegalArgumentException("Обработчик события для хаба не был найден: " + event.getType());
        }

        hubEventHandler.handle(event);
    }

}
