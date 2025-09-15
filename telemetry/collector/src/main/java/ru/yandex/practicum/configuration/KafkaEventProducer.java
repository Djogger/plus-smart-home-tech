package ru.yandex.practicum.configuration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
public class KafkaEventProducer {
    private final Producer<String, SpecificRecordBase> producer;

    public KafkaEventProducer() {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        producer = new KafkaProducer<>(config);
    }

    public void send(String topic, SpecificRecordBase event) {
        if (event == null) {
            return;
        }

        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, event);

        Future<RecordMetadata> futureResult = producer.send(record);
        producer.flush();

        try {
            RecordMetadata metadata = futureResult.get();
            log.info("Событие {} успешно записано в топик {} в партицию {} со смещением {}",
                    record, metadata.topic(), metadata.partition(), metadata.offset());
        } catch (InterruptedException | ExecutionException ex) {
            log.warn("Не удалось записать событие ", ex);
        }
    }

    @PreDestroy
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }

}
