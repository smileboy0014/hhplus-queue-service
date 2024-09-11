package com.hhplus.hhplusqueueservice.domain.outbox.command;

import com.hhplus.hhplusqueueservice.domain.outbox.Outbox;

import static com.hhplus.hhplusqueueservice.domain.outbox.Outbox.*;

public class OutboxCommand {
    public record Create(
            String messageId,
            DomainType type,
            EventStatus status,
            String payload) {

        public Outbox toDomain() {
            return builder()
                    .messageId(messageId)
                    .type(type)
                    .status(status)
                    .payload(payload)
                    .build();
        }
    }
}
