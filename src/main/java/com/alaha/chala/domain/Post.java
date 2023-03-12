package com.alaha.chala.domain;

import java.time.LocalDateTime;

public class Post {
    private Long id;
    private String title;
    private String content;
    private String tag;

    private LocalDateTime createAt;
    private String createdBy;
    private LocalDateTime modifiedAt;
    private String modifiedBy;
}
