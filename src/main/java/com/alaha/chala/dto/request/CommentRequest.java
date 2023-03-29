package com.alaha.chala.dto.request;

import com.alaha.chala.dto.CommentDto;
import com.alaha.chala.dto.UserAccountDto;

public record CommentRequest(
        Long postId,
        Long parentCommentId,
        String content
) {

    public static CommentRequest of(Long postId, String content) {
        return CommentRequest.of(postId, null, content);
    }

    public static CommentRequest of(Long postId, Long parentCommentId, String content) {
        return new CommentRequest(postId, parentCommentId, content);
    }

    public CommentDto toDto(UserAccountDto userAccountDto) {
        return CommentDto.of(
                postId,
                userAccountDto,
                parentCommentId,
                content
        );
    }

}
