package io.mixeway.scanner.db.repository;

import io.mixeway.scanner.db.entity.DependencyTrackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DependencyTrackRepository extends JpaRepository<DependencyTrackEntity, Long> {
    Optional<DependencyTrackEntity> findByEnabledAndApiKeyNotNull(boolean enabled);
}
