package com.example.cav_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cav_backend.exception.ProfileNotFoundException;
import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.model.ProfileStatus;
import com.example.cav_backend.repository.ProfileRepository;

@Service
public class ProfileServiceImpl implements ProfileService {

        private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);

        private final ProfileRepository profileRepository;

        public ProfileServiceImpl(ProfileRepository profileRepository) {
                this.profileRepository = profileRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public Page<EsimProfile> getProfiles(
                        ProfileStatus status,
                        OperatorType operator,
                        Pageable pageable) {

                logger.info(
                                "Fetching eSIM profiles. Page: {}, Size: {}, Status: {}, Operator: {}",
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                status,
                                operator);

                if (status != null && operator != null) {

                        return profileRepository
                                        .findByStatusAndOperator(
                                                        status,
                                                        operator,
                                                        pageable);
                }

                if (status != null) {

                        return profileRepository
                                        .findByStatus(
                                                        status,
                                                        pageable);
                }

                if (operator != null) {

                        return profileRepository
                                        .findByOperator(
                                                        operator,
                                                        pageable);
                }

                return profileRepository.findAll(pageable);
        }

        @Override
        @Transactional(readOnly = true)
        public EsimProfile findByIccid(String iccid) {

                logger.debug(
                                "Searching for eSIM profile with ICCID: {}",
                                iccid);

                return profileRepository
                                .findByIccid(iccid)
                                .orElseThrow(() -> {

                                        logger.warn(
                                                        "eSIM profile not found. ICCID: {}",
                                                        iccid);

                                        return new ProfileNotFoundException(
                                                        "Profile not found: " + iccid);
                                });
        }

        @Override
        @Transactional
        public EsimProfile addProfile(EsimProfile profile) {

                logger.info(
                                "Creating eSIM profile with ICCID: {}",
                                profile.getIccid());

                if (profileRepository.existsByIccid(profile.getIccid())) {

                        logger.warn(
                                        "Cannot create profile. ICCID already exists: {}",
                                        profile.getIccid());

                        throw new IllegalArgumentException(
                                        "ICCID already exists: " + profile.getIccid());
                }

                EsimProfile savedProfile = profileRepository.save(profile);

                logger.info(
                                "eSIM profile created successfully. ICCID: {}",
                                savedProfile.getIccid());

                return savedProfile;
        }

        @Override
        @Transactional
        public EsimProfile updateProfile(
                        String iccid,
                        String eid,
                        OperatorType operator) {

                logger.info(
                                "Updating eSIM profile. ICCID: {}",
                                iccid);

                EsimProfile profile = findByIccid(iccid);

                if (eid != null) {
                        profile.setEid(eid);
                }

                if (operator != null) {
                        profile.setOperator(operator);
                }

                EsimProfile savedProfile = profileRepository.save(profile);

                logger.info(
                                "eSIM profile updated successfully. ICCID: {}",
                                iccid);

                return savedProfile;
        }

        @Override
        @Transactional
        public EsimProfile startDownload(String iccid) {

                logger.info(
                                "Starting profile download. ICCID: {}",
                                iccid);

                EsimProfile profile = findByIccid(iccid);

                if (profile.getStatus() != ProfileStatus.CREATED) {

                        logger.warn(
                                        "Download rejected. ICCID: {}, current status: {}",
                                        iccid,
                                        profile.getStatus());

                        throw new IllegalStateException(
                                        "Profile must be CREATED before download.");
                }

                profile.startDownload();

                EsimProfile savedProfile = profileRepository.save(profile);

                logger.info(
                                "Profile status changed to DOWNLOADING. ICCID: {}",
                                iccid);

                return savedProfile;
        }

        @Override
        @Transactional
        public EsimProfile completeDownload(String iccid) {

                logger.info(
                                "Completing profile download. ICCID: {}",
                                iccid);

                EsimProfile profile = findByIccid(iccid);

                if (profile.getStatus() != ProfileStatus.DOWNLOADING) {

                        logger.warn(
                                        "Download completion rejected. ICCID: {}, current status: {}",
                                        iccid,
                                        profile.getStatus());

                        throw new IllegalStateException(
                                        "Profile must be DOWNLOADING.");
                }

                profile.completeDownload();

                EsimProfile savedProfile = profileRepository.save(profile);

                logger.info(
                                "Profile status changed to DOWNLOADED. ICCID: {}",
                                iccid);

                return savedProfile;
        }

        @Override
        @Transactional
        public EsimProfile enableProfile(String iccid) {

                logger.info(
                                "Enabling profile. ICCID: {}",
                                iccid);

                EsimProfile profile = findByIccid(iccid);

                if (profile.getStatus() != ProfileStatus.DOWNLOADED) {

                        logger.warn(
                                        "Profile enable rejected. ICCID: {}, current status: {}",
                                        iccid,
                                        profile.getStatus());

                        throw new IllegalStateException(
                                        "Profile must be DOWNLOADED before enabling.");
                }

                profile.enableProfile();

                EsimProfile savedProfile = profileRepository.save(profile);

                logger.info(
                                "Profile enabled successfully. ICCID: {}",
                                iccid);

                return savedProfile;
        }

        @Override
        @Transactional
        public void deleteProfile(String iccid) {

                logger.info(
                                "Deleting eSIM profile. ICCID: {}",
                                iccid);

                EsimProfile profile = findByIccid(iccid);

                profileRepository.delete(profile);

                logger.info(
                                "eSIM profile deleted successfully. ICCID: {}",
                                iccid);
        }
}