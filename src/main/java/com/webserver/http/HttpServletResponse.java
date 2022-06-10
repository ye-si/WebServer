package com.webserver.http;

import static com.webserver.http.HttpContext.CR;
import static com.webserver.http.HttpContext.LF;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 响应对象
 * 该类的每一个实例用于表示一个HTTP协议规定的响应内容。
 * 每个响应由三部分构成:
 * 状态行，响应头，响应正文
 */
public class HttpServletResponse {
    //状态行相关信息
    private int statusCode = 200;//状态代码
    private String statusReason = "OK";//状态描述

    //响应头相关信息
    //key:响应头的名字 value:响应头对应的值
    private Map<String,String> headers = new HashMap<>();

    //响应正文的相关信息
    private File contentFile;
    /*
        使用字节数组输出流中的字节数组作为正文内容
        java.io.ByteArrayOutputStream是一个低级流，其内部维护一个字节数组
        通过这个流写出的数据全部存入该数组中。
     */
    private ByteArrayOutputStream baos;
    private byte[] contentData;//保存动态数据的(数据从baos里获取的)

    private Socket socket;

    public HttpServletResponse(Socket socket){
        this.socket = socket;
    }

    /**
     * 发送响应
     * 将当前响应对象内容按照标准的响应格式发送给客户端
     */
    public void response() throws IOException {
        //发送前的准备工作
        sendBefore();
        //3.1发送状态行
        sendStatusLine();
        //3.2发送响应头
        sendHeaders();
        //3.3发送响应正文
        sendContent();
    }
    /**
     * 响应发送前的准备工作
     */
    private void sendBefore(){
        if(baos!=null){//不为null说明处理请求的环节用过过它写出过动态数据
            //获取到baos内部的字节数组(获取所用之前通过这个流写出的字节)
            contentData = baos.toByteArray();
            //根据该数组的长度设置响应头Content-Length
            addHeader("Content-Length",contentData.length+"");
        }
    }


    private void sendStatusLine() throws IOException {
        String line = "HTTP/1.1"+" "+statusCode+" "+statusReason;
        println(line);
        System.out.println("发送状态行:"+line);
    }
    private void sendHeaders() throws IOException {
        //遍历headers将所有响应头发送给浏览器
        Set<Map.Entry<String,String>> entrySet = headers.entrySet();
        for(Map.Entry<String,String> e : entrySet){
            String name = e.getKey();//获取响应头的名字
            String value = e.getValue();//获取响应头对应的值
            //Content-Type: text/html
            String line = name + ": " + value;
            println(line);
            System.out.println("响应头:"+line);
        }

        //单独发送回车+换行表示响应头部分发送完毕
        println("");
    }
    private void sendContent() throws IOException {
        OutputStream out = socket.getOutputStream();
        if(contentData!=null){//有动态数据
            out.write(contentData);
        }else if(contentFile!=null) {
            byte[] buf = new byte[1024 * 10];
            int len;
            try (
                    FileInputStream fis = new FileInputStream(contentFile);
            ) {
                while ((len = fis.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            }
        }
        System.out.println("响应正文发送完毕!");
    }


    private void println(String line) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] data = line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);
        out.write(CR);//发送回车符
        out.write(LF);//发送换行符
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
        //获取文件的后缀名   image.png
        String fileName = contentFile.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".")+1);
        //根据后缀名提取对应的mime类型
        String mime = HttpContext.getMimeType(ext);
        addHeader("Content-Type",mime);
        addHeader("Content-Length",contentFile.length()+"");
    }

    /**
     * 添加一个要发送的响应头
     * @param name
     * @param value
     */
    public void addHeader(String name,String value){
        this.headers.put(name,value);
    }

    /**
     * 返回一个字节输出流，通过该输出流写出的字节最终都会作为正文发送给浏览器
     * @return
     */
    public OutputStream getOutputStream(){
        if(baos==null){
            baos = new ByteArrayOutputStream();
        }
        return baos;
    }

    public PrintWriter getWriter(){
        return new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                getOutputStream(),//baos
                                StandardCharsets.UTF_8
                        )
                ),true
        );
    }

    /**
     * 设置响应头Content-Type
     * @param mime
     */
    public void setContentType(String mime){
        addHeader("Content-Type",mime);
    }

    /**
     * 要求客户端重定向到指定路径
     * @param uri
     */
    public void sendRedirect(String uri){
        /*
            重定向的响应中，状态代码为302
            并且应当包含一个响应头Location,用来指定浏览器需要重定向的路径
         */
        //修改状态代码
        statusCode = 302;
        statusReason = "Moved Temporarily";

        //响应头
        addHeader("Location",uri);

    }
}
