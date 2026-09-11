package com.example.cav_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.model.ProfileStatus;

public interface ProfileService {

    Page<EsimProfile> getProfiles(
            ProfileStatus status,
            OperatorType operator,
            Pageable pageable);

    EsimProfile findByIccid(String iccid);

    EsimProfile addProfile(EsimProfile profile);

    EsimProfile updateProfile(
            String iccid,
            String eid,
            OperatorType operator);

    EsimProfile startDownload(String iccid);

    EsimProfile completeDownload(String iccid);

    EsimProfile enableProfile(String iccid);

    void deleteProfile(String iccid);
}