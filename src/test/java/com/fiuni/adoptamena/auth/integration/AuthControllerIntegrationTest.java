package com.fiuni.adoptamena.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiuni.adoptamena.api.domain.user.UserDomain;
import com.fiuni.adoptamena.auth.LoginRequest;
import com.fiuni.adoptamena.auth.RegisterRequest;
import com.fiuni.adoptamena.api.dao.user.IUserDao;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private IUserDao IUserDao;

        @Test
        void testRegisterAndLoginFlowWithVerifiedAcc() throws Exception {
                String email = "usertest@example.com";
                String password = "password123";
                String role = "USER";
                RegisterRequest registerRequest = new RegisterRequest(email, password, role);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").exists());

                UserDomain createdUser = IUserDao.findByEmail(email).orElseThrow();
                createdUser.setIsVerified(true);
                IUserDao.save(createdUser);

                LoginRequest loginRequest = new LoginRequest(email, password);
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists());
        }

        @Test
        void testRegisterAndLoginFlowWithoutVerifiedAcc() throws Exception {
                String email = "usertest@example.com";
                String password = "password123";
                String role = "USER";
                RegisterRequest registerRequest = new RegisterRequest(email, password, role);

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").exists());

                LoginRequest loginRequest = new LoginRequest(email, password);
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void testRegisterWithInvalidRole() throws Exception {
                RegisterRequest request = new RegisterRequest("valid@example.com", "password123", "Invalid");

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testRegisterWithExistingEmail() throws Exception {
                String email = "existing@example.com";
                String password = "password123";
                String role = "USER";
                RegisterRequest request = new RegisterRequest(email, password, role);

                // Registrar un usuario
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                // Intentar registrar un usuario con el mismo email
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testLoginWithInvalidCredentials() throws Exception {
                LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "wrongpassword");

                // Intentar iniciar sesión con credenciales inválidas
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testRegisterWithInvalidEmail() throws Exception {
                RegisterRequest request = new RegisterRequest("invalidemail", "password123", "USER");

                // Intentar registrar un usuario con email inválido
                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testRegisterWithShortPassword() throws Exception {
                RegisterRequest request = new RegisterRequest("valid@example.com", "123", "USER");

                mockMvc.perform(post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }
}
