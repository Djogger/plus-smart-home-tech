package ru.yandex.practicum.service.sensor;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.configuration.KafkaEventProducer;
import ru.yandex.practicum.model.sensor.SensorEvent;

public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler {
    protected final KafkaEventProducer<T> producer;

    public BaseSensorEventHandler(KafkaEventProducer<T> producer) {
        this.producer = producer;
    }

    protected abstract T mapToAvro(SensorEvent event);

}
