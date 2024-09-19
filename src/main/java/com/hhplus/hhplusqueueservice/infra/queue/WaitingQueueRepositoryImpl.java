package com.hhplus.hhplusqueueservice.infra.queue;

import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue;
import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueRepository;
import com.hhplus.hhplusqueueservice.infra.redis.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hhplus.hhplusqueueservice.domain.queue.WaitingQueueConstants.*;

@Repository
@RequiredArgsConstructor
public class WaitingQueueRepositoryImpl implements WaitingQueueRepository {

    private final WaitingQueueJpaRepository waitingQueueJpaRepository;
    private final RedisRepository redisRepository;

    @Override
    public Optional<WaitingQueue> saveQueue(WaitingQueue queue) {
        WaitingQueueEntity queueEntity = waitingQueueJpaRepository.save(WaitingQueueEntity.from(queue));

        return Optional.of(queueEntity.toDomain());
    }

    @Override
    public long getActiveCnt() {
        return redisRepository.countActiveTokens();
    }

    @Override
    public void saveActiveQueue(Long userId, String token) {
        redisRepository.setAdd(ACTIVE_KEY + ":" + token, String.valueOf(userId));
    }

    @Override
    public void setTimeout(String key, long timeout, TimeUnit unit) {
        redisRepository.setTtl(ACTIVE_KEY + ":" + key, timeout, unit);
    }

    @Override
    public void deleteWaitingQueue(Long userId, String token) {
        redisRepository.zSetRemove(WAIT_KEY, token + ":" + userId);
    }

    @Override
    public Long getMyWaitingNum(Long userId, String token) {
        return redisRepository.zSetRank(WAIT_KEY, token + ":" + userId);
    }

    @Override
    public void saveWaitingQueue(Long userId, String token) {
        redisRepository.zSetAdd(WAIT_KEY, token + ":" + userId, System.currentTimeMillis());
    }

    @Override
    public Set<String> getWaitingTokens() {
        return redisRepository.zSetGetRange(WAIT_KEY, 0, ENTER_10_SECONDS - 1);
    }

    @Override
    public void deleteWaitingTokens() {
        redisRepository.zSetRemoveRange(WAIT_KEY, 0, ENTER_10_SECONDS - 1);
    }

    @Override
    public void saveActiveQueues(Set<String> tokens) {
        redisRepository.setAddRangeWithTtl(ACTIVE_KEY, tokens, AUTO_EXPIRED_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public void deleteExpiredToken(String token) {
        redisRepository.deleteKey(ACTIVE_KEY + ":" + token);
    }

}
