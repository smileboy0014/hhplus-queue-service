package com.hhplus.hhplusqueueservice.interfaces.controller.queue.dto;

import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue;
import com.hhplus.hhplusqueueservice.domain.queue.command.WaitingQueueCommand;
import lombok.Builder;

import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue.WaitingQueueStatus.ACTIVE;

public class WaitingQueueDto {

    @Builder(toBuilder = true)
    public record Request(Long userId, String token) {
        public WaitingQueueCommand.Create toCreateCommand() {
            return new WaitingQueueCommand.Create(userId, token);
        }
    }

    @Builder(toBuilder = true)
    public record Response(Long userId, String token, boolean isActive,
                           WaitingInfo waitingInfo) {

        public static Response of(WaitingQueue queue) {
            return Response.builder()
                    .userId(queue.getUserId())
                    .token(queue.getToken())
                    .isActive(queue.getStatus() == ACTIVE)
                    .waitingInfo(queue.getWaitingNum() != null ? new WaitingInfo(queue.getWaitingNum(), queue.getWaitTimeInSeconds()) : null)
                    .build();

        }

        public record WaitingInfo(
                long waitingNumber,
                long waitTimeInSeconds
        ) {
        }
    }
}
