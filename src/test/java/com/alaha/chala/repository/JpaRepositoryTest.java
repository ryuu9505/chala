package com.alaha.chala.repository;

import com.alaha.chala.config.JpaConfig;
import com.alaha.chala.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA Connection Test")
@Import(JpaConfig.class)
@DataJpaTest
class JpaRepositoryTest {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public JpaRepositoryTest(
            @Autowired PostRepository postRepository,
            @Autowired CommentRepository commentRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @DisplayName("select test")
    @Test
    void givenTestData_whenSelecting_thenWorksFine() {
        // Given by data.sql

        // When
        List<Post> posts = postRepository.findAll();

        // Then
        assertThat(posts)
                .isNotNull()
                .hasSize(1000);
    }

    @DisplayName("insert test")
    @Test
    void givenTestData_whenInserting_thenWorksFine() {
        // Given by data.sql
        long previousCount = postRepository.count();

        // When
        Post savedPost = postRepository.save(Post.of("title", "content", "Korea"));

        // Then
        assertThat(postRepository.count()).isEqualTo(previousCount + 1);
    }

    @DisplayName("update test")
    @Test
    void givenTestData_whenUpdating_thenWorksFine() {
        // Given by data.sql
        Post post = postRepository.findById(1L).orElseThrow();
        String updatedTag = "NEW_TAG";
        post.setTag(updatedTag);

        // When
        Post savedPost = postRepository.saveAndFlush(post);

        // Then
        assertThat(savedPost).hasFieldOrPropertyWithValue("tag", updatedTag);
    }

    @DisplayName("delete test")
    @Test
    void givenTestData_whenDeleting_thenWorksFine() {
        // Given
        Post article = postRepository.findById(1L).orElseThrow();
        long previousPostCount = postRepository.count();
        long previousCommentCount = commentRepository.count();
        int deletedCommentsSize = article.getComments().size();

        // When
        postRepository.delete(article);

        // Then
        assertThat(postRepository.count()).isEqualTo(previousPostCount - 1);
        assertThat(commentRepository.count()).isEqualTo(previousCommentCount - deletedCommentsSize);
    }

}