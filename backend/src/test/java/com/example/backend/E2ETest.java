package com.example.backend;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class E2ETest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void userRegistrationLoginAndPostCreationFlow() throws Exception {
        // 1. Register User
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("e2e@example.com");
        registerRequest.setUsername("e2euser");
        registerRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // 2. Login User
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLogin("e2e@example.com");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        AuthResponseDto authResponse = objectMapper.readValue(responseContent, AuthResponseDto.class);
        String accessToken = authResponse.getAccessToken();

        // 3. Create Post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("E2E Test Post");
        postRequest.setContent("This is an E2E test post content.");

        mockMvc.perform(post("/api/forum/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("E2E Test Post"))
                .andExpect(jsonPath("$.content").value("This is an E2E test post content."));
    }
}
