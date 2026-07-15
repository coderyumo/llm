package com.hollis.llm.rag.controller;

import com.hollis.llm.rag.router.QueryRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-14 15:12
 **/
@RestController
@RequestMapping("/rag/router")
public class RagRouterController {

    @Autowired
    private QueryRouteService queryRouteService;


    @GetMapping("/route")
    public String route(String question) {
        return queryRouteService.route(question);
    }

}
