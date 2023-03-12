package com.alaha.chala.domain;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private Post post;
    private String content;

    private LocalDateTime createAt;
    private String createdBy;
    private LocalDateTime modifiedAt;
    private String modifiedBy;
}
