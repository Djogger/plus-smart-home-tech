package ru.yandex.practicum.service.hub;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.configuration.KafkaEventProducer;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

public abstract class BaseHubHandler<T extends SpecificRecordBase> implements HubHandler {
    protected final KafkaEventProducer producer;

    public BaseHubHandler(KafkaEventProducer producer) {
        this.producer = producer;
    }

    protected abstract T mapToAvro(HubEvent event);

}
