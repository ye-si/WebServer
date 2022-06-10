package com.webserver.core;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 用来维护所有请求路径与对应的Controller中的处理方法
 */
public class HandlerMapping {
    private static Map<String,MethodMapping> mapping = new HashMap<>();

    static {
        initMapping();
    }

    private static void initMapping(){
        try {
            File dir = new File(
                    HandlerMapping.class.getClassLoader().getResource(
                            "./com/webserver/controller"
                    ).toURI()
            );
            File[] subs = dir.listFiles(f -> f.getName().endsWith(".class"));
            for (File sub : subs) {
                String fileName = sub.getName();
                String className = fileName.substring(0, fileName.indexOf("."));
                Class cls = Class.forName("com.webserver.controller." + className);
                //判断该类是否被@Controller标注了
                if (cls.isAnnotationPresent(Controller.class)) {
                    Object o = cls.newInstance();//将该Controller实例化
                    Method[] methods = cls.getDeclaredMethods();
                    for (Method method : methods) {
                        //判断该方法是否被@RequestMapping标注了
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            //获取该注解
                            RequestMapping rm = method.getAnnotation(RequestMapping.class);
                            //获取该注解的参数(该方法处理的请求路径)
                            String path = rm.value();
                            MethodMapping methodMapping = new MethodMapping(o,method);
                            System.out.println("扫描的方法:"+method.getName()+",处理的请求:"+path);
                            mapping.put(path,methodMapping);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据请求路径返回对应的处理方法
     * @param path
     * @return
     */
    public static MethodMapping getMethod(String path){
        return mapping.get(path);
    }

    public static class MethodMapping{
        private Object controller;
        private Method method;

        public MethodMapping(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            this.controller = controller;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }
    }
}
