package com.example.cav_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.model.ProfileStatus;

public interface ProfileRepository
        extends JpaRepository<EsimProfile, Long> {

    Optional<EsimProfile> findByIccid(String iccid);

    boolean existsByIccid(String iccid);

    Page<EsimProfile> findByStatus(
            ProfileStatus status,
            Pageable pageable
    );

    Page<EsimProfile> findByOperator(
            OperatorType operator,
            Pageable pageable
    );

    Page<EsimProfile> findByStatusAndOperator(
            ProfileStatus status,
            OperatorType operator,
            Pageable pageable
    );
}