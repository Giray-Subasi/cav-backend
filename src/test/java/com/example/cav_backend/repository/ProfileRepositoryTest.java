package com.example.cav_backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;

@DataJpaTest
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void shouldSaveAndFindProfileByIccid() {

        EsimProfile profile = new EsimProfile(
                "EID500",
                "899500",
                OperatorType.VODAFONE
        );

        profileRepository.save(profile);

        Optional<EsimProfile> result =
                profileRepository.findByIccid("899500");

        assertTrue(result.isPresent());

        assertEquals(
                "EID500",
                result.get().getEid()
        );

        assertEquals(
                "899500",
                result.get().getIccid()
        );

        assertEquals(
                OperatorType.VODAFONE,
                result.get().getOperator()
        );
    }

    @Test
    void shouldCheckIfIccidExists() {

        EsimProfile profile = new EsimProfile(
                "EID600",
                "899600",
                OperatorType.TURKCELL
        );

        profileRepository.save(profile);

        boolean exists =
                profileRepository.existsByIccid("899600");

        boolean doesNotExist =
                profileRepository.existsByIccid("999999");

        assertTrue(exists);
        assertFalse(doesNotExist);
    }

    @Test
    void shouldRejectDuplicateIccid() {

        EsimProfile firstProfile = new EsimProfile(
                "EID700",
                "899700",
                OperatorType.VODAFONE
        );

        EsimProfile secondProfile = new EsimProfile(
                "EID701",
                "899700",
                OperatorType.TURKCELL
        );

        profileRepository.saveAndFlush(firstProfile);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> profileRepository.saveAndFlush(secondProfile)
        );
    }
}