package com.example.cav_backend.mapper;

import com.example.cav_backend.dto.ProfileResponse;
import com.example.cav_backend.model.EsimProfile;

public class ProfileMapper {

    private ProfileMapper() {
    }

    public static ProfileResponse toResponse(
            EsimProfile profile) {

        return new ProfileResponse(
                profile.getId(),
                profile.getEid(),
                profile.getIccid(),
                profile.getOperator(),
                profile.getStatus(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}