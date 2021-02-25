package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.vo.User;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rookie YU
 * @create 2021-02-25 9:48
 */
public class ShowAllUserServlet {
    public void service(HttpRequest request, HttpResponse response){
        System.out.println("showAllUserServlet:开始处理用户列表页面...");
        //1: 先将user.dat文件中所有用户信息读取出来
        List<User> list = new ArrayList<>();//保存所有用户记录的集合
        try (
                RandomAccessFile raf = new RandomAccessFile("user.dat","r")
                ){
            byte[] data = new byte[32];
            for (int i = 0; i < raf.length() / 100; i++) {
                //读取用户名
                raf.read(data);
                String userName = new String(data, StandardCharsets.UTF_8).trim();
                //读取密码
                raf.read(data);
                String passWord = new String(data,StandardCharsets.UTF_8).trim();
                //读取昵称
                raf.read(data);
                String nickName = new String(data,StandardCharsets.UTF_8).trim();
                //读年龄
                int age = raf.readInt();
                User user = new User(userName,passWord,nickName,age);
                System.out.println(user);
                list.add(user);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        //2.使用thymeleaf将数据与静态页面userList.html结合生成动态页面
        //2.1:创建Context实例,thymeleaf提供的,用于保存所有在页面上要显示的数据
        Context context = new Context();//使用类似Map
        //将存放所有用户信息的List集合存入Context
        context.setVariable("list",list);//类似Map的key,value

        //2.2初始化thymeleaf模板引擎
        //模板解释器,用来告知模板引擎模板的相关信息(模板就是要结合的静态页面)
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setTemplateMode("html");//模板是html
        resolver.setCharacterEncoding(String.valueOf(StandardCharsets.UTF_8));//模板使用的字符集

        //实例化模板引擎
        TemplateEngine te = new TemplateEngine();
        //将模板解释器设置给引擎,这样它就能了解模板的相关信息了
        te.setTemplateResolver(resolver);


        //2.3利用模板引擎将数据与静态页面结合,生成动态页面
        /*
            process方法用于生成动态页面
            参数1:模板位置(静态页面的位置)
            参数2:要在页面上显示的动态数据
            返回值:生成好的动态页面源代码
         */
        String result = te.process("./webapps/myweb/userList.html", context);
        System.out.println(result);

        System.out.println("页面生成完毕!");
        PrintWriter pw = response.getWriter();
        pw.println(result);
        //设置正文类型.告知浏览器它是一个页面
        response.setContentType("text/html");


        System.out.println("showAllUserServlet:用户列表页面处理完毕!");
    }
}
