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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cav_integration_test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
public class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAndThenGetProfile() throws Exception {

        String requestBody = """
                {
                  "eid": "EID900",
                  "iccid": "899900",
                  "operator": "VODAFONE"
                }
                """;

        mockMvc.perform(
                post("/profiles")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eid").value("EID900"))
                .andExpect(jsonPath("$.iccid").value("899900"))
                .andExpect(jsonPath("$.operator").value("VODAFONE"))
                .andExpect(jsonPath("$.status").value("CREATED"));

        mockMvc.perform(
                get("/profiles/899900")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eid").value("EID900"))
                .andExpect(jsonPath("$.iccid").value("899900"))
                .andExpect(jsonPath("$.operator").value("VODAFONE"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void shouldReturn409WhenIccidAlreadyExists() throws Exception {

        String firstRequest = """
                {
                  "eid": "EID901",
                  "iccid": "899901",
                  "operator": "VODAFONE"
                }
                """;

        String secondRequest = """
                {
                  "eid": "EID902",
                  "iccid": "899901",
                  "operator": "TURKCELL"
                }
                """;

        mockMvc.perform(
                post("/profiles")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest)
        )
                .andExpect(status().isCreated());

        mockMvc.perform(
                post("/profiles")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequest)
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(
                        jsonPath("$.message")
                                .value("ICCID already exists: 899901")
                );
    }

    @Test
    void shouldCompleteProfileLifecycle() throws Exception {

        String requestBody = """
                {
                  "eid": "EID950",
                  "iccid": "899950",
                  "operator": "TURKCELL"
                }
                """;

        mockMvc.perform(
                post("/profiles")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"));

        mockMvc.perform(
                post("/profiles/899950/download")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWNLOADING"));

        mockMvc.perform(
                post("/profiles/899950/complete")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWNLOADED"));

        mockMvc.perform(
                post("/profiles/899950/enable")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENABLED"));

        mockMvc.perform(
                get("/profiles/899950")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENABLED"));
    }
}