package com.hhplus.hhplusqueueservice.domain.outbox.command;

import com.hhplus.hhplusqueueservice.domain.outbox.Outbox;

import static com.hhplus.hhplusqueueservice.domain.outbox.Outbox.*;

public class OutboxCommand {
    public record Create(
            String messageId,
            EventStatus status,
            String payload) {

        public Outbox toDomain() {
            return builder()
                    .messageId(messageId)
                    .status(status)
                    .payload(payload)
                    .build();
        }
    }
}
