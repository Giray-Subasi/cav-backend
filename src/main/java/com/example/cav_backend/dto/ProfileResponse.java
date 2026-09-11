package com.example.cav_backend.dto;

import java.time.OffsetDateTime;

import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.model.ProfileStatus;

public class ProfileResponse {

    private final Long id;
    private final String eid;
    private final String iccid;
    private final OperatorType operator;
    private final ProfileStatus status;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public ProfileResponse(
            Long id,
            String eid,
            String iccid,
            OperatorType operator,
            ProfileStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {

        this.id = id;
        this.eid = eid;
        this.iccid = iccid;
        this.operator = operator;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
}