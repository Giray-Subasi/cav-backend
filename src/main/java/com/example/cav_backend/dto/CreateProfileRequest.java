package com.example.cav_backend.dto;

import com.example.cav_backend.model.OperatorType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateProfileRequest {

    @NotBlank(message = "EID cannot be empty")
    @Size(max = 64, message = "EID cannot exceed 64 characters")
    private String eid;

    @NotBlank(message = "ICCID cannot be empty")
    @Size(max = 32, message = "ICCID cannot exceed 32 characters")
    private String iccid;

    @NotNull(message = "Operator cannot be null")
    private OperatorType operator;

    public CreateProfileRequest() {
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public OperatorType getOperator() {
        return operator;
    }

    public void setOperator(OperatorType operator) {
        this.operator = operator;
    }
}