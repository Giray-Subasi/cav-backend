package com.example.cav_backend.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cav_backend.exception.GlobalExceptionHandler;
import com.example.cav_backend.exception.ProfileNotFoundException;
import com.example.cav_backend.model.EsimProfile;
import com.example.cav_backend.model.OperatorType;
import com.example.cav_backend.service.ProfileService;

@ExtendWith(MockitoExtension.class)
public class ProfileControllerTest {

        @Mock
        private ProfileService profileService;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {

                ProfileController profileController = new ProfileController(profileService);

                mockMvc = MockMvcBuilders
                                .standaloneSetup(profileController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @Test
        void shouldGetProfileByIccid() throws Exception {

                EsimProfile profile = new EsimProfile(
                                "EID001",
                                "899001",
                                OperatorType.VODAFONE);

                when(profileService.findByIccid("899001"))
                                .thenReturn(profile);

                mockMvc.perform(
                                get("/profiles/899001"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.eid").value("EID001"))
                                .andExpect(jsonPath("$.iccid").value("899001"))
                                .andExpect(jsonPath("$.operator").value("VODAFONE"))
                                .andExpect(jsonPath("$.status").value("CREATED"));
        }

        @Test
        void shouldReturn404WhenProfileNotFound() throws Exception {

                when(profileService.findByIccid("999999"))
                                .thenThrow(
                                                new ProfileNotFoundException(
                                                                "Profile not found: 999999"));

                mockMvc.perform(
                                get("/profiles/999999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("PROFILE_NOT_FOUND"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Profile not found: 999999"));
        }

        @Test
        void shouldCreateProfileAndReturn201() throws Exception {

                when(profileService.addProfile(any(EsimProfile.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                String requestBody = """
                                {
                                  "eid": "EID100",
                                  "iccid": "899100",
                                  "operator": "VODAFONE"
                                }
                                """;

                mockMvc.perform(
                                post("/profiles")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.eid").value("EID100"))
                                .andExpect(jsonPath("$.iccid").value("899100"))
                                .andExpect(jsonPath("$.operator").value("VODAFONE"))
                                .andExpect(jsonPath("$.status").value("CREATED"));
        }

        @Test
        void shouldReturn400WhenRequestIsInvalid() throws Exception {

                String invalidRequestBody = """
                                {
                                  "eid": "",
                                  "iccid": "",
                                  "operator": null
                                }
                                """;

                mockMvc.perform(
                                post("/profiles")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(invalidRequestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void shouldUpdateProfile() throws Exception {

                EsimProfile updatedProfile = new EsimProfile(
                                "UPDATED-EID",
                                "899001",
                                OperatorType.TURKCELL);

                when(profileService.updateProfile(
                                "899001",
                                "UPDATED-EID",
                                OperatorType.TURKCELL)).thenReturn(updatedProfile);

                String requestBody = """
                                {
                                  "eid": "UPDATED-EID",
                                  "operator": "TURKCELL"
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899001")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.eid").value("UPDATED-EID"))
                                .andExpect(jsonPath("$.iccid").value("899001"))
                                .andExpect(jsonPath("$.operator").value("TURKCELL"))
                                .andExpect(jsonPath("$.status").value("CREATED"));

                verify(profileService).updateProfile(
                                "899001",
                                "UPDATED-EID",
                                OperatorType.TURKCELL);
        }

        @Test
        void shouldReturn400WhenUpdateEidIsInvalid() throws Exception {

                String requestBody = """
                                {
                                  "eid": "",
                                  "operator": "TURKCELL"
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899001")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.message").exists());

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenUpdateEidIsBlank() throws Exception {

                String requestBody = """
                                {
                                  "eid": "   "
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899001")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("eid: EID must not be blank"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenUpdateRequestIsEmpty() throws Exception {

                String requestBody = """
                                {
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899001")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("At least one field must be provided"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenStatusEnumIsInvalid() throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .param("status", "ABC"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value(
                                                                                "Invalid value 'ABC' for parameter 'status'"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenOperatorEnumIsInvalidInRequestBody()
                        throws Exception {

                String requestBody = """
                                {
                                  "operator": "ABC"
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899001")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value(
                                                                                "Invalid value 'ABC' for field 'operator'"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldDeleteProfileAndReturn204() throws Exception {

                mockMvc.perform(
                                delete("/profiles/899001"))
                                .andExpect(status().isNoContent());

                verify(profileService).deleteProfile("899001");
        }

        @Test
        void shouldSortProfilesByIdAscending() throws Exception {

                when(profileService.getProfiles(
                                isNull(),
                                isNull(),
                                any(Pageable.class))).thenReturn(
                                                new PageImpl<>(List.of()));

                mockMvc.perform(
                                get("/profiles")
                                                .param("page", "0")
                                                .param("size", "10")
                                                .param("sortBy", "id")
                                                .param("direction", "asc"))
                                .andExpect(status().isOk());

                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

                verify(profileService).getProfiles(
                                isNull(),
                                isNull(),
                                pageableCaptor.capture());

                Pageable pageable = pageableCaptor.getValue();

                assertEquals(0, pageable.getPageNumber());
                assertEquals(10, pageable.getPageSize());

                Sort.Order order = pageable.getSort().getOrderFor("id");

                assertNotNull(order);

                assertEquals(
                                Sort.Direction.ASC,
                                order.getDirection());
        }

        @Test
        void shouldSortProfilesByIdDescending() throws Exception {

                when(profileService.getProfiles(
                                isNull(),
                                isNull(),
                                any(Pageable.class))).thenReturn(
                                                new PageImpl<>(List.of()));

                mockMvc.perform(
                                get("/profiles")
                                                .param("page", "0")
                                                .param("size", "10")
                                                .param("sortBy", "id")
                                                .param("direction", "desc"))
                                .andExpect(status().isOk());

                ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

                verify(profileService).getProfiles(
                                isNull(),
                                isNull(),
                                pageableCaptor.capture());

                Pageable pageable = pageableCaptor.getValue();

                Sort.Order order = pageable.getSort().getOrderFor("id");

                assertNotNull(order);

                assertEquals(
                                Sort.Direction.DESC,
                                order.getDirection());
        }

        @Test
        void shouldReturn400WhenSortFieldIsInvalid() throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .param("sortBy", "banana")
                                                .param("direction", "asc"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Invalid sort field: banana"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenSortDirectionIsInvalid() throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .param("sortBy", "id")
                                                .param("direction", "test"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Invalid sort direction: test"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenPageIsNegative() throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .param("page", "-1")
                                                .param("size", "10"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value(
                                                                                "Page must be greater than or equal to 0"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenSizeIsZero() throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .param("page", "0")
                                                .param("size", "0"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Size must be between 1 and 100"));

                verifyNoInteractions(profileService);
        }

        @Test
        void shouldReturn400WhenSizeIsGreaterThanMaximum()
                        throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .param("page", "0")
                                                .param("size", "101"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Size must be between 1 and 100"));

                verifyNoInteractions(profileService);
        }
}