package com.synapse.knowledge.note.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<Note> findByIdAndDeletedAtIsNull(Long id);
    Optional<Note> findByTenantIdAndTitleAndDeletedAtIsNull(String tenantId, String title);
}
