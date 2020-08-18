package io.mixeway.scanner.db.entity;

import io.mixeway.scanner.integrations.scanner.DependencyTrack;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@EntityScan
@Table(name = "dependencytrack")
@EntityListeners(AuditingEntityListener.class)
public class DependencyTrackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean enabled;
    @Column(name = "apikey")
    private String apiKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public DependencyTrackEntity() {}
    public DependencyTrackEntity(String apiKey){
        this.enabled = false;
        this.apiKey =apiKey;
    }
}
