package com.webserver.servlet;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * 当前servlet用于处理登录业务
 * @author Rookie YU
 * @create 2021-02-24 16:46
 */
public class LoginServlet extends HttpServlet{
    private static Logger log = Logger.getLogger(LoginServlet.class);
    @Override
    public void doGet(HttpRequest request, HttpResponse response) {

    }

    public void doPost(HttpRequest request, HttpResponse response){
        //info是用来记录一般信息
        log.info("LoginServlet:开始处理登录...");
        //1.通过request获取用户在登录页面上输入的登录信息(表单上的信息)
        String user = request.getParameter("user");
        String password = request.getParameter("password");


        if(user == null || password ==null){
            File file = new File("./webapps/myweb/login_fail.html");
            response.setEntity(file);
            return;
        }
        byte[] data = new byte[32];
        try(
               RandomAccessFile raf = new RandomAccessFile("user.dat","r")
                ){

            for (int i = 0; i < raf.length()/100; raf.seek(++i*100)) {
                raf.read(data);
                String useName = new String(data, StandardCharsets.UTF_8).trim();
                if (useName.equals(user)){
                    raf.read(data);
                    String passWord = new String(data, StandardCharsets.UTF_8).trim();
                    if (passWord.equals(password)){
                        //登录成昆
                        File file = new File("./webapps/myweb/login_success.html");
                        response.setEntity(file);
                        return;
                    }
                    break;
                }
            }

            //统一处理登录失败
            File file = new File("./webapps/myweb/login_fail.html");
            response.setEntity(file);



        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("LoginServlet:登录处理完毕!");
    }
}
