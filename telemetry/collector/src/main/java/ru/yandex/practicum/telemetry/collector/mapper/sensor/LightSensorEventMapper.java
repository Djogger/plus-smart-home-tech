package ru.yandex.practicum.telemetry.collector.mapper.sensor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;

@Slf4j
@Component
public class LightSensorEventMapper extends BaseSensorEventMapper<LightSensorAvro> {
    @Override
    protected LightSensorAvro mapToAvroPayload(SensorEventProto event) {
        LightSensorProto sensorEvent = event.getLightSensorEvent();

        log.info("Маппинг события {} - результат: {}", LightSensorProto.class.getSimpleName(), sensorEvent);

        return LightSensorAvro.newBuilder()
                .setLinkQuality(sensorEvent.getLinkQuality())
                .setLuminosityl(sensorEvent.getLuminosity())
                .build();
    }

    @Override
    public SensorEventProto.PayloadCase getSensorEventType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }
}
