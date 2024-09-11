package com.hhplus.hhplusqueueservice.domain.queue;

import com.hhplus.hhplusqueueservice.domain.common.exception.CustomException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.hhplus.hhplusqueueservice.domain.common.exception.ErrorCode.ALREADY_TOKEN_IS_ACTIVE;
import static com.hhplus.hhplusqueueservice.domain.common.exception.ErrorCode.TOKEN_IS_NOT_YET;
import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueConstants.MAX_ACTIVE_USER;
import static java.time.LocalDateTime.now;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Getter
public class WaitingQueue {
    private Long waitingQueueId;

    private Long userId;

    private String token;

    private WaitingQueueStatus status; // 대기 / 활성 / 만료

    private LocalDateTime requestTime; // 토큰 요청 시각

    private LocalDateTime activeTime; // 토큰 활성화 시각

    private Long waitingNum;

    private Long waitTimeInSeconds;

    public void addWaitingInfo(long waitingNum, long waitTimeInSeconds) {
        this.waitingNum = waitingNum;
        this.waitTimeInSeconds = waitTimeInSeconds;
    }


    public static long calculateActiveCnt(long activeTokenCnt) {

        return MAX_ACTIVE_USER - activeTokenCnt;
    }

    public enum WaitingQueueStatus {

        WAIT, // 대기 중
        ACTIVE, // 활성화
        EXPIRED // 만료
    }

    public static WaitingQueue toDomain(long availableActiveTokenCnt, Long userId, String token) {

        if (availableActiveTokenCnt > 0) return WaitingQueue.toActiveDomain(userId, token);

        return WaitingQueue.toWaitingDomain(userId, token);
    }

    public static WaitingQueue toActiveDomain(Long userId, String token) {
        return WaitingQueue.builder()
                .userId(userId)
                .token(token)
                .status(WaitingQueueStatus.ACTIVE)
                .requestTime(now())
                .activeTime(now())
                .build();
    }

    public static WaitingQueue toWaitingDomain(Long userId, String token) {
        return WaitingQueue.builder()
                .userId(userId)
                .token(token)
                .status(WaitingQueueStatus.WAIT)
                .requestTime(now())
                .build();
    }


    public void isActive() {
        if (status == WaitingQueueStatus.ACTIVE) {
            throw new CustomException(ALREADY_TOKEN_IS_ACTIVE, "이미 활성화 된 토큰입니다.");
        }
    }


    public void expireOver10min() {
        if (activeTime.isAfter(LocalDateTime.now().plusMinutes(10))) { //10분뒤에 만료
            throw new CustomException(TOKEN_IS_NOT_YET,
                    "토큰 만료 대상이 아닙니다.");
        }
        expire();
    }

    public void expire() {
        status = WaitingQueueStatus.EXPIRED;
    }

    public void active() {
        if (status == WaitingQueueStatus.ACTIVE) {
            throw new CustomException(ALREADY_TOKEN_IS_ACTIVE,
                    "이미 토큰이 활성화 상태입니다.");
        }
        status = WaitingQueueStatus.ACTIVE;
        activeTime = LocalDateTime.now();
    }


}
