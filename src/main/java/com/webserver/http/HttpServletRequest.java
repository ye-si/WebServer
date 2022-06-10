package com.webserver.http;

import static com.webserver.http.HttpContext.CR;
import static com.webserver.http.HttpContext.LF;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于表示HTTP协议规定的客户端发送过来的一个请求内容。
 * 每个请求由三部分构成:
 * 请求行，消息头，消息正文
 */
public class HttpServletRequest {
    //请求行的相关信息
    private String method;//请求方式
    private String uri;//抽象路径
    private String protocol;//协议

    private String requestURI;//存uri中"?"左侧的请求部分
    private String queryString;//存uri中"?"右侧的参数部分
    //存客户端提交上来的每一组参数
    private Map<String,String> parameters = new HashMap<>();

    //消息头相关信息
    private Map<String,String> headers = new HashMap<>();

    private Socket socket;
    /**
     * 实例化请求对象的过程也是解析的过程
     */
    public HttpServletRequest(Socket socket) throws IOException, EmptyRequestException {
        this.socket = socket;
        //1.1解析请求行
        parseRequestLine();
        //1.2解析消息头
        parseHeaders();
        //1.3解析消息正文
        parseContent();

    }

    /**
     *  解析请求行
     */
    private void parseRequestLine() throws IOException, EmptyRequestException {
        String line = readLine();
        //如果请求行是一个空字符串则说明本次是空请求
        if(line.isEmpty()){
            //对外抛出空请求异常
            throw new EmptyRequestException();
        }
        System.out.println(line);
        //将请求行内容拆分出来并分别赋值给三个变量
        String[] data = line.split("\\s");
        method = data[0];
        uri = data[1];
        protocol = data[2];

        parseUri();//进一步解析uri

        //测试路径:http://localhost:8088/myweb/index.html
        System.out.println("method:"+method);//method:GET
        System.out.println("uri:"+uri);//uri:/myweb/index.html
        System.out.println("protocol:"+protocol);//protocol:HTTP/1.1
    }

    /**
     * 进一步解析uri
     */
    private void parseUri(){
        /*
            uri是有两种情况的，1:不含有参数的 2:含有参数的
            例如:
            不含有参数的:/myweb/reg.html
            含有参数的:/myweb/reg?username=fanchuanqi&password=123456&nickname=chuanqi&age=22

            处理方式:
            1:若不含有参数，则直接将uri的值赋值给requestURI
            2:若含有参数
              2.1:先将uri按照"?"拆分为请求部分和参数部分
                  将请求部分赋值给requestURI
                  将参数部分赋值给queryString
              2.2:再将参数部分按照"&"拆分出每一组参数
                  每组参数再按照"="拆分为参数名和参数值
                  并将参数名作为key，参数值作为value保存到parameters这个Map中

              允许页面输入框空着，这种情况该参数的值为null存入parameters即可
         */

//        [/myweb/reg, username=fanchuanqi&password=123456&nickname=chuanqi&age=22]
        String[] data = uri.split("\\?");
        requestURI = data[0];
        if(data.length>1){
            //username=fanchuanqi&password=&nickname=chuanqi&age=22
            queryString = data[1];
            //[username=fanchuanqi, password=, nickname=chuanqi, age=22]
            parseParameters(queryString);
        }


        System.out.println("requestURI:"+requestURI);
        System.out.println("queryString:"+queryString);
        System.out.println("parameters:"+parameters);
    }

    /**
     * 解析参数
     * @param line 格式应当为:name1=value1&name2=value2&...
     */
    private void parseParameters(String line){
        //先进行转码
        try {
            line = URLDecoder.decode(line,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] data = line.split("&");
        for(String para : data){
            //[password]
            String[] paras = para.split("=");
            parameters.put(paras[0],paras.length>1?paras[1]:null);
        }
    }

    /**
     * 解析消息头
     */
    private void parseHeaders() throws IOException {
        while(true) {
            String line = readLine();
            if(line.isEmpty()){//若读取的字符串为空串，说明单独读取了回车+换行
                break;
            }
            System.out.println("消息头:" + line);
            //将消息头的名字和值以key，value形式存入headers这个Map中
            String[] data = line.split(":\\s");
            headers.put(data[0],data[1]);
        }
        System.out.println("headers:"+headers);
    }

    /**
     * 解析消息正文
     */
    private void parseContent() throws IOException {
        System.out.println("开始解析消息正文...");
        //通过判断请求中的消息头是否包含Content-Length来判定是否有正文
        if(headers.containsKey("Content-Length")){
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            System.out.println("正文长度:"+contentLength);
            //基于消息头告知的长度来创建一个字节数组，用于保存正文内容
            byte[] contentData = new byte[contentLength];
            //读取正文所有的字节存入contentData中
            InputStream in = socket.getInputStream();
            in.read(contentData);
            //获取消息头Content-Type
            String contentType = headers.get("Content-Type");
            //根据Content-Type的值判定正文类型并做对应的解析
            if("application/x-www-form-urlencoded".equals(contentType)){
                String line = new String(contentData, StandardCharsets.ISO_8859_1);
                System.out.println("正文内容:"+line);
                parseParameters(line);
            }


        }


    }


    private String readLine() throws IOException {
        InputStream in = socket.getInputStream();
        StringBuilder builder = new StringBuilder();
        int d;
        char pre='a',cur='a';//pre上一次读取的字符  cur本次读取到的字符
        while((d = in.read())!=-1){
            cur = (char)d;//将本地读取的字节转换为字符赋值给cur
            if(pre==CR&cur==LF){//是否连续读取到了回车+换行
                break;
            }
            builder.append(cur);//将本次读取的字符拼接到StringBuilder中
            pre = cur;//在进行下一个字符读取前将本地读取的字符记录为上次读取的字符
        }
        return builder.toString().trim();
    }


    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    /**
     * 根据给定的消息头的名字获取对应的值
     * @param name
     * @return
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     * 根据给定的参数名获取对应的参数值
     * @param name
     * @return
     */
    public String getParameter(String name){
        return parameters.get(name);
    }
}
