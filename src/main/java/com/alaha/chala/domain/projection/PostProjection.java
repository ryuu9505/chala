package com.alaha.chala.domain.projection;

import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.UserAccount;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name ="withUserAccount", types = Post.class)
public interface PostProjection {
    Long getId();
    UserAccount getUserAccount();
    String getTitle();
    String getContent();
    LocalDateTime getCreatedAt();
    String getCreatedBy();
    LocalDateTime getModifiedAt();
    String getModifiedBy();
}