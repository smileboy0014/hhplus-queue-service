package com.hhplus.hhplusqueueservice.domain.queue.command;

import com.hhplus.hhplusqueueservice.domain.event.PaymentEvent;

public class WaitingQueueCommand {

    public record Create(Long userId, String token) {
    }

    public record Expire(
            String paymentId,
            String token
    ) {
        public static WaitingQueueCommand.Expire toCommand(PaymentEvent event) {
            return new WaitingQueueCommand.Expire(event.getPaymentId(), event.getToken());

        }

    }

}
