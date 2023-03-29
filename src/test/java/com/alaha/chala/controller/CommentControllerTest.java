package com.alaha.chala.controller;

import com.alaha.chala.config.TestSecurityConfig;
import com.alaha.chala.dto.CommentDto;
import com.alaha.chala.dto.request.CommentRequest;
import com.alaha.chala.service.CommentService;
import com.alaha.chala.util.FormDataEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller/Comment")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(CommentController.class)
class CommentControllerTest {

    private final MockMvc mvc;

    private final FormDataEncoder formDataEncoder;

    @MockBean private CommentService commentService;


    CommentControllerTest(
            @Autowired MockMvc mvc,
            @Autowired FormDataEncoder formDataEncoder
    ) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }


    @WithUserDetails(value = "testuser1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("New comment")
    @Test
    void givenCommentInfo_whenRequesting_thenSavesNewComment() throws Exception {
        // Given
        long postId = 1L;
        CommentRequest request = CommentRequest.of(postId, "test comment");
        willDoNothing().given(commentService).saveComment(any(CommentDto.class));

        // When & Then
        mvc.perform(
                        post("/comments/new")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(request))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/posts/" + postId))
                .andExpect(redirectedUrl("/posts/" + postId));
        then(commentService).should().saveComment(any(CommentDto.class));
    }

    @WithUserDetails(value = "testuser1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("Delete comment")
    @Test
    void givenCommentIdToDelete_whenRequesting_thenDeletesComment() throws Exception {
        // Given
        long postId = 1L;
        long commentId = 1L;
        String username = "testuser1";
        willDoNothing().given(commentService).deleteComment(commentId, username);

        // When & Then
        mvc.perform(
                        post("/comments/" + commentId + "/delete")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(Map.of("postId", postId)))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/posts/" + postId))
                .andExpect(redirectedUrl("/posts/" + postId));
        then(commentService).should().deleteComment(commentId, username);
    }

    @WithUserDetails(value = "testuser1", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("New reply comment")
    @Test
    void givenCommentInfoWithParentCommentId_whenRequesting_thenSavesNewChildComment() throws Exception {
        // Given
        long postId = 1L;
        CommentRequest request = CommentRequest.of(postId, 1L, "test comment");
        willDoNothing().given(commentService).saveComment(any(CommentDto.class));

        // When & Then
        mvc.perform(
                        post("/comments/new")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .content(formDataEncoder.encode(request))
                                .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/posts/" + postId))
                .andExpect(redirectedUrl("/posts/" + postId));
        then(commentService).should().saveComment(any(CommentDto.class));
    }

}
