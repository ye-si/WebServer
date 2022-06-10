package com.webserver.core;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.controller.ArticleController;
import com.webserver.controller.UserController;
import com.webserver.http.HttpContext;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理请求的环节
 */
public class DispatcherServlet {
    //表示sources下的static目录，实际运行时是编译后target/classes下的static目录。
    private static File staticDir;

    static {
        try {
            staticDir = new File(
                    ClientHandler.class.getClassLoader().getResource(
                            "./static"
                    ).toURI()
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();
        System.out.println("请求路径:" + path);
        //判断该请求是否为请求某个业务
        HandlerMapping.MethodMapping methodMapping
                                = HandlerMapping.getMethod(path);
        if(methodMapping!=null){//该请求为请求一个业务
            Object controller = methodMapping.getController();
            Method method = methodMapping.getMethod();
            try {
                method.invoke(controller,request,response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        //去static目录下根据用户请求的抽象路径定位下面的文件
        File file = new File(staticDir, path);
        if (file.isFile()) {//实际存在的文件
            response.setContentFile(file);
        } else {//1:文件不存在  2:是一个目录
            response.setStatusCode(404);
            response.setStatusReason("NotFound");
            file = new File(staticDir, "/root/404.html");
            response.setContentFile(file);
        }

    }
}
