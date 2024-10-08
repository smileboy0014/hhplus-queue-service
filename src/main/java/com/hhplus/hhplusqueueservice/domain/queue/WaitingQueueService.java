package com.hhplus.hhplusqueueservice.domain.queue;

import com.hhplus.hhplusqueueservice.domain.event.PaymentEvent;
import com.hhplus.hhplusqueueservice.domain.queue.command.WaitingQueueCommand;
import com.hhplus.hhplusqueueservice.support.aop.DistributedLock;
import com.hhplus.hhplusqueueservice.support.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue.WaitingQueueStatus.ACTIVE;
import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue.WaitingQueueStatus.WAIT;
import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueConstants.AUTO_EXPIRED_TIME;
import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueConstants.ENTER_10_SECONDS;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingQueueService {

    private final JwtUtils jwtUtils;
    private final WaitingQueueRepository waitingQueueRepository;
    private final ApplicationEventPublisher publisher;

    /**
     * 토큰의 활성화 여부를 체크하여 토큰 대기열 정보를 반환한다.
     *
     * @param command userId, token 정보
     * @return WaitingQueue 대기열 정보
     */
    @Transactional
    @DistributedLock(key = "'waitingQueueLock'")
    public WaitingQueue checkWaiting(WaitingQueueCommand.Create command) {
        // 1. 토큰을 발급한다.
        String token = command.token();

        if (token == null) token = jwtUtils.createToken(command.userId());
        // 2. 현재 활성 유저 수 확인
        long activeTokenCnt = waitingQueueRepository.getActiveCnt();
        // 3. 활성화 시킬 수 있는 수 계산
        long availableActiveTokenCnt = WaitingQueue.calculateActiveCnt(activeTokenCnt);

        if (availableActiveTokenCnt > 0) {
            return getInActive(command.userId(), token); // 활성화 정보 반환
        }
        return getInWaiting(command.userId(), token); // 대기열 정보 반환
    }

    private WaitingQueue getInActive(Long userId, String token) {
        // 1. 활성 유저열에 추가
        waitingQueueRepository.saveActiveQueue(userId, token);
        // 2. ttl 설정
        waitingQueueRepository.setTimeout(token, AUTO_EXPIRED_TIME, TimeUnit.MILLISECONDS);
        // 3. 대기열에서 토큰 정보 제거
        waitingQueueRepository.deleteWaitingQueue(userId, token);
        // 4. 활성화 정보 반환
        return WaitingQueue.builder()
                .token(token)
                .userId(userId)
                .status(ACTIVE)
                .build();
    }

    private WaitingQueue getInWaiting(Long userId, String token) {
        Long myWaitingNum = waitingQueueRepository.getMyWaitingNum(userId, token);
        if (myWaitingNum == null) { // 대기순번이 없다면 대기열에 없는 유저
            // 대기열에 추가
            waitingQueueRepository.saveWaitingQueue(userId, token);
            // 내 대기순번 반환
            myWaitingNum = waitingQueueRepository.getMyWaitingNum(userId, token);
        }
        // 대기 잔여 시간 계산 (10초당 활성 전환 수)
        long waitTimeInSeconds = (long) Math.ceil((double) (myWaitingNum - 1) / ENTER_10_SECONDS) * 10;

        return WaitingQueue.builder()
                .token(token)
                .userId(userId)
                .status(WAIT)
                .waitingNum(myWaitingNum)
                .waitTimeInSeconds(waitTimeInSeconds)
                .build();
    }

    /**
     * N초당 M 명씩 active token 으로 전환한다.
     */
    public void activeTokens() {
        // 대기열에서 순서대로 정해진 유저만큼 가져오기
        Set<String> waitingTokens = waitingQueueRepository.getWaitingTokens();
        // 대기열에서 가져온만큼 삭제
        waitingQueueRepository.deleteWaitingTokens();
        // 활성화 열로 유저 변경
        waitingQueueRepository.saveActiveQueues(waitingTokens);
    }

    /**
     * 강제로 active token 을 만료시킨다.
     *
     * @param command token 정보
     */
    @Transactional
    public void forceExpireToken(WaitingQueueCommand.Expire command) {
        try {
            waitingQueueRepository.deleteExpiredToken(command.token());
            // 최종 결제 완료를 위한 이벤트 발행
//            publisher.publishEvent(new PaymentEvent(this, command.reservationId(), command.userId(), command.paymentId(),
//                    command.token(), command.amount(), PaymentEvent.EventConstants.TOKEN_EXPIRED));
            publisher.publishEvent(new PaymentEvent(this, command.paymentId(), PaymentEvent.EventConstants.TOKEN_EXPIRED));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
