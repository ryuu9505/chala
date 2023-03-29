package com.alaha.chala.service;

import com.alaha.chala.domain.Comment;
import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.Tag;
import com.alaha.chala.domain.UserAccount;
import com.alaha.chala.dto.CommentDto;
import com.alaha.chala.dto.UserAccountDto;
import com.alaha.chala.repository.CommentRepository;
import com.alaha.chala.repository.PostRepository;
import com.alaha.chala.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@DisplayName("Service/Comment")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks private CommentService sut;

    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private UserAccountRepository userAccountRepository;

    @DisplayName("If you search by post ID, it returns a list of corresponding comments.")
    @Test
    void givenPostId_whenSearchingComments_thenReturnsComments() {
        // Given
        Long postId = 1L;
        Comment expectedParentComment = createComment(1L, "parent content");
        Comment expectedChildComment = createComment(2L, "child content");
        expectedChildComment.setParentCommentId(expectedParentComment.getId());
        given(commentRepository.findByPost_Id(postId)).willReturn(List.of(
                expectedParentComment,
                expectedChildComment
        ));

        // When
        List<CommentDto> actual = sut.searchComments(postId);

        // Then
        assertThat(actual).hasSize(2);
        assertThat(actual)
                .extracting("id", "postId", "parentCommentId", "content")
                .containsExactlyInAnyOrder(
                        tuple(1L, 1L, null, "parent content"),
                        tuple(2L, 1L, 1L, "child content")
                );
        then(commentRepository).should().findByPost_Id(postId);
    }

    @DisplayName("If you input comment information, it saves the comment.")
    @Test
    void givenCommentInfo_whenSavingComment_thenSavesComment() {
        // Given
        CommentDto dto = createCommentDto("댓글");
        given(postRepository.getReferenceById(dto.postId())).willReturn(createPost());
        given(userAccountRepository.getReferenceById(dto.userAccountDto().username())).willReturn(createUserAccount());
        given(commentRepository.save(any(Comment.class))).willReturn(null);

        // When
        sut.saveComment(dto);

        // Then
        then(postRepository).should().getReferenceById(dto.postId());
        then(userAccountRepository).should().getReferenceById(dto.userAccountDto().username());
        then(commentRepository).should(never()).getReferenceById(anyLong());
        then(commentRepository).should().save(any(Comment.class));
    }

    @DisplayName("If you attempt to save a comment but there is no matching post, it logs a warning and does nothing.")
    @Test
    void givenNonexistentPost_whenSavingComment_thenLogsSituationAndDoesNothing() {
        // Given
        CommentDto dto = createCommentDto("댓글");
        given(postRepository.getReferenceById(dto.postId())).willThrow(EntityNotFoundException.class);

        // When
        sut.saveComment(dto);

        // Then
        then(postRepository).should().getReferenceById(dto.postId());
        then(userAccountRepository).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
    }

    @DisplayName("If you input the parent comment ID and comment information, it saves a reply comment.")
    @Test
    void givenParentCommentIdAndCommentInfo_whenSaving_thenSavesChildComment() {
        // Given
        Long parentCommentId = 1L;
        Comment parent = createComment(parentCommentId, "comment");
        CommentDto child = createCommentDto(parentCommentId, "reply comment");
        given(postRepository.getReferenceById(child.postId())).willReturn(createPost());
        given(userAccountRepository.getReferenceById(child.userAccountDto().username())).willReturn(createUserAccount());
        given(commentRepository.getReferenceById(child.parentCommentId())).willReturn(parent);

        // When
        sut.saveComment(child);

        // Then
        assertThat(child.parentCommentId()).isNotNull();
        then(postRepository).should().getReferenceById(child.postId());
        then(userAccountRepository).should().getReferenceById(child.userAccountDto().username());
        then(commentRepository).should().getReferenceById(child.parentCommentId());
        then(commentRepository).should(never()).save(any(Comment.class));
    }

    @DisplayName("If you input the comment ID, it deletes the comment.")
    @Test
    void givenCommentId_whenDeletingComment_thenDeletesComment() {
        // Given
        Long commentId = 1L;
        String username = "user";
        willDoNothing().given(commentRepository).deleteByIdAndUserAccount_Username(commentId, username);

        // When
        sut.deleteComment(commentId, username);

        // Then
        then(commentRepository).should().deleteByIdAndUserAccount_Username(commentId, username);
    }

    private CommentDto createCommentDto(String content) {
        return createCommentDto(null, content);
    }

    private CommentDto createCommentDto(Long parentCommentId, String content) {
        return createCommentDto(1L, parentCommentId, content);
    }

    private CommentDto createCommentDto(Long id, Long parentCommentId, String content) {
        return CommentDto.of(
                id,
                1L,
                createUserAccountDto(),
                parentCommentId,
                content,
                LocalDateTime.now(),
                "user",
                LocalDateTime.now(),
                "user"
        );
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

    private Comment createComment(Long id, String content) {
        Comment comment = Comment.of(
                createPost(),
                createUserAccount(),
                content
        );
        ReflectionTestUtils.setField(comment, "id", id);

        return comment;
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "user",
                "password",
                "user@email.com",
                "User",
                null
        );
    }

    private Post createPost() {
        Post post = Post.of(
                createUserAccount(),
                "title",
                "content"
        );
        ReflectionTestUtils.setField(post, "id", 1L);
        post.addTags(Set.of(createTag(post)));

        return post;
    }

    private Tag createTag(Post post) {
        return Tag.of("java");
    }

}