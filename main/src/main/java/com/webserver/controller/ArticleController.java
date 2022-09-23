package com.webserver.controller;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.core.ClientHandler;
import com.webserver.entity.Article;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.net.URISyntaxException;

/**
 * 處理與文章相關的業務
 */
@Controller
public class ArticleController {
    //該目錄用於保存所有的發表的文章
    private static final File ARTICLE_DIR=new File("./articles");
    static {
        if (!ARTICLE_DIR.exists()) {//判斷文件是否存在
            ARTICLE_DIR.mkdirs();//創建目錄
        }
    }
    @RequestMapping("/myweb/writeArticle")
    public void writeArticle(HttpServletRequest request, HttpServletResponse response){
        System.out.println("開始處理發表文章");
        String title=request.getParameter("title");
        String author=request.getParameter("author");
        String content=request.getParameter("content");
        if(title==null||author==null||content==null){
            response.sendRediect("/myweb/writeArticle_info_error.html");
            return;
        }
        File articleFile=new File(ARTICLE_DIR,title+".obj");
        try(
                FileOutputStream fos=new FileOutputStream(articleFile);
                ObjectOutputStream oos=new ObjectOutputStream(fos);
        ) {
            Article article=new Article(title,author,content);
            oos.writeObject(article);
            response.sendRediect("/myweb/writeArticle_success.html");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("處理發表文章結束");
    }
}
