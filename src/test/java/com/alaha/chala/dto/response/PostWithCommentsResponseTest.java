package com.alaha.chala.dto.response;

import com.alaha.chala.dto.CommentDto;
import com.alaha.chala.dto.PostWithCommentsDto;
import com.alaha.chala.dto.TagDto;
import com.alaha.chala.dto.UserAccountDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO/PostWithComment")
class PostWithCommentsResponseTest {

    @DisplayName("When converting a post without any child comments and a comment DTO to an API response, sort the comments in descending order of time and ascending order of ID.")
    @Test
    void givenPostWithCommentsDtoWithoutChildComments_whenMapping_thenOrganizesCommentsWithCertainOrder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<CommentDto> commentDtos = Set.of(
                createCommentDto(1L, null, now),
                createCommentDto(2L, null, now.plusDays(1L)),
                createCommentDto(3L, null, now.plusDays(3L)),
                createCommentDto(4L, null, now),
                createCommentDto(5L, null, now.plusDays(5L)),
                createCommentDto(6L, null, now.plusDays(4L)),
                createCommentDto(7L, null, now.plusDays(2L)),
                createCommentDto(8L, null, now.plusDays(7L))
        );
        PostWithCommentsDto input = createPostWithCommentsDto(commentDtos);

        // When
        PostWithCommentsResponse actual = PostWithCommentsResponse.from(input);

        // Then
        assertThat(actual.commentsResponse())
                .containsExactly(
                        createCommentResponse(8L, null, now.plusDays(7L)),
                        createCommentResponse(5L, null, now.plusDays(5L)),
                        createCommentResponse(6L, null, now.plusDays(4L)),
                        createCommentResponse(3L, null, now.plusDays(3L)),
                        createCommentResponse(7L, null, now.plusDays(2L)),
                        createCommentResponse(2L, null, now.plusDays(1L)),
                        createCommentResponse(1L, null, now),
                        createCommentResponse(4L, null, now)
                );
    }

    @DisplayName("When converting a post and comment DTO to an API response, the parent-child relationship of comments is sorted and organized according to their respective rules.")
    @Test
    void givenPostWithCommentsDto_whenMapping_thenOrganizesParentAndChildCommentsWithCertainOrders() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<CommentDto> commentDtos = Set.of(
                createCommentDto(1L, null, now),
                createCommentDto(2L, 1L, now.plusDays(1L)),
                createCommentDto(3L, 1L, now.plusDays(3L)),
                createCommentDto(4L, 1L, now),
                createCommentDto(5L, null, now.plusDays(5L)),
                createCommentDto(6L, null, now.plusDays(4L)),
                createCommentDto(7L, 6L, now.plusDays(2L)),
                createCommentDto(8L, 6L, now.plusDays(7L))
        );
        PostWithCommentsDto input = createPostWithCommentsDto(commentDtos);

        // When
        PostWithCommentsResponse actual = PostWithCommentsResponse.from(input);

        // Then
        assertThat(actual.commentsResponse())
                .containsExactly(
                        createCommentResponse(5L, null, now.plusDays(5)),
                        createCommentResponse(6L, null, now.plusDays(4)),
                        createCommentResponse(1L, null, now)
                )
                .flatExtracting(CommentResponse::childComments)
                .containsExactly(
                        createCommentResponse(7L, 6L, now.plusDays(2L)),
                        createCommentResponse(8L, 6L, now.plusDays(7L)),
                        createCommentResponse(4L, 1L, now),
                        createCommentResponse(2L, 1L, now.plusDays(1L)),
                        createCommentResponse(3L, 1L, now.plusDays(3L))
                );
    }

    @DisplayName("When converting post and comment DTOs to API responses, there is no limit on the depth of parent-child relationships.")
    @Test
    void givenPostWithCommentsDto_whenMapping_thenOrganizesParentAndChildCommentsWithoutDepthLimit() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Set<CommentDto> commentDtos = Set.of(
                createCommentDto(1L, null, now),
                createCommentDto(2L, 1L, now.plusDays(1L)),
                createCommentDto(3L, 2L, now.plusDays(2L)),
                createCommentDto(4L, 3L, now.plusDays(3L)),
                createCommentDto(5L, 4L, now.plusDays(4L)),
                createCommentDto(6L, 5L, now.plusDays(5L)),
                createCommentDto(7L, 6L, now.plusDays(6L)),
                createCommentDto(8L, 7L, now.plusDays(7L))
        );
        PostWithCommentsDto input = createPostWithCommentsDto(commentDtos);

        // When
        PostWithCommentsResponse actual = PostWithCommentsResponse.from(input);

        // Then
        Iterator<CommentResponse> iterator = actual.commentsResponse().iterator();
        long i = 1L;
        while (iterator.hasNext()) {
            CommentResponse commentResponse = iterator.next();
            assertThat(commentResponse)
                    .hasFieldOrPropertyWithValue("id", i)
                    .hasFieldOrPropertyWithValue("parentCommentId", i == 1L ? null : i - 1L)
                    .hasFieldOrPropertyWithValue("createdAt", now.plusDays(i - 1L));

            iterator = commentResponse.childComments().iterator();
            i++;
        }
    }


    private PostWithCommentsDto createPostWithCommentsDto(Set<CommentDto> commentDtos) {
        return PostWithCommentsDto.of(
                1L,
                createUserAccountDto(),
                commentDtos,
                "title",
                "content",
                Set.of(TagDto.of("java")),
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

    private CommentDto createCommentDto(Long id, Long parentCommentId, LocalDateTime createdAt) {
        return CommentDto.of(
                id,
                1L,
                createUserAccountDto(),
                parentCommentId,
                "test comment " + id,
                createdAt,
                "user",
                createdAt,
                "user"
        );
    }

    private CommentResponse createCommentResponse(Long id, Long parentCommentId, LocalDateTime createdAt) {
        return CommentResponse.of(
                id,
                "test comment " + id,
                createdAt,
                "user@mail.com",
                "User",
                "user",
                parentCommentId
        );
    }

}
