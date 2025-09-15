package ru.yandex.practicum.service.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaEventProducer;
import ru.yandex.practicum.enums.hub.HubEventType;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.hub.ScenarioRemovedEvent;

@Component
public class ScenarioRemovedEventHandler extends BaseHubHandler<ScenarioRemovedEventAvro> {
    public ScenarioRemovedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEvent event) {
        String topic = "telemetry.hubs.v1";
        ScenarioRemovedEventAvro avroEvent = mapToAvro(event);
        super.producer.send(topic, avroEvent);
    }

    @Override
    protected ScenarioRemovedEventAvro mapToAvro(HubEvent event) {
        ScenarioRemovedEvent _event = (ScenarioRemovedEvent) event;

        return ScenarioRemovedEventAvro.newBuilder()
                .setName(_event.getName())
                .build();
    }
}
