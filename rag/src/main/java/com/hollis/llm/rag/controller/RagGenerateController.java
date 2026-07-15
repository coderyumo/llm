package com.hollis.llm.rag.controller;

import com.hollis.llm.rag.generate.SqlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-14 15:24
 **/
@RestController
@RequestMapping("/rag/generate")
public class RagGenerateController {


    @Autowired
    private SqlQueryService sqlQueryService;


    @GetMapping("/sql/query")
    public Object sqlQuery(String query) {
        return sqlQueryService.text2sql(query);
    }

}
