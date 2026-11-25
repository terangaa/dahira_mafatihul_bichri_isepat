
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AuthRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AccountStatus;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @AfterEach
        void cleanUp() {
                userRepository.deleteAll();
        }

        @Test
        void registerEndpointShouldAcceptValidUser() throws Exception {
                AuthRequest request = new AuthRequest();
                request.setEmail("new-member@dahira.test");
                request.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        @Test
        void registerEndpointShouldRejectDuplicateEmail() throws Exception {
                AuthRequest request = new AuthRequest();
                request.setEmail("duplicate@dahira.test");
                request.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        void registerEndpointShouldReturnPendingStatusAndNoToken() throws Exception {
                AuthRequest request = new AuthRequest();
                request.setEmail("pending-member@dahira.test");
                request.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.message").isString());
        }

        @Test
        void loginEndpointShouldRejectPendingAccount() throws Exception {
                AuthRequest registerRequest = new AuthRequest();
                registerRequest.setEmail("pending-login@dahira.test");
                registerRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("pending-login@dahira.test");
                loginRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void loginEndpointShouldRejectRejectedAccount() throws Exception {
                User user = User.builder()
                                .email("rejected@dahira.test")
                                .password(passwordEncoder.encode("Password123!"))
                                .accountStatus(AccountStatus.REJECTED)
                                .build();

                userRepository.save(user);

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("rejected@dahira.test");
                loginRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void loginEndpointShouldReturnJwtToken() throws Exception {
                User user = User.builder()
                                .email("member@dahira.test")
                                .password(passwordEncoder.encode("Password123!"))
                                .accountStatus(AccountStatus.APPROVED)
                                .build();

                userRepository.save(user);

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("member@dahira.test");
                loginRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").isNotEmpty())
                                .andExpect(jsonPath("$.email").value("member@dahira.test"))
                                .andExpect(jsonPath("$.role").value("MEMBER"));
        }

        @Test
        void loginEndpointShouldRejectInvalidPassword() throws Exception {
                User user = User.builder()
                                .email("invalid-password@dahira.test")
                                .password(passwordEncoder.encode("Password123!"))
                                .accountStatus(AccountStatus.APPROVED)
                                .build();

                userRepository.save(user);

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("invalid-password@dahira.test");
                loginRequest.setPassword("WrongPassword123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void initialAdminAccountShouldLoginWithAdminRole() throws Exception {
                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("admin@dahira.test");
                loginRequest.setPassword("Admin123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.role").value("ADMIN"))
                                .andExpect(jsonPath("$.token").isNotEmpty());
        }
}
