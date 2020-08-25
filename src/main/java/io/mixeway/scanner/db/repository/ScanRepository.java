package io.mixeway.scanner.db.repository;

import io.mixeway.scanner.db.entity.ScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanRepository extends JpaRepository<ScanEntity, Long> {
}
