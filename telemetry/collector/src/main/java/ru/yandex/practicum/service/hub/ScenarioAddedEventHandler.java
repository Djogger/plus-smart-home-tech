package ru.yandex.practicum.service.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaEventProducer;
import ru.yandex.practicum.enums.hub.HubEventType;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.mapper.DeviceActionMapper;
import ru.yandex.practicum.mapper.ScenarioConditionMapper;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.hub.ScenarioAddedEvent;

@Component
public class ScenarioAddedEventHandler extends BaseHubHandler<ScenarioAddedEventAvro> {
    private final DeviceActionMapper deviceMapper;
    private final ScenarioConditionMapper scenarioMapper;

    public ScenarioAddedEventHandler(KafkaEventProducer producer, DeviceActionMapper deviceMapper, ScenarioConditionMapper scenarioMapper) {
        super(producer);
        this.deviceMapper = deviceMapper;
        this.scenarioMapper = scenarioMapper;
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEvent event) {
        String topic = "telemetry.hubs.v1";
        ScenarioAddedEventAvro avroEvent = mapToAvro(event);
        super.producer.send(topic, avroEvent);
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEvent event) {
        ScenarioAddedEvent _event = (ScenarioAddedEvent) event;

        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setActions(deviceMapper.mapToAvro(_event.getActions()))
                .setConditions(scenarioMapper.mapToAvro(_event.getConditions()))
                .build();
    }
}
