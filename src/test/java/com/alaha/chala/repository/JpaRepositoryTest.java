package com.alaha.chala.repository;

import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.Comment;
import com.alaha.chala.domain.Tag;
import com.alaha.chala.domain.UserAccount;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA 연결 테스트")
@Import(JpaRepositoryTest.TestJpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserAccountRepository userAccountRepository;
    private final TagRepository tagRepository;

    JpaRepositoryTest(
            @Autowired PostRepository postRepository,
            @Autowired CommentRepository commentRepository,
            @Autowired UserAccountRepository userAccountRepository,
            @Autowired TagRepository tagRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userAccountRepository = userAccountRepository;
        this.tagRepository = tagRepository;
    }

    @DisplayName("select 테스트")
    @Test
    void givenTestData_whenSelecting_thenWorksFine() {
        // Given

        // When
        List<Post> posts = postRepository.findAll();

        // Then
        assertThat(posts)
                .isNotNull()
                .hasSize(100); // classpath:resources/data.sql 참조
    }

    @DisplayName("insert 테스트")
    @Test
    void givenTestData_whenInserting_thenWorksFine() {
        // Given
        long previousCount = postRepository.count();
        UserAccount userAccount = userAccountRepository.save(UserAccount.of("newUser", "pw", null, null, null));
        Post post = Post.of(userAccount, "new post", "new content");
        post.addTags(Set.of(Tag.of("spring")));

        // When
        postRepository.save(post);

        // Then
        assertThat(postRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update 테스트")
    @Test
    void givenTestData_whenUpdating_thenWorksFine() {
        // Given
        Post post = postRepository.findById(1L).orElseThrow();
        Tag updatedTag = Tag.of("springboot");
        post.clearTags();
        post.addTags(Set.of(updatedTag));

        // When
        Post savedPost = postRepository.saveAndFlush(post);

        // Then
        assertThat(savedPost.getTags())
                .hasSize(1)
                .extracting("tagName", String.class)
                .containsExactly(updatedTag.getTagName());
    }

    @DisplayName("delete 테스트")
    @Test
    void givenTestData_whenDeleting_thenWorksFine() {
        // Given
        Post post = postRepository.findById(1L).orElseThrow();
        long previousPostCount = postRepository.count();
        long previousCommentCount = commentRepository.count();
        int deletedCommentsSize = post.getComments().size();

        // When
        postRepository.delete(post);

        // Then
        assertThat(postRepository.count()).isEqualTo(previousPostCount - 1);
        assertThat(commentRepository.count()).isEqualTo(previousCommentCount - deletedCommentsSize);
    }

    @DisplayName("대댓글 조회 테스트")
    @Test
    void givenParentCommentId_whenSelecting_thenReturnsChildComments() {
        // Given

        // When
        Optional<Comment> parentComment = commentRepository.findById(1L);

        // Then
        assertThat(parentComment).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(4);
    }

    @DisplayName("댓글에 대댓글 삽입 테스트")
    @Test
    void givenParentComment_whenSaving_thenInsertsChildComment() {
        // Given
        Comment parentComment = commentRepository.getReferenceById(1L);
        Comment childComment = Comment.of(
                parentComment.getPost(),
                parentComment.getUserAccount(),
                "대댓글"
        );

        // When
        parentComment.addChildComment(childComment);
        commentRepository.flush();

        // Then
        assertThat(commentRepository.findById(1L)).get()
                .hasFieldOrPropertyWithValue("parentCommentId", null)
                .extracting("childComments", InstanceOfAssertFactories.COLLECTION)
                .hasSize(5);
    }

    @DisplayName("댓글 삭제와 대댓글 전체 연동 삭제 테스트")
    @Test
    void givenCommentHavingChildComments_whenDeletingParentComment_thenDeletesEveryComment() {
        // Given
        Comment parentComment = commentRepository.getReferenceById(1L);
        long previousCommentCount = commentRepository.count();

        // When
        commentRepository.delete(parentComment);

        // Then
        assertThat(commentRepository.count()).isEqualTo(previousCommentCount - 5); // 테스트 댓글 + 대댓글 4개
    }

    @Disabled
    @DisplayName("댓글 삭제와 대댓글 전체 연동 삭제 테스트 - 댓글 ID + 유저 ID")
    @Test
    void givenCommentIdHavingChildCommentsAndUsername_whenDeletingParentComment_thenDeletesEveryComment() {
        // Given
        long previousCommentCount = commentRepository.count();

        // When
        commentRepository.deleteByIdAndUserAccount_Username(1L, "user");

        // Then
        assertThat(commentRepository.count()).isEqualTo(previousCommentCount - 5); // 테스트 댓글 + 대댓글 4개
    }

    @DisplayName("[Querydsl] 전체 tag 리스트에서 이름만 조회하기")
    @Test
    void givenNothing_whenQueryingTags_thenReturnsTagNames() {
        // Given

        // When
        List<String> tagNames = tagRepository.findAllTagNames();

        // Then
        assertThat(tagNames).hasSize(19);
    }

    @Disabled
    @DisplayName("[Querydsl] tag로 페이징된 게시글 검색하기")
    @Test
    void givenTagNamesAndPageable_whenQueryingPosts_thenReturnsPostPage() {
        // Given
        List<String> tagNames = List.of("blue", "crimson", "fuscia");
        Pageable pageable = PageRequest.of(0, 5, Sort.by(
                Sort.Order.desc("tags.tagName"),
                Sort.Order.asc("title")
        ));

        // When
        Page<Post> postPage = postRepository.findByTagNames(tagNames, pageable);

        // Then
        assertThat(postPage.getContent()).hasSize(pageable.getPageSize());
        assertThat(postPage.getContent().get(0).getTitle()).isEqualTo("Fusce posuere felis sed lacus.");
        assertThat(postPage.getContent().get(0).getTags())
                .extracting("tagName", String.class)
                .containsExactly("fuscia");
        assertThat(postPage.getTotalElements()).isEqualTo(17);
        assertThat(postPage.getTotalPages()).isEqualTo(4);
    }


    @EnableJpaAuditing
    @TestConfiguration
    static class TestJpaConfig {
        @Bean
        AuditorAware<String> auditorAware() {
            return () -> Optional.of("user");
        }
    }

}
