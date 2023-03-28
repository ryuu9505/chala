package com.alaha.chala.dto;

import com.alaha.chala.domain.Tag;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record TagWithPostsDto(
        Long id,
        Set<PostDto> posts,
        String tagName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {

    public static TagWithPostsDto of(Set<PostDto> posts, String tagName) {
        return new TagWithPostsDto(null, posts, tagName, null, null, null, null);
    }

    public static TagWithPostsDto of(Long id, Set<PostDto> posts, String tagName, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new TagWithPostsDto(id, posts, tagName, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static TagWithPostsDto from(Tag entity) {
        return new TagWithPostsDto(
                entity.getId(),
                entity.getPosts().stream()
                        .map(PostDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                ,
                entity.getTagName(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getModifiedAt(),
                entity.getModifiedBy()
        );
    }

    public Tag toEntity() {
        return Tag.of(tagName);
    }

}
