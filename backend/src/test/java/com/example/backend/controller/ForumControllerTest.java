package com.example.backend.controller;

import com.example.backend.model.dto.CreatePostRequest;
import com.example.backend.model.dto.PostDto;
import com.example.backend.service.ForumService;
import com.example.backend.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.example.backend.config.SecurityConfig;
import com.example.backend.config.JwtAuthFilter;

@WebMvcTest(ForumController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class ForumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ForumService forumService;

    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getAllPostsReturnsOkAndList() throws Exception {
        PostDto postDto = new PostDto();
        postDto.setId(1L);
        postDto.setTitle("Test Post");
        postDto.setContent("Test Content");

        when(forumService.getAllPosts()).thenReturn(List.of(postDto));

        mockMvc.perform(get("/api/forum/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Post"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));
    }

    @Test
    void createPostWithUserReturnsOkAndPost() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");

        PostDto postDto = new PostDto();
        postDto.setId(1L);
        postDto.setTitle("New Post");
        postDto.setContent("New Content");

        when(forumService.createPost(any(CreatePostRequest.class), eq(1L))).thenReturn(postDto);

        mockMvc.perform(post("/api/forum/posts")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Post"))
                .andExpect(jsonPath("$.content").value("New Content"));
    }
}
