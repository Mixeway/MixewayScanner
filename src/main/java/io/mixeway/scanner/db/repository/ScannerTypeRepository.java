package io.mixeway.scanner.db.repository;

import io.mixeway.scanner.db.entity.ScannerTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScannerTypeRepository extends JpaRepository<ScannerTypeEntity, Long> {
}
