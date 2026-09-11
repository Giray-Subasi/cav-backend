package com.example.cav_backend.controller;

import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cav_backend.dto.CreateProfileRequest;
import com.example.cav_backend.dto.PageResponse;
import com.example.cav_backend.dto.ProfileResponse;
import com.example.cav_backend.dto.UpdateProfileRequest;
import com.example.cav_backend.exception.InvalidRequestException;
import com.example.cav_backend.mapper.ProfileMapper;
import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.model.ProfileStatus;
import com.example.cav_backend.service.ProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/profiles")
@Tag(name = "eSIM Profiles", description = "Operations for managing eSIM profiles")
public class ProfileController {

        private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "eid", "iccid", "operator", "status");

        private static final int MAX_PAGE_SIZE = 100;

        private final ProfileService profileService;

        public ProfileController(ProfileService profileService) {
                this.profileService = profileService;
        }

        @GetMapping
        @Operation(summary = "Get profiles", description = "Returns eSIM profiles using pagination, filtering and sorting")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profiles returned successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
        })
        public PageResponse<ProfileResponse> getProfiles(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) ProfileStatus status,
                        @RequestParam(required = false) OperatorType operator,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "asc") String direction) {

                if (page < 0) {
                        throw new InvalidRequestException(
                                        "Page must be greater than or equal to 0");
                }

                if (size < 1 || size > MAX_PAGE_SIZE) {
                        throw new InvalidRequestException(
                                        "Size must be between 1 and " + MAX_PAGE_SIZE);
                }

                String normalizedSortBy = sortBy.toLowerCase(Locale.ROOT);

                String normalizedDirection = direction.toLowerCase(Locale.ROOT);

                if (!ALLOWED_SORT_FIELDS.contains(normalizedSortBy)) {
                        throw new InvalidRequestException(
                                        "Invalid sort field: " + sortBy);
                }

                if (!normalizedDirection.equals("asc")
                                && !normalizedDirection.equals("desc")) {

                        throw new InvalidRequestException(
                                        "Invalid sort direction: " + direction);
                }

                Sort sort;

                if (normalizedDirection.equals("desc")) {
                        sort = Sort.by(normalizedSortBy).descending();
                } else {
                        sort = Sort.by(normalizedSortBy).ascending();
                }

                Pageable pageable = PageRequest.of(page, size, sort);

                Page<ProfileResponse> profiles = profileService
                                .getProfiles(
                                                status,
                                                operator,
                                                pageable)
                                .map(ProfileMapper::toResponse);

                return PageResponse.from(profiles);
        }

        @GetMapping("/{iccid}")
        @Operation(summary = "Get profile by ICCID", description = "Returns a single eSIM profile using its ICCID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile found"),
                        @ApiResponse(responseCode = "404", description = "Profile not found")
        })
        public ProfileResponse getProfileByIccid(
                        @PathVariable String iccid) {

                EsimProfile profile = profileService.findByIccid(iccid);

                return ProfileMapper.toResponse(profile);
        }

        @PostMapping
        @Operation(summary = "Create profile", description = "Creates a new eSIM profile")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Profile created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "409", description = "ICCID already exists")
        })
        public ResponseEntity<ProfileResponse> createProfile(
                        @Valid @RequestBody CreateProfileRequest request) {

                EsimProfile profile = new EsimProfile(
                                request.getEid(),
                                request.getIccid(),
                                request.getOperator());

                EsimProfile createdProfile = profileService.addProfile(profile);

                ProfileResponse response = ProfileMapper.toResponse(createdProfile);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @PatchMapping("/{iccid}")
        @Operation(summary = "Update profile", description = "Updates the EID and/or operator of an existing eSIM profile")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "404", description = "Profile not found")
        })
        public ProfileResponse updateProfile(
                        @PathVariable String iccid,
                        @Valid @RequestBody UpdateProfileRequest request) {

                if (request.getEid() == null
                                && request.getOperator() == null) {

                        throw new InvalidRequestException(
                                        "At least one field must be provided");
                }

                EsimProfile updatedProfile = profileService.updateProfile(
                                iccid,
                                request.getEid(),
                                request.getOperator());

                return ProfileMapper.toResponse(updatedProfile);
        }

        @PostMapping("/{iccid}/download")
        @Operation(summary = "Start profile download", description = "Changes profile status from CREATED to DOWNLOADING")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Download started"),
                        @ApiResponse(responseCode = "404", description = "Profile not found"),
                        @ApiResponse(responseCode = "409", description = "Invalid profile state")
        })
        public ProfileResponse startDownload(
                        @PathVariable String iccid) {

                EsimProfile profile = profileService.startDownload(iccid);

                return ProfileMapper.toResponse(profile);
        }

        @PostMapping("/{iccid}/complete")
        @Operation(summary = "Complete profile download", description = "Changes profile status from DOWNLOADING to DOWNLOADED")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Download completed"),
                        @ApiResponse(responseCode = "404", description = "Profile not found"),
                        @ApiResponse(responseCode = "409", description = "Invalid profile state")
        })
        public ProfileResponse completeDownload(
                        @PathVariable String iccid) {

                EsimProfile profile = profileService.completeDownload(iccid);

                return ProfileMapper.toResponse(profile);
        }

        @PostMapping("/{iccid}/enable")
        @Operation(summary = "Enable profile", description = "Changes profile status from DOWNLOADED to ENABLED")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile enabled"),
                        @ApiResponse(responseCode = "404", description = "Profile not found"),
                        @ApiResponse(responseCode = "409", description = "Invalid profile state")
        })
        public ProfileResponse enableProfile(
                        @PathVariable String iccid) {

                EsimProfile profile = profileService.enableProfile(iccid);

                return ProfileMapper.toResponse(profile);
        }

        @DeleteMapping("/{iccid}")
        @Operation(summary = "Delete profile", description = "Deletes an eSIM profile using its ICCID")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Profile not found")
        })
        public ResponseEntity<Void> deleteProfile(
                        @PathVariable String iccid) {

                profileService.deleteProfile(iccid);

                return ResponseEntity
                                .noContent()
                                .build();
        }
}