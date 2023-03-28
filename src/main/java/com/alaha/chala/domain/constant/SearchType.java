package com.alaha.chala.domain.constant;

import lombok.Getter;

public enum SearchType {
    TITLE("Title"),
    CONTENT("Content"),
    ID("Username"),
    NICKNAME("Nickname"),
    TAG("Tag");

    @Getter
    private final String description;

    SearchType(String description) {
        this.description = description;
    }

}