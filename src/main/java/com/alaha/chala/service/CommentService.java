package com.alaha.chala.service;

import com.alaha.chala.domain.Comment;
import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.UserAccount;
import com.alaha.chala.dto.CommentDto;
import com.alaha.chala.repository.CommentRepository;
import com.alaha.chala.repository.PostRepository;
import com.alaha.chala.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> searchComments(Long postId) {
        return commentRepository.findByPost_Id(postId)
                .stream()
                .map(CommentDto::from)
                .toList();
    }

    public void saveComment(CommentDto dto) {
        try {
            Post post = postRepository.getReferenceById(dto.postId());
            UserAccount userAccount = userAccountRepository.getReferenceById(dto.userAccountDto().username());
            Comment comment = dto.toEntity(post, userAccount);

            if (dto.parentCommentId() != null) {
                Comment parentComment = commentRepository.getReferenceById(dto.parentCommentId());
                parentComment.addChildComment(comment);
            } else {
                commentRepository.save(comment);
            }
        } catch (EntityNotFoundException e) {
            log.warn("댓글 저장 실패. 댓글 작성에 필요한 정보를 찾을 수 없습니다 - {}", e.getLocalizedMessage());
        }
    }

    public void deleteComment(Long commentId, String username) {
        commentRepository.deleteByIdAndUserAccount_Username(commentId, username);
    }

}
