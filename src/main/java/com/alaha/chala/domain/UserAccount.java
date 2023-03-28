package com.alaha.chala.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Objects;

@Getter
@ToString
@Table(indexes = {
        @Index(columnList = "email", unique = true),
        @Index(columnList = "createdAt"),
})
@EntityListeners(AuditingEntityListener.class)
@Entity
public class UserAccount extends AuditingFields {

    @Id
    @Column(nullable = false, length = 50)
    private String username;
    @Setter
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(length = 100)
    private String email;

    @Setter
    @Column(length = 100)
    private String nickname;

    @Setter
    private String memo;

    protected UserAccount() {}

    private UserAccount(String username, String password, String email, String nickname, String memo, String createdBy) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.memo = memo;
        this.createdBy = createdBy;
        this.modifiedBy = createdBy;
    }

    public static UserAccount of(String username, String password, String email, String nickname, String memo) {
        return UserAccount.of(username, password, email, nickname, memo, null);
    }

    public static UserAccount of(String userId, String userPassword, String email, String nickname, String memo, String createdBy) {
        return new UserAccount(userId, userPassword, email, nickname, memo, createdBy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount userAccount)) return false;
        return this.getUsername() != null && this.getUsername().equals(userAccount.getUsername());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getUsername());
    }

}
