package com.alaha.chala.controller;

import com.alaha.chala.config.TestSecurityConfig;
import com.alaha.chala.domain.constant.FormStatus;
import com.alaha.chala.domain.constant.SearchType;
import com.alaha.chala.dto.PostDto;
import com.alaha.chala.dto.PostWithCommentsDto;
import com.alaha.chala.dto.TagDto;
import com.alaha.chala.dto.UserAccountDto;
import com.alaha.chala.dto.request.PostRequest;
import com.alaha.chala.dto.response.PostResponse;
import com.alaha.chala.service.PaginationService;
import com.alaha.chala.service.PostService;
import com.alaha.chala.util.FormDataEncoder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Controller/Post")
@Import({TestSecurityConfig.class, FormDataEncoder.class})
@WebMvcTest(PostController.class)
class PostControllerTest {

    private final MockMvc mvc;

    private final FormDataEncoder formDataEncoder;

    @MockBean private PostService postService;
    @MockBean private PaginationService paginationService;


    PostControllerTest(
            @Autowired MockMvc mvc,
            @Autowired FormDataEncoder formDataEncoder
    ) {
        this.mvc = mvc;
        this.formDataEncoder = formDataEncoder;
    }


    @DisplayName("Post List")
    @Test
    void givenNothing_whenRequestingPostsView_thenReturnsPostsView() throws Exception {
        // Given
        given(postService.searchPosts(eq(null), eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));

        // When & Then
        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/index"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attributeExists("searchTypes"))
                .andExpect(model().attribute("searchTypeTag", SearchType.TAG));
        then(postService).should().searchPosts(eq(null), eq(null), any(Pageable.class));
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("Post list by searching")
    @Test
    void givenSearchKeyword_whenSearchingPostsView_thenReturnsPostsView() throws Exception {
        // Given
        SearchType searchType = SearchType.TITLE;
        String searchValue = "title";
        given(postService.searchPosts(eq(searchType), eq(searchValue), any(Pageable.class))).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(0, 1, 2, 3, 4));

        // When & Then
        mvc.perform(
                        get("/posts")
                                .queryParam("searchType", searchType.name())
                                .queryParam("searchValue", searchValue)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/index"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attributeExists("searchTypes"));
        then(postService).should().searchPosts(eq(searchType), eq(searchValue), any(Pageable.class));
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("Post list by paging and sorting")
    @Test
    void givenPagingAndSortingParams_whenSearchingPostsView_thenReturnsPostsView() throws Exception {
        // Given
        String sortName = "title";
        String direction = "desc";
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc(sortName)));
        List<Integer> barNumbers = List.of(1, 2, 3, 4, 5);
        given(postService.searchPosts(null, null, pageable)).willReturn(Page.empty());
        given(paginationService.getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages())).willReturn(barNumbers);

        // When & Then
        mvc.perform(
                        get("/posts")
                                .queryParam("page", String.valueOf(pageNumber))
                                .queryParam("size", String.valueOf(pageSize))
                                .queryParam("sort", sortName + "," + direction)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/index"))
                .andExpect(model().attributeExists("posts"))
                .andExpect(model().attribute("paginationBarNumbers", barNumbers));
        then(postService).should().searchPosts(null, null, pageable);
        then(paginationService).should().getPaginationBarNumbers(pageable.getPageNumber(), Page.empty().getTotalPages());
    }

    @DisplayName("Post list by unauthenticated user")
    @Test
    void givenNothing_whenRequestingPostPage_thenRedirectsToLoginPage() throws Exception {
        // Given
        long postId = 1L;

        // When & Then
        mvc.perform(get("/posts/" + postId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        then(postService).shouldHaveNoInteractions();
        then(postService).shouldHaveNoInteractions();
    }

    @Disabled("FIXING")
    @WithMockUser
    @DisplayName("Post list by authenticated user")
    @Test
    void givenAuthorizedUser_whenRequestingPostView_thenReturnsPostView() throws Exception {
        // Given
        Long postId = 1L;
        long totalCount = 1L;
        given(postService.getPostWithComments(postId)).willReturn(createPostWithCommentsDto());
        given(postService.getPostCount()).willReturn(totalCount);

        // When & Then
        mvc.perform(get("/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/detail"))
                .andExpect(model().attributeExists("post"))
                .andExpect(model().attributeExists("comments"))
                .andExpect(model().attribute("totalCount", totalCount))
                .andExpect(model().attribute("searchTypeTag", SearchType.TAG));
        then(postService).should().getPostWithComments(postId);
        then(postService).should().getPostCount();
    }

    @Disabled
    @DisplayName("Searching page")
    @Test
    void givenNothing_whenRequestingPostSearchView_thenReturnsPostSearchView() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/posts/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/search"));
    }

    @DisplayName("Tag searching page")
    @Test
    void givenNothing_whenRequestingPostSearchTagView_thenReturnsPostSearchTagView() throws Exception {
        // Given
        List<String> tags = List.of("#java", "#spring", "#boot");
        given(postService.searchPostsViaTag(eq(null), any(Pageable.class))).willReturn(Page.empty());
        given(postService.getTags()).willReturn(tags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));

        // When & Then
        mvc.perform(get("/posts/search-tag"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/search-tag"))
                .andExpect(model().attribute("posts", Page.empty()))
                .andExpect(model().attribute("tags", tags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.TAG));
        then(postService).should().searchPostsViaTag(eq(null), any(Pageable.class));
        then(postService).should().getTags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @DisplayName("Tag searching page - searching")
    @Test
    void givenTag_whenRequestingPostSearchTagView_thenReturnsPostSearchTagView() throws Exception {
        // Given
        String tag = "#java";
        List<String> tags = List.of("#java", "#spring", "#boot");
        given(postService.searchPostsViaTag(eq(tag), any(Pageable.class))).willReturn(Page.empty());
        given(postService.getTags()).willReturn(tags);
        given(paginationService.getPaginationBarNumbers(anyInt(), anyInt())).willReturn(List.of(1, 2, 3, 4, 5));

        // When & Then
        mvc.perform(
                        get("/posts/search-tag")
                                .queryParam("searchValue", tag)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/search-tag"))
                .andExpect(model().attribute("posts", Page.empty()))
                .andExpect(model().attribute("tags", tags))
                .andExpect(model().attributeExists("paginationBarNumbers"))
                .andExpect(model().attribute("searchType", SearchType.TAG));
        then(postService).should().searchPostsViaTag(eq(tag), any(Pageable.class));
        then(postService).should().getTags();
        then(paginationService).should().getPaginationBarNumbers(anyInt(), anyInt());
    }

    @Disabled("FIXING")
    @WithMockUser
    @DisplayName("New post form")
    @Test
    void givenNothing_whenRequesting_thenReturnsNewPostPage() throws Exception {
        // Given

        // When & Then
        mvc.perform(get("/posts/form"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("posts/form"))
                .andExpect(model().attribute("formStatus", FormStatus.CREATE));
    }




    private PostDto createPostDto() {
        return PostDto.of(
                createUserAccountDto(),
                "title",
                "content",
                Set.of(TagDto.of("java"))
        );
    }

    private PostWithCommentsDto createPostWithCommentsDto() {
        return PostWithCommentsDto.of(
                1L,
                createUserAccountDto(),
                Set.of(),
                "title",
                "content",
                Set.of(TagDto.of("java")),
                LocalDateTime.now(),
                "user",
                LocalDateTime.now(),
                "user"
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                "user",
                "pw",
                "user@mail.com",
                "User",
                "memo",
                LocalDateTime.now(),
                "user",
                LocalDateTime.now(),
                "user"
        );
    }

}
