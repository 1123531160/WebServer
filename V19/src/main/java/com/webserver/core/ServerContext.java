package com.webserver.core;

import com.webserver.servlet.*;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前类用于保存服务端重用的一些内容
 * @author Rookie YU
 * @create 2021-02-27 14:31
 */
public class ServerContext {
    private static Map<String, HttpServlet> servletMapping = new HashMap<>();

    static {
        initServletMapping();
    }
    private static void initServletMapping(){
        /*
            解析config/servlets.xml文件,将根标签下所有名为<servlet>的标签获取到,并将其中
            属性path的值作为key
            className的值利用反射实例化对应的类并作为value
            保存到servletMapping这个Map完成初始化操作.
         */

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read("./config/servlets.xml");
            Element rootElement = document.getRootElement();
            List<Element> elementList = rootElement.elements("servlet");
            for (Element element : elementList) {
                String path = element.attributeValue("path");
                String className = element.attributeValue("className");
                Class cls = Class.forName(className);
                HttpServlet servlet = (HttpServlet) cls.newInstance();
                servletMapping.put(path,servlet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static HttpServlet getServlet(String path){
        return servletMapping.get(path);
    }

}
