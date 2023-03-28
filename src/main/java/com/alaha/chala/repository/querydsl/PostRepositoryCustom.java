package com.alaha.chala.repository.querydsl;

import com.alaha.chala.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface PostRepositoryCustom {
    @Deprecated
    List<String> findAllDistinctTags();
    Page<Post> findByTagNames(Collection<String> tagNames, Pageable pageable);
}
