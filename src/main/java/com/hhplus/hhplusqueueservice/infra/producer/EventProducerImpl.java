package com.hhplus.hhplusqueueservice.infra.producer;

import com.hhplus.hhplusqueueservice.domain.producer.EventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventProducerImpl implements EventProducer {

    private final KafkaProducer kafkaProducer;

    @Override
    public void publish(String topic, String key, String payload) {
        kafkaProducer.publish(topic, key, payload);
    }
}
