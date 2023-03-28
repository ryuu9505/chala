package com.alaha.chala.dto;

import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.UserAccount;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record PostDto(
        Long id,
        UserAccountDto userAccountDto,
        String title,
        String content,
        Set<TagDto> tagDtos,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {

    public static PostDto of(UserAccountDto userAccountDto, String title, String content, Set<TagDto> tagDtos) {
        return new PostDto(null, userAccountDto, title, content, tagDtos, null, null, null, null);
    }

    public static PostDto of(Long id, UserAccountDto userAccountDto, String title, String content, Set<TagDto> tagDtos, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new PostDto(id, userAccountDto, title, content, tagDtos, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static PostDto from(Post entity) {
        return new PostDto(
                entity.getId(),
                UserAccountDto.from(entity.getUserAccount()),
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

    public Post toEntity(UserAccount userAccount) {
        return Post.of(
                userAccount,
                title,
                content
        );
    }

}