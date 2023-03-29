package com.alaha.chala.dto.response;

import com.alaha.chala.dto.PostDto;
import com.alaha.chala.dto.TagDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record PostResponse(
        Long id,
        String title,
        String content,
        Set<String> tags,
        LocalDateTime createdAt,
        String email,
        String nickname
) {

    public static PostResponse of(Long id, String title, String content, Set<String> tags, LocalDateTime createdAt, String email, String nickname) {
        return new PostResponse(id, title, content, tags, createdAt, email, nickname);
    }

    public static PostResponse from(PostDto dto) {
        String nickname = dto.userAccountDto().nickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = dto.userAccountDto().username();
        }

        return new PostResponse(
                dto.id(),
                dto.title(),
                dto.content(),
                dto.tagDtos().stream()
                        .map(TagDto::tagName)
                        .collect(Collectors.toUnmodifiableSet())
                ,
                dto.createdAt(),
                dto.userAccountDto().email(),
                nickname
        );
    }

}
