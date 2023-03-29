package com.alaha.chala.repository.querydsl;

import com.alaha.chala.domain.QTag;
import com.alaha.chala.domain.Tag;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class TagRepositoryCustomImpl extends QuerydslRepositorySupport implements TagRepositoryCustom {

    public TagRepositoryCustomImpl() {
        super(Tag.class);
    }

    @Override
    public List<String> findAllTagNames() {
        QTag tag = QTag.tag;

        return from(tag)
                .select(tag.tagName)
                .fetch();
    }

}
