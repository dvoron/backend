package com.example.backend;

import com.example.backend.model.dto.*;
import com.example.backend.model.entity.UserSession;
import com.example.backend.repository.UserSessionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Base64;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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

    @Autowired
    private UserSessionRepository userSessionRepository;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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
                .andExpect(status().isCreated())
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
                .andExpect(status().isCreated())
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
                .andExpect(status().isCreated());

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
        String payload = new String(Base64.getDecoder().decode(accessToken.split("\\.")[1]));
        Long userId = Long.valueOf(objectMapper.readTree(payload).get("sub").asText());

        // 4. Create Post
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("Post to be deleted");
        postRequest.setContent("This post should be deleted when the user is deleted.");

        MvcResult postResult = mockMvc.perform(post("/api/forum/posts")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
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
                .andExpect(status().isCreated());

        // 6. Delete User
        DeleteUserRequestDto deleteRequest = new DeleteUserRequestDto();
        deleteRequest.setPassword("password");

        mockMvc.perform(delete("/api/users/" + userId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk());

        // 7. Verify posts and comments are still there but anonymised
        MvcResult allPostsResult = mockMvc.perform(get("/api/forum/posts"))
                .andExpect(status().isOk())
                .andReturn();

        PostDto[] allPosts = objectMapper.readValue(allPostsResult.getResponse().getContentAsString(), PostDto[].class);
        
        boolean postAnonymised = Arrays.stream(allPosts)
                .filter(p -> p.getId().equals(postId))
                .anyMatch(p -> "[deleted user]".equals(p.getAuthor()));
        assert(postAnonymised) : "Post should be anonymised when user is deleted";

        MvcResult postCommentsResult = mockMvc.perform(get("/api/forum/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andReturn();

        CommentDto[] postComments = objectMapper.readValue(postCommentsResult.getResponse().getContentAsString(), CommentDto[].class);

        boolean commentAnonymised = Arrays.stream(postComments)
                .anyMatch(c -> "[deleted user]".equals(c.getAuthor()));
        assert(commentAnonymised) : "Comments should be anonymised when user is deleted";
    }

    @Test
    void refreshTokenReuseRevokesOtherSessionsFlow() throws Exception {
        // 1. Register User
        RegisterRequestDto registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("e2e_reuse@example.com");
        registerRequest.setUsername("e2ereuseuser");
        registerRequest.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Login User (Session 1)
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setLogin("e2e_reuse@example.com");
        loginRequest.setPassword("password");

        MvcResult loginResult1 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDto authResponse1 = objectMapper.readValue(loginResult1.getResponse().getContentAsString(), AuthResponseDto.class);
        String refreshToken1 = loginResult1.getResponse().getCookie("refreshToken").getValue();

        // 3. Login User Again (Session 2)
        MvcResult loginResult2 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDto authResponse2 = objectMapper.readValue(loginResult2.getResponse().getContentAsString(), AuthResponseDto.class);
        String refreshToken2 = loginResult2.getResponse().getCookie("refreshToken").getValue();
        String accessToken2 = authResponse2.getAccessToken();

        // 4. Rotate Session 1's token (legitimate refresh)
        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken1)))
                .andExpect(status().isOk());

        // Make the revoked session older than 30s to bypass the concurrent refresh grace period
        UserSession s = userSessionRepository.findAll().stream()
                .filter(us -> us.isRevoked() && us.getRevokedAt() != null)
                .findFirst().orElseThrow();
        s.setRevokedAt(Instant.now().minus(35, ChronoUnit.SECONDS));
        userSessionRepository.save(s);

        // 5. Reuse Session 1's old token (simulated theft). This should return 401 and revoke ALL sessions.
        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken1)))
                .andExpect(status().isUnauthorized());

        // 6. Verify Session 2 is revoked (attempting to refresh using Session 2's token should fail)
        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken2)))
                .andExpect(status().isUnauthorized());

        // 7. Verify Session 2's access token is rejected for protected endpoints because its session was revoked
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("Test Post");
        postRequest.setContent("Test Content");

        mockMvc.perform(post("/api/forum/posts")
                .header("Authorization", "Bearer " + accessToken2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenReturns401() throws Exception {
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("Test Post");
        postRequest.setContent("Test Content");

        mockMvc.perform(post("/api/forum/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isUnauthorized());
    }
}
