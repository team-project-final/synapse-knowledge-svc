package com.synapse.knowledge.note.kafka.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteEventOutboxRepository extends JpaRepository<NoteEventOutbox, Long> {

    List<NoteEventOutbox> findByStatusOrderByIdAsc(NoteEventOutboxStatus status, Pageable pageable);
}
