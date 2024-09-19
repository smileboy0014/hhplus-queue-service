package com.hhplus.hhplusqueueservice.domain.queue.listener;

import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueEventListener {

    private final WaitingQueueService waitingQueueService;

//    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//    public void onPaymentEvent(PaymentEvent event) {
//        waitingQueueService.forceExpireToken(event.getToken());
//    }
}
