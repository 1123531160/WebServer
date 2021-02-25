package com.webserver.core;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
            //2处理请求
            //首先通过request获取请求中的抽象路径
            String path = "./webapps"+httpRequest.getUri();
            File file = new File(path);
            OutputStream out = socket.getOutputStream();
            HttpResponse httpResponse = new HttpResponse(socket);
            /*
                一个响应的大致内容:
                HTTP/1.1 200 OK(CRLF)
                Content-Type: text/html(CRLF)
                Content-Length: 2546(CRLF)(CRLF)
                1011101010101010101...
             */
            System.out.println("正在寻找指定资源...");
            if (file.exists() && file.isFile()) {
                System.out.println("该资源已找到...");
                httpResponse.setEntity(file);
            }else {
                System.out.println("该资源不存在!");
               file = new File("./webapps/root/404.html");
               httpResponse.setEntity(file);
               httpResponse.setStatusCode(404);
               httpResponse.setStatusReason("FileNotFound");
            }
            //3发送响应
            httpResponse.flush();
            System.out.println("响应完毕");
        } catch (Exception e) {
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
