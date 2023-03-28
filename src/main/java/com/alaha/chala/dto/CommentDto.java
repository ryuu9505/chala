package com.alaha.chala.dto;

import com.alaha.chala.domain.Comment;
import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.UserAccount;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        Long postId,
        UserAccountDto userAccountDto,
        Long parentCommentId,
        String content,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static CommentDto of(Long postId, UserAccountDto userAccountDto, String content) {
        return CommentDto.of(postId, userAccountDto, null, content);
    }

    public static CommentDto of(Long postId, UserAccountDto userAccountDto, Long parentCommentId, String content) {
        return CommentDto.of(null, postId, userAccountDto, parentCommentId, content, null, null, null, null);
    }

    public static CommentDto of(Long id, Long postId, UserAccountDto userAccountDto, Long parentCommentId, String content, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new CommentDto(id, postId, userAccountDto, parentCommentId, content, createdAt, createdBy, modifiedAt, modifiedBy);
    }
    
    public static CommentDto from(Comment entity) {
        return new CommentDto(
                entity.getId(),
                entity.getPost().getId(),
                UserAccountDto.from(entity.getUserAccount()),
                entity.getParentCommentId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

    public Comment toEntity(Post post, UserAccount userAccount) {
        return Comment.of(
                post,
                userAccount,
                content
        );
    }
}