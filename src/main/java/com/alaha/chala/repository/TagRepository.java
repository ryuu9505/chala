package com.alaha.chala.repository;

import com.alaha.chala.domain.Tag;
import com.alaha.chala.repository.querydsl.TagRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RepositoryRestResource
public interface TagRepository extends
        JpaRepository<Tag, Long>,
        TagRepositoryCustom,
        QuerydslPredicateExecutor<Tag> {
    Optional<Tag> findByTagName(String tagName);
    List<Tag> findByTagNameIn(Set<String> tagNames);
}
