package com.alaha.chala.controller;

import com.alaha.chala.config.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SecurityConfig.class)
@WebMvcTest(PostController.class)
class PostControllerTest {

    private final MockMvc mvc;

    public PostControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[View][GET] Post list")
    @Test
    public void givenNothing_whenRequestingPostsView_thenReturnsPostsView() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/index"))
                .andExpect(model().attributeExists("posts"));
    }

    @DisplayName("[View][GET] Post details")
    @Test
    public void givenNothing_whenRequestingPostView_thenReturnsPostView() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("Comments"));
    }

    @Disabled
    @DisplayName("[View][GET] Post searching page")
    @Test
    public void givenNothing_whenRequestingPostSearchView_thenReturnsPostSearchView() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/posts/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("posts/search"));
    }

    @Disabled
    @DisplayName("[View][GET] Post tag searching page")
    @Test
    public void givenNothing_whenRequestingPostHashtagSearchView_thenReturnsPostHashtagSearchView() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/posts/search-tag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(model().attributeExists("posts/search-tag"));
    }

    @DisplayName("[view][GET] Login page")
    @Test
    public void givenNothing_whenTryingToLogIn_thenReturnsLogInView() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
