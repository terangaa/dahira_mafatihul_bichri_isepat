
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FinancialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void financeEndpointsShouldBeAvailable() throws Exception {

        mockMvc.perform(
                get("/api/events")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/projects")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/monthly-payments")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/contributions")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
