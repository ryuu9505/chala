package com.alaha.chala.dto.request;

import com.alaha.chala.dto.PostDto;
import com.alaha.chala.dto.TagDto;
import com.alaha.chala.dto.UserAccountDto;

import java.util.Set;

public record PostRequest(
        String title,
        String content
) {

    public static PostRequest of(String title, String content) {
        return new PostRequest(title, content);
    }

    public PostDto toDto(UserAccountDto userAccountDto) {
        return toDto(userAccountDto, null);
    }

    public PostDto toDto(UserAccountDto userAccountDto, Set<TagDto> tagDtos) {
        return PostDto.of(
                userAccountDto,
                title,
                content,
                tagDtos
        );
    }

}