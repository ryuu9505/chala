package com.alaha.chala.domain.projection;

import com.alaha.chala.domain.Comment;
import com.alaha.chala.domain.UserAccount;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name = "withUserAccount", types = Comment.class)
public interface CommentProjection {
    Long getId();
    UserAccount getUserAccount();
    Long getParentCommentId();
    String getContent();
    LocalDateTime getCreatedAt();
    String getCreatedBy();
    LocalDateTime getModifiedAt();
    String getModifiedBy();
}
