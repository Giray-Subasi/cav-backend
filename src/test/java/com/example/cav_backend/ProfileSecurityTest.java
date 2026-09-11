package com.example.cav_backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
                "spring.datasource.url=jdbc:h2:mem:cav_security_test;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
public class ProfileSecurityTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void shouldReturn401WhenNoTokenIsProvided() throws Exception {

                mockMvc.perform(get("/profiles"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                                .andExpect(jsonPath("$.message")
                                                .value("Authentication is required"));
        }

        @Test
        void shouldAllowUserToGetProfiles() throws Exception {

                mockMvc.perform(
                                get("/profiles")
                                                .with(jwt().authorities(
                                                                new SimpleGrantedAuthority("ROLE_USER"))))
                                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn403WhenUserTriesToCreateProfile() throws Exception {

                String requestBody = """
                                {
                                  "eid": "EID_SECURITY_USER",
                                  "iccid": "899800",
                                  "operator": "TURKCELL"
                                }
                                """;

                mockMvc.perform(
                                post("/profiles")
                                                .with(jwt().authorities(
                                                                new SimpleGrantedAuthority("ROLE_USER")))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value(403))
                                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "You do not have permission to access this resource"));
        }

        @Test
        void shouldAllowAdminToCreateProfile() throws Exception {

                String requestBody = """
                                {
                                  "eid": "EID_SECURITY_ADMIN",
                                  "iccid": "899801",
                                  "operator": "TURKCELL"
                                }
                                """;

                mockMvc.perform(
                                post("/profiles")
                                                .with(jwt().authorities(
                                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isCreated());
        }

        @Test
        void shouldReturn403WhenUserTriesToUpdateProfile() throws Exception {

                String requestBody = """
                                {
                                  "eid": "UPDATED-EID",
                                  "operator": "VODAFONE"
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899802")
                                                .with(jwt().authorities(
                                                                new SimpleGrantedAuthority("ROLE_USER")))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value(403))
                                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "You do not have permission to access this resource"));
        }

        @Test
        void shouldAllowAdminToUpdateProfile() throws Exception {

                String createRequestBody = """
                                {
                                  "eid": "EID_BEFORE_UPDATE",
                                  "iccid": "899802",
                                  "operator": "VODAFONE"
                                }
                                """;

                mockMvc.perform(
                                post("/profiles")
                                                .with(jwt().authorities(
                                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(createRequestBody))
                                .andExpect(status().isCreated());

                String updateRequestBody = """
                                {
                                  "eid": "EID_AFTER_UPDATE",
                                  "operator": "TURKCELL"
                                }
                                """;

                mockMvc.perform(
                                patch("/profiles/899802")
                                                .with(jwt().authorities(
                                                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(updateRequestBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.eid")
                                                .value("EID_AFTER_UPDATE"))
                                .andExpect(jsonPath("$.iccid")
                                                .value("899802"))
                                .andExpect(jsonPath("$.operator")
                                                .value("TURKCELL"))
                                .andExpect(jsonPath("$.status")
                                                .value("CREATED"));
        }

        @Test
        void shouldAllowCorsPreflightFromFrontend() throws Exception {

                mockMvc.perform(
                                options("/profiles")
                                                .header(
                                                                "Origin",
                                                                "http://localhost:5173")
                                                .header(
                                                                "Access-Control-Request-Method",
                                                                "GET")
                                                .header(
                                                                "Access-Control-Request-Headers",
                                                                "Authorization,Content-Type"))
                                .andExpect(status().isOk())
                                .andExpect(
                                                header().string(
                                                                "Access-Control-Allow-Origin",
                                                                "http://localhost:5173"))
                                .andExpect(
                                                header().string(
                                                                "Access-Control-Allow-Credentials",
                                                                "true"));
        }
}