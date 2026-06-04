package com.synapse.knowledge.note.repository;

import com.synapse.knowledge.note.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<Note> findByIdAndDeletedAtIsNull(Long id);
    Optional<Note> findByTenantIdAndTitleAndDeletedAtIsNull(String tenantId, String title);
    List<Note> findTop1000ByUserIdAndDeletedAtIsNull(Long userId);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE n.userId = :userId AND n.deletedAt IS NULL AND t = :tag")
    Page<Note> findByUserIdAndTagAndDeletedAtIsNull(
        @Param("userId") Long userId,
        @Param("tag") String tag,
        Pageable pageable
    );
}
