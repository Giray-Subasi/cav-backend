package com.example.cav_backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.cav_backend.exception.ProfileNotFoundException;
import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.model.ProfileStatus;
import com.example.cav_backend.repository.ProfileRepository;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

        @Mock
        private ProfileRepository profileRepository;

        @InjectMocks
        private ProfileServiceImpl profileService;

        @Test
        void shouldFindProfileByIccid() {

                EsimProfile profile = new EsimProfile(
                                "EID001",
                                "899001",
                                OperatorType.VODAFONE);

                when(profileRepository.findByIccid("899001"))
                                .thenReturn(Optional.of(profile));

                EsimProfile result = profileService.findByIccid("899001");

                assertEquals("899001", result.getIccid());
                assertEquals("EID001", result.getEid());
                assertEquals(
                                OperatorType.VODAFONE,
                                result.getOperator());
        }

        @Test
        void shouldThrowExceptionWhenProfileNotFound() {

                when(profileRepository.findByIccid("999999"))
                                .thenReturn(Optional.empty());

                ProfileNotFoundException exception = assertThrows(
                                ProfileNotFoundException.class,
                                () -> profileService.findByIccid("999999"));

                assertEquals(
                                "Profile not found: 999999",
                                exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenIccidAlreadyExists() {

                EsimProfile profile = new EsimProfile(
                                "EID002",
                                "899001",
                                OperatorType.TURKCELL);

                when(profileRepository.existsByIccid("899001"))
                                .thenReturn(true);

                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> profileService.addProfile(profile));

                assertEquals(
                                "ICCID already exists: 899001",
                                exception.getMessage());
        }

        @Test
        void shouldUpdateProfile() {

                EsimProfile profile = new EsimProfile(
                                "OLD-EID",
                                "899010",
                                OperatorType.VODAFONE);

                when(profileRepository.findByIccid("899010"))
                                .thenReturn(Optional.of(profile));

                when(profileRepository.save(profile))
                                .thenReturn(profile);

                EsimProfile result = profileService.updateProfile(
                                "899010",
                                "NEW-EID",
                                OperatorType.TURKCELL);

                assertEquals(
                                "NEW-EID",
                                result.getEid());

                assertEquals(
                                OperatorType.TURKCELL,
                                result.getOperator());

                assertEquals(
                                "899010",
                                result.getIccid());

                assertEquals(
                                ProfileStatus.CREATED,
                                result.getStatus());

                verify(profileRepository)
                                .save(profile);
        }

        @Test
        void shouldUpdateOnlyProvidedFields() {

                EsimProfile profile = new EsimProfile(
                                "ORIGINAL-EID",
                                "899011",
                                OperatorType.VODAFONE);

                when(profileRepository.findByIccid("899011"))
                                .thenReturn(Optional.of(profile));

                when(profileRepository.save(profile))
                                .thenReturn(profile);

                EsimProfile result = profileService.updateProfile(
                                "899011",
                                null,
                                OperatorType.TURKCELL);

                assertEquals(
                                "ORIGINAL-EID",
                                result.getEid());

                assertEquals(
                                OperatorType.TURKCELL,
                                result.getOperator());

                assertEquals(
                                "899011",
                                result.getIccid());

                assertEquals(
                                ProfileStatus.CREATED,
                                result.getStatus());

                verify(profileRepository)
                                .save(profile);
        }

        @Test
        void shouldStartDownload() {

                EsimProfile profile = new EsimProfile(
                                "EID003",
                                "899003",
                                OperatorType.VODAFONE);

                when(profileRepository.findByIccid("899003"))
                                .thenReturn(Optional.of(profile));

                when(profileRepository.save(profile))
                                .thenReturn(profile);

                EsimProfile result = profileService.startDownload("899003");

                assertEquals(
                                ProfileStatus.DOWNLOADING,
                                result.getStatus());
        }

        @Test
        void shouldRejectDownloadWhenProfileIsNotCreated() {

                EsimProfile profile = new EsimProfile(
                                "EID004",
                                "899004",
                                OperatorType.TURKCELL);

                profile.startDownload();

                when(profileRepository.findByIccid("899004"))
                                .thenReturn(Optional.of(profile));

                IllegalStateException exception = assertThrows(
                                IllegalStateException.class,
                                () -> profileService.startDownload("899004"));

                assertEquals(
                                "Profile must be CREATED before download.",
                                exception.getMessage());
        }

        @Test
        void shouldCompleteDownload() {

                EsimProfile profile = new EsimProfile(
                                "EID005",
                                "899005",
                                OperatorType.VODAFONE);

                profile.startDownload();

                when(profileRepository.findByIccid("899005"))
                                .thenReturn(Optional.of(profile));

                when(profileRepository.save(profile))
                                .thenReturn(profile);

                EsimProfile result = profileService.completeDownload("899005");

                assertEquals(
                                ProfileStatus.DOWNLOADED,
                                result.getStatus());
        }

        @Test
        void shouldEnableProfile() {

                EsimProfile profile = new EsimProfile(
                                "EID006",
                                "899006",
                                OperatorType.TURKCELL);

                profile.startDownload();
                profile.completeDownload();

                when(profileRepository.findByIccid("899006"))
                                .thenReturn(Optional.of(profile));

                when(profileRepository.save(profile))
                                .thenReturn(profile);

                EsimProfile result = profileService.enableProfile("899006");

                assertEquals(
                                ProfileStatus.ENABLED,
                                result.getStatus());
        }

        @Test
        void shouldGetAllProfilesWhenNoFiltersProvided() {

                Pageable pageable = PageRequest.of(0, 10);

                EsimProfile profile = new EsimProfile(
                                "EID100",
                                "899100",
                                OperatorType.TURKCELL);

                Page<EsimProfile> page = new PageImpl<>(
                                List.of(profile),
                                pageable,
                                1);

                when(profileRepository.findAll(pageable))
                                .thenReturn(page);

                Page<EsimProfile> result = profileService.getProfiles(
                                null,
                                null,
                                pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(
                                "899100",
                                result.getContent().get(0).getIccid());

                verify(profileRepository)
                                .findAll(pageable);
        }

        @Test
        void shouldFilterProfilesByStatus() {

                Pageable pageable = PageRequest.of(0, 10);

                EsimProfile profile = new EsimProfile(
                                "EID101",
                                "899101",
                                OperatorType.VODAFONE);

                Page<EsimProfile> page = new PageImpl<>(
                                List.of(profile),
                                pageable,
                                1);

                when(profileRepository.findByStatus(
                                ProfileStatus.CREATED,
                                pageable)).thenReturn(page);

                Page<EsimProfile> result = profileService.getProfiles(
                                ProfileStatus.CREATED,
                                null,
                                pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(
                                ProfileStatus.CREATED,
                                result.getContent().get(0).getStatus());

                verify(profileRepository)
                                .findByStatus(
                                                ProfileStatus.CREATED,
                                                pageable);
        }

        @Test
        void shouldFilterProfilesByOperator() {

                Pageable pageable = PageRequest.of(0, 10);

                EsimProfile profile = new EsimProfile(
                                "EID102",
                                "899102",
                                OperatorType.TURKCELL);

                Page<EsimProfile> page = new PageImpl<>(
                                List.of(profile),
                                pageable,
                                1);

                when(profileRepository.findByOperator(
                                OperatorType.TURKCELL,
                                pageable)).thenReturn(page);

                Page<EsimProfile> result = profileService.getProfiles(
                                null,
                                OperatorType.TURKCELL,
                                pageable);

                assertEquals(1, result.getTotalElements());
                assertEquals(
                                OperatorType.TURKCELL,
                                result.getContent().get(0).getOperator());

                verify(profileRepository)
                                .findByOperator(
                                                OperatorType.TURKCELL,
                                                pageable);
        }

        @Test
        void shouldFilterProfilesByStatusAndOperator() {

                Pageable pageable = PageRequest.of(0, 10);

                EsimProfile profile = new EsimProfile(
                                "EID103",
                                "899103",
                                OperatorType.TURKCELL);

                Page<EsimProfile> page = new PageImpl<>(
                                List.of(profile),
                                pageable,
                                1);

                when(profileRepository.findByStatusAndOperator(
                                ProfileStatus.CREATED,
                                OperatorType.TURKCELL,
                                pageable)).thenReturn(page);

                Page<EsimProfile> result = profileService.getProfiles(
                                ProfileStatus.CREATED,
                                OperatorType.TURKCELL,
                                pageable);

                assertEquals(1, result.getTotalElements());

                assertEquals(
                                ProfileStatus.CREATED,
                                result.getContent().get(0).getStatus());

                assertEquals(
                                OperatorType.TURKCELL,
                                result.getContent().get(0).getOperator());

                verify(profileRepository)
                                .findByStatusAndOperator(
                                                ProfileStatus.CREATED,
                                                OperatorType.TURKCELL,
                                                pageable);
        }
}