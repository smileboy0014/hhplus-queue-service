package com.hhplus.hhplusqueueservice.domain.queue.command;

public class WaitingQueueCommand {

    public record Create(Long userId, String token) {
    }

}
