package com.alaha.chala.service;

import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.Tag;
import com.alaha.chala.domain.UserAccount;
import com.alaha.chala.domain.constant.SearchType;
import com.alaha.chala.dto.PostDto;
import com.alaha.chala.dto.PostWithCommentsDto;
import com.alaha.chala.dto.TagDto;
import com.alaha.chala.dto.UserAccountDto;
import com.alaha.chala.repository.PostRepository;
import com.alaha.chala.repository.TagRepository;
import com.alaha.chala.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("Service/Post")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks private PostService sut;

    @Mock private TagService tagService;
    @Mock private PostRepository postRepository;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private TagRepository tagRepository;

    @DisplayName("If you search for posts without a search term, it returns the post page.")
    @Test
    void givenNoSearchParameters_whenSearchingPosts_thenReturnsPostPage() {
        // Given
        Pageable pageable = Pageable.ofSize(20);
        given(postRepository.findAll(pageable)).willReturn(Page.empty());

        // When
        Page<PostDto> posts = sut.searchPosts(null, null, pageable);

        // Then
        assertThat(posts).isEmpty();
        then(postRepository).should().findAll(pageable);
    }

    @DisplayName("If you search for posts with a search term, it returns the post page.")
    @Test
    void givenSearchParameters_whenSearchingPosts_thenReturnsPostPage() {
        // Given
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(postRepository.findByTitleContaining(searchKeyword, pageable)).willReturn(Page.empty());

        // When
        Page<PostDto> posts = sut.searchPosts(searchType, searchKeyword, pageable);

        // Then
        assertThat(posts).isEmpty();
        then(postRepository).should().findByTitleContaining(searchKeyword, pageable);
    }

    @DisplayName("If you search for posts by tag without a search term, it returns an empty page.")
    @Test
    void givenNoSearchParameters_whenSearchingPostsViaTag_thenReturnsEmptyPage() {
        // Given
        Pageable pageable = Pageable.ofSize(20);

        // When
        Page<PostDto> posts = sut.searchPostsViaTag(null, pageable);

        // Then
        assertThat(posts).isEqualTo(Page.empty(pageable));
        then(tagRepository).shouldHaveNoInteractions();
        then(postRepository).shouldHaveNoInteractions();
    }

    @DisplayName("If you search for a non-existent tag, it returns an empty page.")
    @Test
    void givenNonexistentTag_whenSearchingPostsViaTag_thenReturnsEmptyPage() {
        // Given
        String tagName = "non-existent tag";
        Pageable pageable = Pageable.ofSize(20);
        given(postRepository.findByTagNames(List.of(tagName), pageable)).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // When
        Page<PostDto> posts = sut.searchPostsViaTag(tagName, pageable);

        // Then
        assertThat(posts).isEqualTo(Page.empty(pageable));
        then(postRepository).should().findByTagNames(List.of(tagName), pageable);
    }

    @DisplayName("If you search for posts by tag, it returns the post page.")
    @Test
    void givenTag_whenSearchingPostsViaTag_thenReturnsPostsPage() {
        // Given
        String tagName = "java";
        Pageable pageable = Pageable.ofSize(20);
        Post expectedPost = createPost();
        given(postRepository.findByTagNames(List.of(tagName), pageable)).willReturn(new PageImpl<>(List.of(expectedPost), pageable, 1));

        // When
        Page<PostDto> posts = sut.searchPostsViaTag(tagName, pageable);

        // Then
        assertThat(posts).isEqualTo(new PageImpl<>(List.of(PostDto.from(expectedPost)), pageable, 1));
        then(postRepository).should().findByTagNames(List.of(tagName), pageable);
    }

    @DisplayName("If you search by post ID, it returns the post with its comments.")
    @Test
    void givenPostId_whenSearchingPostWithComments_thenReturnsPostWithComments() {
        // Given
        Long postId = 1L;
        Post post = createPost();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // When
        PostWithCommentsDto dto = sut.getPostWithComments(postId);

        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", post.getTitle())
                .hasFieldOrPropertyWithValue("content", post.getContent())
                .hasFieldOrPropertyWithValue("tagDtos", post.getTags().stream()
                        .map(TagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );
        then(postRepository).should().findById(postId);
    }

    @DisplayName("If there are no posts with comments, it throws an exception.")
    @Test
    void givenNonexistentPostId_whenSearchingPostWithComments_thenThrowsException() {
        // Given
        Long postId = 0L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // When
        Throwable t = catchThrowable(() -> sut.getPostWithComments(postId));

        // Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("No post, post-id: " + postId);
        then(postRepository).should().findById(postId);
    }

    @DisplayName("If you search for a post, it returns the post.")
    @Test
    void givenPostId_whenSearchingPost_thenReturnsPost() {
        // Given
        Long postId = 1L;
        Post post = createPost();
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // When
        PostDto dto = sut.getPost(postId);

        // Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", post.getTitle())
                .hasFieldOrPropertyWithValue("content", post.getContent())
                .hasFieldOrPropertyWithValue("tagDtos", post.getTags().stream()
                        .map(TagDto::from)
                        .collect(Collectors.toUnmodifiableSet())
                );
        then(postRepository).should().findById(postId);
    }

    @DisplayName("If there are no posts, it throws an exception.")
    @Test
    void givenNonexistentPostId_whenSearchingPost_thenThrowsException() {
        // Given
        Long postId = 0L;
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // When
        Throwable t = catchThrowable(() -> sut.getPost(postId));

        // Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("no post - post-id: " + postId);
        then(postRepository).should().findById(postId);
    }

    @DisplayName("If you input post information, it extracts tag information from the body and creates a post that includes hashtag information.")
    @Test
    void givenPostInfo_whenSavingPost_thenExtractsTagsFromContentAndSavesPostWithExtractedTags() {
        // Given
        PostDto dto = createPostDto();
        Set<String> expectedTagNames = Set.of("java", "spring");
        Set<Tag> expectedTags = new HashSet<>();
        expectedTags.add(createTag("java"));

        given(userAccountRepository.getReferenceById(dto.userAccountDto().username())).willReturn(createUserAccount());
        given(tagService.parseTagNames(dto.content())).willReturn(expectedTagNames);
        given(tagService.findTagsByNames(expectedTagNames)).willReturn(expectedTags);
        given(postRepository.save(any(Post.class))).willReturn(createPost());

        // When
        sut.savePost(dto);

        // Then
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().username());
        then(tagService).should().parseTagNames(dto.content());
        then(tagService).should().findTagsByNames(expectedTagNames);
        then(postRepository).should().save(any(Post.class));
    }



    private UserAccount createUserAccount() {
        return createUserAccount("user");
    }

    private UserAccount createUserAccount(String username) {
        return UserAccount.of(
                username,
                "password",
                "user@email.com",
                "User",
                null
        );
    }

    private Post createPost() {
        return createPost(1L);
    }

    private Post createPost(Long id) {
        Post post = Post.of(
                createUserAccount(),
                "title",
                "content"
        );
        post.addTags(Set.of(
                createTag(1L, "java"),
                createTag(2L, "spring")
        ));
        ReflectionTestUtils.setField(post, "id", id);

        return post;
    }

    private Tag createTag(String tagName) {
        return createTag(1L, tagName);
    }

    private Tag createTag(Long id, String tagName) {
        Tag tag = Tag.of(tagName);
        ReflectionTestUtils.setField(tag, "id", id);

        return tag;
    }

    private TagDto createTagDto() {
        return TagDto.of("java");
    }

    private PostDto createPostDto() {
        return createPostDto("title", "content");
    }

    private PostDto createPostDto(String title, String content) {
        return PostDto.of(
                1L,
                createUserAccountDto(),
                title,
                content,
                null,
                LocalDateTime.now(),
                "User",
                LocalDateTime.now(),
                "User");
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "user",
                "password",
                "user@mail.com",
                "User",
                "This is memo",
                LocalDateTime.now(),
                "user",
                LocalDateTime.now(),
                "user"
        );
    }

}