package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class AccountApprovalSecurityTest {

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
        void pendingAccountLoginShouldBeForbidden() throws Exception {
                AuthRequest registerRequest = new AuthRequest();
                registerRequest.setEmail("pending-account@dahira.test");
                registerRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("pending-account@dahira.test");
                loginRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void rejectedAccountLoginShouldBeForbidden() throws Exception {
                User user = User.builder()
                                .email("rejected-account@dahira.test")
                                .password(passwordEncoder.encode("Password123!"))
                                .accountStatus(AccountStatus.REJECTED)
                                .build();

                userRepository.save(user);

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("rejected-account@dahira.test");
                loginRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void approvedAccountLoginShouldSucceed() throws Exception {
                User user = User.builder()
                                .email("approved-account@dahira.test")
                                .password(passwordEncoder.encode("Password123!"))
                                .accountStatus(AccountStatus.APPROVED)
                                .build();

                userRepository.save(user);

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("approved-account@dahira.test");
                loginRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk());
        }

        @Test
        void pendingAccountWithWrongPasswordShouldBeUnauthorized() throws Exception {
                AuthRequest registerRequest = new AuthRequest();
                registerRequest.setEmail("pending-wrong@dahira.test");
                registerRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("pending-wrong@dahira.test");
                loginRequest.setPassword("WrongPassword123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void approvedAccountWithWrongPasswordShouldBeUnauthorized() throws Exception {
                User user = User.builder()
                                .email("approved-wrong@dahira.test")
                                .password(passwordEncoder.encode("Password123!"))
                                .accountStatus(AccountStatus.APPROVED)
                                .build();

                userRepository.save(user);

                AuthRequest loginRequest = new AuthRequest();
                loginRequest.setEmail("approved-wrong@dahira.test");
                loginRequest.setPassword("WrongPassword123!");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void pendingAccountCannotAccessPrivateEndpoint() throws Exception {
                AuthRequest registerRequest = new AuthRequest();
                registerRequest.setEmail("pending-private@dahira.test");
                registerRequest.setPassword("Password123!");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                // Aucun JWT valide : l'accès privé doit être refusé.
                mockMvc.perform(get("/api/my-profile"))
                                .andExpect(status().isUnauthorized());
        }
}
