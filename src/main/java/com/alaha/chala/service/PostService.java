package com.alaha.chala.service;

import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.Tag;
import com.alaha.chala.domain.UserAccount;
import com.alaha.chala.domain.constant.SearchType;
import com.alaha.chala.dto.PostDto;
import com.alaha.chala.dto.PostWithCommentsDto;
import com.alaha.chala.repository.PostRepository;
import com.alaha.chala.repository.TagRepository;
import com.alaha.chala.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(SearchType searchType, String searchKeyword, Pageable pageable) {
        if (searchKeyword == null || searchKeyword.isBlank()) {
            return postRepository.findAll(pageable).map(PostDto::from);
        }

        return switch (searchType) {
            case TITLE -> postRepository.findByTitleContaining(searchKeyword, pageable).map(PostDto::from);
            case CONTENT -> postRepository.findByContentContaining(searchKeyword, pageable).map(PostDto::from);
            case ID -> postRepository.findByUserAccount_UsernameContaining(searchKeyword, pageable).map(PostDto::from);
            case NICKNAME -> postRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(PostDto::from);
            case TAG -> postRepository.findByTagNames(
                            Arrays.stream(searchKeyword.split(" ")).toList(),
                            pageable
                    )
                    .map(PostDto::from);
        };
    }

    @Transactional(readOnly = true)
    public PostWithCommentsDto getPostWithComments(Long postId) {
        return postRepository.findById(postId)
                .map(PostWithCommentsDto::from)
                .orElseThrow(() -> new EntityNotFoundException("No post, post-id: " + postId));
    }

    @Transactional(readOnly = true)
    public PostDto getPost(Long postId) {
        return postRepository.findById(postId)
                .map(PostDto::from)
                .orElseThrow(() -> new EntityNotFoundException("no post - post-id: " + postId));
    }

    public void savePost(PostDto dto) {
        UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().username());
        Set<Tag> tags = renewTagsFromContent(dto.content());

        Post post = dto.toEntity(userAccount);
        post.addTags(tags);
        postRepository.save(post);
    }

    public void updatePost(Long postId, PostDto dto) {
        try {
            Post post = postRepository.getReferenceById(postId);
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().username());

            if (post.getUserAccount().equals(userAccount)) {
                if (dto.title() != null) { post.setTitle(dto.title()); }
                if (dto.content() != null) { post.setContent(dto.content()); }

                Set<Long> tagIds = post.getTags().stream()
                        .map(Tag::getId)
                        .collect(Collectors.toUnmodifiableSet());
                post.clearTags();
                postRepository.flush();

                tagIds.forEach(tagService::deleteTagWithoutPosts);

                Set<Tag> tags = renewTagsFromContent(dto.content());
                post.addTags(tags);
            }
        } catch (EntityNotFoundException e) {
            log.warn("게시글 업데이트 실패. 게시글을 수정하는데 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
        }
    }

    public void deletePost(long postId, String username) {
        Post post = postRepository.getReferenceById(postId);
        Set<Long> tagIds = post.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toUnmodifiableSet());

        postRepository.deleteByIdAndUserAccount_Username(postId, username);
        postRepository.flush();

        tagIds.forEach(tagService::deleteTagWithoutPosts);
    }

    public long getPostCount() {
        return postRepository.count();
    }

    @Transactional(readOnly = true)
    public Page<PostDto> searchPostsViaTag(String tagName, Pageable pageable) {
        if (tagName == null || tagName.isBlank()) {
            return Page.empty(pageable);
        }

        return postRepository.findByTagNames(List.of(tagName), pageable)
                .map(PostDto::from);
    }

    public List<String> getTags() {
        return tagRepository.findAllTagNames(); // TODO: TagService 로 이동을 고려해보자.
    }


    private Set<Tag> renewTagsFromContent(String content) {
        Set<String> tagNamesInContent = tagService.parseTagNames(content);
        Set<Tag> tags = tagService.findTagsByNames(tagNamesInContent);
        Set<String> existingTagNames = tags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toUnmodifiableSet());

        tagNamesInContent.forEach(newTagName -> {
            if (!existingTagNames.contains(newTagName)) {
                tags.add(Tag.of(newTagName));
            }
        });

        return tags;
    }

}