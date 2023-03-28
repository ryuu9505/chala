package com.alaha.chala.dto.response;

import com.alaha.chala.dto.CommentDto;
import com.alaha.chala.dto.PostWithCommentsDto;
import com.alaha.chala.dto.TagDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public record PostWithCommentsResponse(
        Long id,
        String title,
        String content,
        Set<String> tags,
        LocalDateTime createdAt,
        String email,
        String nickname,
        String username,
        Set<CommentResponse> commentsResponse
) {

    public static PostWithCommentsResponse of(Long id, String title, String content, Set<String> tags, LocalDateTime createdAt, String email, String nickname, String username, Set<CommentResponse> commentResponses) {
        return new PostWithCommentsResponse(id, title, content, tags, createdAt, email, nickname, username, commentResponses);
    }

    public static PostWithCommentsResponse from(PostWithCommentsDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().username();
        }

        return new PostWithCommentsResponse(
                dto.id(),
                dto.title(),
                dto.content(),
                dto.tagDtos().stream()
                        .map(TagDto::tagName)
                        .collect(Collectors.toUnmodifiableSet())
                ,
                dto.createdAt(),
                dto.userAccountDto().email(),
                nickname,
                dto.userAccountDto().username(),
                organizeChildComments(dto.commentDtos())
        );
    }


    private static Set<CommentResponse> organizeChildComments(Set<CommentDto> dtos) {
        Map<Long, CommentResponse> map = dtos.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toMap(CommentResponse::id, Function.identity()));

        map.values().stream()
                .filter(CommentResponse::hasParentComment)
                .forEach(comment -> {
                    CommentResponse parentComment = map.get(comment.parentCommentId());
                    parentComment.childComments().add(comment);
                });

        return map.values().stream()
                .filter(comment -> !comment.hasParentComment())
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator
                                .comparing(CommentResponse::createdAt)
                                .reversed()
                                .thenComparingLong(CommentResponse::id)
                        )
                ));
    }
}
