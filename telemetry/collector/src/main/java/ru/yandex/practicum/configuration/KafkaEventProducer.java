package ru.yandex.practicum.configuration;

import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.Properties;
import org.slf4j.Logger;

@Component
public class KafkaEventProducer {
    private final Producer<String, SpecificRecordBase> producer;
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);

    public KafkaEventProducer() {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());

        producer = new KafkaProducer<>(config);
    }

    public void send(String topic, SpecificRecordBase event) {
        logger.info("Отправляем в Kafka событие: {}", event);
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, event);
        producer.send(record);
    }

    @PreDestroy
    public void close() {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }

}
