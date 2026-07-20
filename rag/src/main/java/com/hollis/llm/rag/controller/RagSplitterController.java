package com.hollis.llm.rag.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.hollis.llm.rag.cleaner.DocumentCleaner;
import com.hollis.llm.rag.reader.DocumentReaderFactory;
import com.hollis.llm.rag.reader.PdfMultimodalProcessor;
import com.hollis.llm.rag.splitter.ModalTextSplitter;
import com.hollis.llm.rag.splitter.OverlapParagraphTextSplitter;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: <a href="https://github.com/coderyumo">程序员雨墨</a>
 * @create: 2026-07-08 16:17
 **/
@RestController
@RequestMapping("/rag/splitter")
public class RagSplitterController {

    @Autowired
    private DocumentReaderFactory documentReaderFactory;


    /**
     * 分片
     */
    @GetMapping("/split")
    public String splitter(@RequestParam("path") String path) {
        File file = new File(path);
        List<Document> documents;
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是有效文件: " + path);
        }
        try {
            documents = DocumentCleaner.cleanDocuments(documentReaderFactory.read(new File(path)));
            for (Document document : documents) {
                System.out.println("before chunk : " + document.getText());
                System.out.println("=================");
                OverlapParagraphTextSplitter tokenTextSplitter = new OverlapParagraphTextSplitter(
                        100,
                        5
                );
                List<Document> documentList = tokenTextSplitter.split(document);
                for (Document document1 : documentList) {
                    System.out.println("after chunk : " + document1.getText());
                }
                System.out.println("=================");
            }
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
        return "success";
    }


    /**
     * 递归分片
     */
    @GetMapping("/splitRecursive")
    public String splitRecursive(@RequestParam("path") String path) {
        File file = new File(path);
        List<Document> documents;
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("文件不存在或不是有效文件: " + path);
        }
        try {
            documents = documentReaderFactory.read(new File(path));
            for (Document document : documents) {
                System.out.println("before chunk : " + document.getText());
                System.out.println("=================");


                RecursiveCharacterTextSplitter recursiveCharacterTextSplitter = new RecursiveCharacterTextSplitter(
                        500, new String[]{"\n\n", "\n"});
                List<Document> documentList = recursiveCharacterTextSplitter.split(document);
                for (Document document1 : documentList) {
                    System.out.println("after chunk : " + document1.getText());
                }
                System.out.println("=================");
            }
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
        return "success";
    }


    /**
     * 递归分片
     */
    @GetMapping("/splitSentence")
    public String splitSentence(@RequestParam("path") String path) {
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(100, 10);

        for (String textSegment : splitter.split("""
                            Harry Potter is a series of seven fantasy novels written by British author J. K. Rowling. The novels chronicle the lives of a young wizard, Harry Potter, and his friends, Ron Weasley and Hermione Granger, all of whom are students at Hogwarts School of Witchcraft and Wizardry. The main story arc concerns Harry's conflict with Lord Voldemort, a dark wizard who intends to become immortal, overthrow the wizard governing body known as the Ministry of Magic, and subjugate all wizards and Muggles (non-magical people).
                            The series was originally published in English by Bloomsbury in the United Kingdom and Scholastic Press in the United States. A series of many genres, including fantasy, drama, coming-of-age fiction, and the British school story (which includes elements of mystery, thriller, adventure, horror, and romance), the world of Harry Potter explores numerous themes and includes many cultural meanings and references.[1] Major themes in the series include prejudice, corruption, madness, love, and death.[2]
                """)) {
            System.out.println(textSegment);
        }
        return "success";
    }

    @Autowired
    private PdfMultimodalProcessor processor;

    @RequestMapping("/multiModel")
    public String multiModel(String filePath) throws Exception {
        String result = processor.processPdf(new File(filePath));
        ModalTextSplitter modalTextSplitter = new ModalTextSplitter(300, 20);
        List<Document> documentList = modalTextSplitter.split(new Document(result));
        for (Document document : documentList) {
            System.out.println(document.getText());
            System.out.println(document.getMetadata());
            System.out.println("=================");
        }

        return "success";
    }


    public static void main(String[] args) {
        RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(100);
        List<String> chunks = splitter.splitText("""
                Harry Potter is a series of seven fantasy novels written by British author J. K. Rowling. The novels chronicle the lives of a young wizard, Harry Potter, and his friends, Ron Weasley and Hermione Granger, all of whom are students at Hogwarts School of Witchcraft and Wizardry. The main story arc concerns Harry's conflict with Lord Voldemort, a dark wizard who intends to become immortal, overthrow the wizard governing body known as the Ministry of Magic, and subjugate all wizards and Muggles (non-magical people).
                        The series was originally published in English by Bloomsbury in the United Kingdom and Scholastic Press in the United States. A series of many genres, including fantasy, drama, coming-of-age fiction, and the British school story (which includes elements of mystery, thriller, adventure, horror, and romance), the world of Harry Potter explores numerous themes and includes many cultural meanings and references.[1] Major themes in the series include prejudice, corruption, madness, love, and death.[2]
                """);

        chunks.forEach(System.out::println);
    }

}
