package com.alaha.chala.controller;

import com.alaha.chala.domain.constant.FormStatus;
import com.alaha.chala.domain.constant.SearchType;
import com.alaha.chala.dto.request.PostRequest;
import com.alaha.chala.dto.response.PostResponse;
import com.alaha.chala.dto.response.PostWithCommentsResponse;
import com.alaha.chala.dto.security.BoardPrincipal;
import com.alaha.chala.service.PaginationService;
import com.alaha.chala.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/posts")
@Controller
public class PostController {

    private final PostService postService;
    private final PaginationService paginationService;

    @GetMapping
    public String posts(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<PostResponse> posts = postService.searchPosts(searchType, searchValue, pageable).map(PostResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), posts.getTotalPages());

        map.addAttribute("posts", posts);
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchTypes", SearchType.values());
        map.addAttribute("searchTypeTag", SearchType.TAG);

        return "posts/index";
    }

    @GetMapping("/{postId}")
    public String post(@PathVariable Long postId, ModelMap map) {
        PostWithCommentsResponse post = PostWithCommentsResponse.from(postService.getPostWithComments(postId));

        map.addAttribute("post", post);
        map.addAttribute("comments", post.commentsResponse());
        map.addAttribute("totalCount", postService.getPostCount());
        map.addAttribute("searchTypeTag", SearchType.TAG);

        return "posts/detail";
    }

    @GetMapping("/search-tag")
    public String searchPostTag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<PostResponse> posts = postService.searchPostsViaTag(searchValue, pageable).map(PostResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), posts.getTotalPages());
        List<String> tags = postService.getTags();

        map.addAttribute("posts", posts);
        map.addAttribute("tags", tags);
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchType", SearchType.TAG);

        return "posts/search-tag";
    }

    @GetMapping("/form")
    public String postForm(ModelMap map) {
        map.addAttribute("formStatus", FormStatus.CREATE);

        return "posts/form";
    }

    @PostMapping("/form")
    public String postNewPost(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            PostRequest postRequest
    ) {
        postService.savePost(postRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/posts";
    }

    @GetMapping("/{postId}/form")
    public String updatePostForm(@PathVariable Long postId, ModelMap map) {
        PostResponse post = PostResponse.from(postService.getPost(postId));

        map.addAttribute("post", post);
        map.addAttribute("formStatus", FormStatus.UPDATE);

        return "posts/form";
    }

    @PostMapping("/{postId}/form")
    public String updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            PostRequest postRequest
    ) {
        postService.updatePost(postId, postRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/posts/" + postId;
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        postService.deletePost(postId, boardPrincipal.getUsername());

        return "redirect:/posts";
    }

}
