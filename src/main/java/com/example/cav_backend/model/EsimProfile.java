package com.example.cav_backend.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "esim_profile")
public class EsimProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eid;

    @Column(nullable = false, unique = true, length = 32)
    private String iccid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OperatorType operator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProfileStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public EsimProfile() {
    }

    public EsimProfile(String eid, String iccid, OperatorType operator) {
        this.eid = eid;
        this.iccid = iccid;
        this.operator = operator;
        this.status = ProfileStatus.CREATED;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEid() {
        return eid;
    }

    public String getIccid() {
        return iccid;
    }

    public OperatorType getOperator() {
        return operator;
    }

    public ProfileStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public void setOperator(OperatorType operator) {
        this.operator = operator;
    }

    public void startDownload() {
        status = ProfileStatus.DOWNLOADING;
    }

    public void completeDownload() {
        status = ProfileStatus.DOWNLOADED;
    }

    public void enableProfile() {
        status = ProfileStatus.ENABLED;
    }

    public void failProfile() {
        status = ProfileStatus.FAILED;
    }
}