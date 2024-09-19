package com.hhplus.hhplusqueueservice.interfaces.consumer;

import com.hhplus.hhplusqueueservice.domain.event.PaymentEvent;
import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueService;
import com.hhplus.hhplusqueueservice.domain.queue.command.WaitingQueueCommand;
import com.hhplus.hhplusqueueservice.support.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.hhplus.hhplusqueueservice.support.config.KafkaTopicConfig.KafkaConstants.PAYMENT_TOPIC;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueConsumer {

    private final WaitingQueueService waitingQueueService;

    @KafkaListener(topics = PAYMENT_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentEvent(String key, String message) {
        log.info("[KAFKA] :: CONSUMER:: Received PAYMENT_TOPIC, key: {}, payload: {}", key, message);
        PaymentEvent payload = JsonUtils.toObject(message, PaymentEvent.class);

        if (payload != null && payload.getStatus().equals(PaymentEvent.EventConstants.DEDUCTION_COMPLETED)) {
            waitingQueueService.forceExpireToken(WaitingQueueCommand.Expire.toCommand(payload));
        }

//        ack.acknowledge(); //수동으로 offset 커밋

    }
}
