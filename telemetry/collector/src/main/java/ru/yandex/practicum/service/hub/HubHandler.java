package ru.yandex.practicum.service.hub;

import ru.yandex.practicum.enums.hub.HubEventType;
import ru.yandex.practicum.model.hub.HubEvent;

public interface HubHandler {
    HubEventType getMessageType();

    void handle(HubEvent event);
}
