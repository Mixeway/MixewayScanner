package io.mixeway.scanner.db.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@EntityScan
@Table(name = "scan")
@EntityListeners(AuditingEntityListener.class)
public class ScanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scannertype_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ScannerTypeEntity scannerType;
    private boolean running;


}
