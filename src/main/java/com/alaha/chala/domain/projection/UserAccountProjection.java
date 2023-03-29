package com.alaha.chala.domain.projection;

import com.alaha.chala.domain.UserAccount;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

@Projection(name ="withoutPassword", types = UserAccount.class)
public interface UserAccountProjection {
    String getUsername();
    String getEmail();
    String getNickname();
    String getMemo();
    LocalDateTime getCreatedAt();
}