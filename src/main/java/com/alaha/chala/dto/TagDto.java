package com.alaha.chala.dto;

import com.alaha.chala.domain.Tag;

import java.time.LocalDateTime;

public record TagDto(
        Long id,
        String tagName,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime modifiedAt,
        String modifiedBy
) {

    public static TagDto of(String tagName) {
        return new TagDto(null, tagName, null, null, null, null);
    }

    public static TagDto of(Long id, String tagName, LocalDateTime createdAt, String createdBy, LocalDateTime modifiedAt, String modifiedBy) {
        return new TagDto(id, tagName, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public static TagDto from(Tag entity) {
        return new TagDto(
                entity.getId(),
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