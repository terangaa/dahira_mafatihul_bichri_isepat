
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
class DashboardHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dashboardEndpointShouldBeAvailable() throws Exception {

        mockMvc.perform(
                get("/api/dashboard")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void historyEndpointShouldReturnList() throws Exception {

        mockMvc.perform(
                get("/api/history")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
