package ru.yandex.practicum.service.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaEventProducer;
import ru.yandex.practicum.enums.sensor.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Component
public class ClimateSensorEventHandler extends BaseSensorEventHandler<ClimateSensorAvro> {
    public ClimateSensorEventHandler(KafkaEventProducer producer) {super(producer);}

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent event) {
        String topic = "telemetry.sensors.v1";
        ClimateSensorAvro avroEvent = mapToAvro(event);
        super.producer.send(topic, avroEvent);
    }

    @Override
    protected ClimateSensorAvro mapToAvro(SensorEvent event) {
        ClimateSensorEvent _event = (ClimateSensorEvent) event;

        return ClimateSensorAvro.newBuilder()
                .setCo2Level(_event.getCo2Level())
                .setHumidity(_event.getHumidity())
                .setTemperatureC(_event.getTemperatureC())
                .build();
    }
}
