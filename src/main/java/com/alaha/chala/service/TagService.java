package com.alaha.chala.service;

import com.alaha.chala.domain.Tag;
import com.alaha.chala.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Transactional
@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Set<Tag> findTagsByNames(Set<String> tagNames) {
        return new HashSet<>(tagRepository.findByTagNameIn(tagNames));
    }

    public Set<String> parseTagNames(String content) {
        if (content == null) {
            return Set.of();
        }

        Pattern pattern = Pattern.compile("#[\\w가-힣]+");
        Matcher matcher = pattern.matcher(content.strip());
        Set<String> result = new HashSet<>();

        while (matcher.find()) {
            result.add(matcher.group().replace("#", ""));
        }

        return Set.copyOf(result);
    }

    public void deleteTagWithoutPosts(Long tagId) {
        Tag tag = tagRepository.getReferenceById(tagId);
        if (tag.getPosts().isEmpty()) {
            tagRepository.delete(tag);
        }
    }

}