package com.example.cav_backend.dto;

import com.example.cav_backend.model.OperatorType;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(min = 1, max = 64, message = "EID must be between 1 and 64 characters")
    @Pattern(regexp = ".*\\S.*", message = "EID must not be blank")
    private String eid;

    private OperatorType operator;

    public UpdateProfileRequest() {
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public OperatorType getOperator() {
        return operator;
    }

    public void setOperator(OperatorType operator) {
        this.operator = operator;
    }
}