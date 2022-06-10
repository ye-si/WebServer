package com.webserver.controller;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.core.ClientHandler;
import com.webserver.entity.Article;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

/**
 * 处理与文章相关的业务
 */
@Controller
public class ArticleController {
    /**
     * 该目录用于保存所有文章信息
     */
    private static File ARTICLE_DIR = new File("./articles");

    static{
        if(!ARTICLE_DIR.exists()){
            ARTICLE_DIR.mkdirs();
        }
    }

    @RequestMapping("/myweb/writeArticle")
    public void writeArticle(HttpServletRequest request, HttpServletResponse response){
        //获取表单信息
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");
        if(title==null||author==null||content==null){
            //跳错误页面
            response.sendRedirect("/myweb/writeArticle_info_error.html");
            return;
        }

        File articleFile = new File(ARTICLE_DIR,title+".obj");

        try(
            FileOutputStream fos = new FileOutputStream(articleFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
        ){
            Article article = new Article(title,author,content);
            oos.writeObject(article);

            response.sendRedirect("/myweb/writeArticle_success.html");
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    @RequestMapping("/myweb/showAllArticle")
    public void showAllArticle(HttpServletRequest request,HttpServletResponse response){
        System.out.println("开始生成动态页面");

        System.out.println("动态页面生成完毕!");
    }
}





