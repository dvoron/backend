package com.example.backend;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.CreateCommentRequest;
import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.PostDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserFlowTest {

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

    @Test
    void userAccountDeletionFlow() throws Exception {
        // 1. Register User
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("e2e_delete@example.com");
        registerRequest.setUsername("e2edeleteuser");
        registerRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Login User
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLogin("e2e_delete@example.com");
        loginRequest.setPassword("password");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        AuthResponseDto authResponse = objectMapper.readValue(responseContent, AuthResponseDto.class);
        String accessToken = authResponse.getAccessToken();

        // 3. Get User ID
        MvcResult usersResult = mockMvc.perform(get("/api")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        UserDto[] users = objectMapper.readValue(usersResult.getResponse().getContentAsString(), UserDto[].class);
        Long userId = Arrays.stream(users)
                .filter(u -> "e2e_delete@example.com".equals(u.getEmail()))
                .findFirst()
                .orElseThrow()
                .getId();

        // 4. Create Post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("Post to be deleted");
        postRequest.setContent("This post should be deleted when the user is deleted.");

        MvcResult postResult = mockMvc.perform(post("/api/forum/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andReturn();

        PostDto createdPost = objectMapper.readValue(postResult.getResponse().getContentAsString(), PostDto.class);
        Long postId = createdPost.getId();

        // 5. Create Comment
        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setPostId(postId);
        commentRequest.setContent("This comment should be deleted.");

        mockMvc.perform(post("/api/forum/comments")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk());

        // 6. Delete User
        mockMvc.perform(delete("/api/" + userId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 7. Verify posts and comments are gone
        MvcResult allPostsResult = mockMvc.perform(get("/api/forum/posts"))
                .andExpect(status().isOk())
                .andReturn();

        PostDto[] allPosts = objectMapper.readValue(allPostsResult.getResponse().getContentAsString(), PostDto[].class);
        boolean postExists = Arrays.stream(allPosts).anyMatch(p -> p.getId().equals(postId));
        assertFalse(postExists, "Post should be deleted when user is deleted");

        boolean commentExists = Arrays.stream(allPosts)
                .flatMap(p -> p.getComments() != null ? p.getComments().stream() : java.util.stream.Stream.empty())
                .anyMatch(c -> "e2edeleteuser".equals(c.getAuthor()));
        assertFalse(commentExists, "Comments should be deleted when user is deleted");
    }
}
