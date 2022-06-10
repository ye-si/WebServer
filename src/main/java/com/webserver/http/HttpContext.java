package com.webserver.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 当前类用于定义所有HTTP协议规定之内容，可被复用。
 */
public class HttpContext {
    /**
     * 回车符
     */
    public static final char CR = 13;
    /**
     * 换行符
     */
    public static final char LF = 10;

    /**
     * 资源后缀与MIME类型的对应关系
     * key:资源的后缀名 例如:png
     * value:MIME类型  例如:image/png
     */
    private static Map<String,String> mimeMapping = new HashMap<>();

    static{
        initMimeMapping();
    }

    private static void initMimeMapping(){
        /*
            java.util.Properties
            该类专门用于解析.properties文件的
            Properties本身是一个Map。
            Properties继承自Hashtable.Hashtable实现了Map接口，它是一个
            并发安全的Map。而HashMap不是并发安全的。
         */
        Properties properties = new Properties();
        //读取和当前类HttpContext在同一个目录下的web.properties
        /*
            两个实际开发中常用的相对路径区别:
            类名.class.getClassLoader().getResource(".")
            这里的"."当前目录，指的是该类所在的包中顶级包的上一级，即:"根"
            例如
            HttpContext类，指定的包package com.webserver.http
            那么
            HttpContext.class.getClassLoader().getResource(".")
            对应的目录就是com的上一级。因为IDEA中编译后，代码都在target/classes
            下，所以这里就相当于是classes这个目录。

            类名.class.getResource(".")
            这里的"."当前目录，指的是当前类所在的目录
            例如
            HttpContext类，指定的包package com.webserver.http
            那么
            HttpContext.class.getResource(".")
            对应的目录就是http目录。因为IDEA中编译后，当前类在:
            target/classes/com/webserver/http下所以这里就相当于是
            target/classes/com/webserver/http这个目录。
         */
        try {
            properties.load(
                    HttpContext.class.getResourceAsStream(
                            "./web.properties"
                    )
            );
            properties.forEach(
                    (k,v)->mimeMapping.put(k.toString(),v.toString())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据资源后缀名获取对应的MIME类型
     * @param ext
     * @return
     */
    public static String getMimeType(String ext){
        return mimeMapping.get(ext);
    }

    public static void main(String[] args) {
        System.out.println(getMimeType("png"));
    }
}







