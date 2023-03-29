package com.alaha.chala.repository.querydsl;

import com.alaha.chala.domain.Post;
import com.alaha.chala.domain.QPost;
import com.alaha.chala.domain.QTag;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Collection;
import java.util.List;

public class PostRepositoryCustomImpl extends QuerydslRepositorySupport implements PostRepositoryCustom {

    public PostRepositoryCustomImpl() {
        super(Post.class);
    }

    @Override
    public List<String> findAllDistinctTags() {
        QPost post = QPost.post;

        return from(post)
                .distinct()
                .select(post.tags.any().tagName)
                .fetch();
    }

    @Override
    public Page<Post> findByTagNames(Collection<String> tagNames, Pageable pageable) {
        QTag tag = QTag.tag;
        QPost post = QPost.post;

        JPQLQuery<Post> query = from(post)
                .innerJoin(post.tags, tag)
                .where(tag.tagName.in(tagNames));
        List<Post> posts = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(posts, pageable, query.fetchCount());
    }

}
