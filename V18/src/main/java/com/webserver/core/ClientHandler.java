package com.webserver.core;

import com.webserver.http.EmptyRequestException;
import com.webserver.http.HttpContext;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.servlet.LoginServlet;
import com.webserver.servlet.RegServlet;
import com.webserver.servlet.ShowAllUserServlet;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


/**
 * 负责与指定客户端进行HTTP交互
 * HTTP协议要求客户端的交互规则采取一问一答的方式。因此，处理客户端交互以3步形式完成：
 * 1.解析请求
 * 2.处理请求
 * 3.发送响应
 * @author Rookie YU
 * @create 2021-02-18 9:38
 */
public class ClientHandler implements Runnable{
    private Socket socket;
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {

        try {
            //1.解析请求
            HttpRequest httpRequest = new HttpRequest(socket);
            HttpResponse httpResponse = new HttpResponse(socket);
            //2处理请求
            //首先通过request获取请求中的抽象路径中的请求部分
            String path = httpRequest.getRequestURI();

            System.out.println("path:"+path);
            switch (path){
                case "/myweb/regUser"://前往注册
                    System.out.println("ClientHandler:正在注册..");
                    RegServlet regServlet = new RegServlet();
                    regServlet.service(httpRequest,httpResponse);
                    break;
                case "/myweb/loginUser"://前往登录
                    System.out.println("ClientHandler:正在登录...");
                    LoginServlet login = new LoginServlet();
                    login.service(httpRequest, httpResponse);
                    break;
                case "/myweb/showAllUser"://显示用户列表
                    System.out.println("ClientHandler:正在显示用户列表...");
                    ShowAllUserServlet show = new ShowAllUserServlet();
                    show.service(httpRequest, httpResponse);
                    break;
                default:
                    File file = new File("./webapps" + path);

                    System.out.println("正在寻找指定资源...");
                    if (file.exists() && file.isFile()) {
                        System.out.println("该资源已找到:" + file.getName());
                        httpResponse.setEntity(file);

                    } else {
                        System.out.println("该资源不存在!");
                        file = new File("./webapps/root/404.html");
                        httpResponse.setStatusCode(404);
                        httpResponse.setStatusReason("FileNotFound");
                        httpResponse.setEntity(file);
                    }
                    break;
            }

            //统一发送
            httpResponse.putHeader("Server","WebServer");
            //3发送响应
            httpResponse.flush();
            System.out.println("响应完毕");
        }catch (EmptyRequestException ignored){}
        catch (Exception e) {
            e.printStackTrace();
        }finally{
            //处理完毕后与客户端断开连接
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
