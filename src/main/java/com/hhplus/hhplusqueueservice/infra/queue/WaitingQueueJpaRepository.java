package com.hhplus.hhplusqueueservice.infra.queue;

import com.hhplus.hhplusqueueservice.domain.queue.WaitingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitingQueueJpaRepository extends JpaRepository<WaitingQueueEntity, Long> {
    long countByStatusIs(WaitingQueue.WaitingQueueStatus active);

    List<WaitingQueueEntity> findAllByStatusIsOrderByRequestTime(WaitingQueue.WaitingQueueStatus status);

    long countByRequestTimeBeforeAndStatusIs(LocalDateTime requestTime, WaitingQueue.WaitingQueueStatus wait);

    Optional<WaitingQueueEntity> findByToken(String token);

    @Query("SELECT w FROM WaitingQueueEntity w WHERE w.activeTime <= :timeThreshold AND w.status = :status")
    List<WaitingQueueEntity> getActiveOver10Min(@Param("timeThreshold") LocalDateTime timeThreshold,
                                                @Param("status") WaitingQueue.WaitingQueueStatus status);

    void deleteAllInBatchByStatusIs(WaitingQueue.WaitingQueueStatus expired);
}
