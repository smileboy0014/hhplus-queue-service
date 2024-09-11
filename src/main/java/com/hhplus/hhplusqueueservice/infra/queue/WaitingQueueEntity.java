package com.hhplus.hhplusqueueservice.infra.queue;

import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "waiting_queue")
public class WaitingQueueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long waitingQueueId;

    private Long userId;

    private String token;

    @Enumerated(EnumType.STRING)
    private WaitingQueue.WaitingQueueStatus status; // 대기 / 활성 / 만료

    private LocalDateTime requestTime; // 토큰 요청 시각

    private LocalDateTime activeTime; // 토큰 활성화 시각

    public static WaitingQueueEntity from(WaitingQueue queue) {
        return WaitingQueueEntity.builder()
                .waitingQueueId(queue.getWaitingQueueId() != null ? queue.getWaitingQueueId() : null)
                .userId(queue.getUserId())
                .token(queue.getToken())
                .status(queue.getStatus())
                .requestTime(queue.getRequestTime())
                .activeTime(queue.getActiveTime() != null ? queue.getActiveTime() : null)
                .build();
    }

    public WaitingQueue toDomain() {
        return WaitingQueue.builder()
                .waitingQueueId(waitingQueueId)
                .userId(userId)
                .token(token)
                .status(status)
                .requestTime(requestTime)
                .activeTime(activeTime)
                .build();
    }

}
