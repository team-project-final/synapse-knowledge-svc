package com.synapse.knowledge.note.kafka.outbox;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteEventOutboxRepository extends JpaRepository<NoteEventOutbox, Long> {

    @Query(
        value = """
            select *
            from note_event_outbox
            where status = 'PENDING'
               or (status = 'IN_PROGRESS' and claim_expires_at < :now)
            order by id asc
            limit :batchSize
            for update skip locked
            """,
        nativeQuery = true
    )
    List<NoteEventOutbox> findClaimCandidates(@Param("now") LocalDateTime now, @Param("batchSize") int batchSize);
}
