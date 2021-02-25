package com.webserver.http;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前类用于保存所有与HTTP协议相关的规定内容以便重用
 * @author Rookie YU
 * @create 2021-02-22 9:15
 */
public class HttpContext {
    /**
     * 资源后缀名与响应头Content-Type值的对应关系
     * key:资源后缀名
     * value:Content-Type对应的值
     */
    private static Map<String,String> mimeMapping = new HashMap<>();

    static {
        initMimeMapping();
    }

    private static void initMimeMapping(){
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read("./config/web.xml");
            Element rootEle = document.getRootElement();
            List<Element> list = rootEle.elements("mime-mapping");
            for (Element element : list) {
                //获取<extension>中间的文本
                String key =element.elementText("extension");
                //获取<mime-type>中间的文本
                String value = element.elementText("mime-type");
                mimeMapping.put(key,value);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更具给定的 资源后缀名获取到对应Content-Type的值
     * @param extend
     * @return value of Content-Type
     */
    public static String getMimeType(String extend){
        return mimeMapping.get(extend);
    }
}
