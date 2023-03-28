package com.alaha.chala.controller;

import com.alaha.chala.dto.request.CommentRequest;
import com.alaha.chala.dto.security.BoardPrincipal;
import com.alaha.chala.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/comments")
@Controller
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/new")
    public String postNewComment(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            CommentRequest commentRequest
    ) {
        commentService.saveComment(commentRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/posts/" + commentRequest.postId();
    }

    @PostMapping("/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            Long postId
    ) {
        commentService.deleteComment(commentId, boardPrincipal.getUsername());

        return "redirect:/posts/" + postId;
    }

}
