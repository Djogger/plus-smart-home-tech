package ru.yandex.practicum.service.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaEventProducer;
import ru.yandex.practicum.enums.sensor.SensorEventType;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Component
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro> {
    public MotionSensorEventHandler(KafkaEventProducer producer) {super(producer);}

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEvent event) {
        String topic = "telemetry.sensors.v1";
        MotionSensorAvro avroEvent = mapToAvro(event);
        super.producer.send(topic, avroEvent);
    }

    @Override
    protected MotionSensorAvro mapToAvro(SensorEvent event) {
        MotionSensorEvent _event = (MotionSensorEvent) event;

        return MotionSensorAvro.newBuilder()
                .setMotion(_event.isMotion())
                .setLinkQuality(_event.getLinkQuality())
                .setVoltage(_event.getVoltage())
                .build();
    }

}
