package com.alaha.chala.dto;

import com.alaha.chala.domain.Post;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public record PostWithCommentsDto(
        Long id,
        UserAccountDto userAccountDto,
        Set<CommentDto> commentDtos,
        String title,
        String content,
        Set<TagDto> tagDtos,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {
    public static PostWithCommentsDto of(Long id, UserAccountDto userAccountDto, Set<CommentDto> commentDtos, String title, String content, Set<TagDto> tagDtos, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new PostWithCommentsDto(id, userAccountDto, commentDtos, title, content, tagDtos, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static PostWithCommentsDto from(Post entity) {
        return new PostWithCommentsDto(
                entity.getId(),
                UserAccountDto.from(entity.getUserAccount()),
                entity.getComments().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
                ,
                entity.getTitle(),
                entity.getContent(),
                entity.getTags().stream()
                        .map(TagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                ,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

}
