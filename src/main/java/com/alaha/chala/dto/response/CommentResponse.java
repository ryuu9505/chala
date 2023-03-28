package com.alaha.chala.dto.response;

import com.alaha.chala.dto.CommentDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public record CommentResponse(
        Long id,
        String content,
        LocalDateTime createdAt,
        String email,
        String nickname,
        String username,
        Long parentCommentId,
        Set<CommentResponse> childComments
) {

    public static CommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String username) {
        return CommentResponse.of(id, content, createdAt, email, nickname, username, null);
    }

    public static CommentResponse of(Long id, String content, LocalDateTime createdAt, String email, String nickname, String username, Long parentCommentId) {
        Comparator<CommentResponse> childCommentComparator = Comparator
                .comparing(CommentResponse::createdAt)
                .thenComparingLong(CommentResponse::id);
        return new CommentResponse(id, content, createdAt, email, nickname, username, parentCommentId, new TreeSet<>(childCommentComparator));
    }

    public static CommentResponse from(CommentDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().username();
        }

        return CommentResponse.of(
                dto.id(),
                dto.content(),
                dto.createdAt(),
                dto.userAccountDto().email(),
                nickname,
                dto.userAccountDto().username(),
                dto.parentCommentId()
        );
    }

    public boolean hasParentComment() {
        return parentCommentId != null;
    }

}
