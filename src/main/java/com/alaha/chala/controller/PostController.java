package com.alaha.chala.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/posts")
@Controller
public class PostController {

    @GetMapping
    public String posts(ModelMap map) {
        map.addAttribute("posts", List.of());

        return "posts/index";
    }

    @GetMapping("/{postId}")
    public String post(@PathVariable Long postId, ModelMap map) {
        map.addAttribute("post", "post"); // todo: update "post" to real value
        map.addAttribute("Comments", List.of());

        return "posts/detail";
    }
}
