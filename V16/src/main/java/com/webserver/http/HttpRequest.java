package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象
 * 该类的每一个实例用于表示客户端发送过来的一个HTTP请求内容
 * 每个请求由三部分构成:
 * 请求行,消息头,消息正文
 * @author Rookie YU
 * @create 2021-02-18 10:22
 */
public class HttpRequest {
    private Socket socket;
    //请求行相关信息
    private String method;//请求方式
    private String uri;//抽象路径
    private String protocol;//协议版本

    /*对uri的二次解析,形如:http://localhost:8088/myweb/regUser?
            user=rookie&password=123456&nickname=whatever&age=18
    */
    private String requestURI;//存抽象路径中的请求部分,即:uri中?左侧的内容
    private String queryString;//存抽象路径中的参数部分,即:uri中?右侧的内容
    private Map<String, String> parameter = new HashMap<>();//存每一组参数

    //消息头相关信息
    private Map<String, String> headers = new HashMap<>();

    //消息正文相关信息



    public HttpRequest() {
    }

    /**
     * HttpRequest的实例化过程就是解析请求的过程
     * @param socket
     */
    public HttpRequest(Socket socket) throws EmptyRequestException {
        this.socket = socket;
        //1.解析请求行
        parseRequestLine();
        //2.解析消息头
        parserHeaders();
        //3.解析消息正文
        parseContent();
    }
    //解析一个请求的的三步骤:
    //1.解析请求行
    private void parseRequestLine() throws EmptyRequestException {
        System.out.println("HttpRequest:开始解析请求行...");
        try {
            //读取请求行
            String line = readLine();
            if (line.isEmpty())
                throw new EmptyRequestException();


            System.out.println("请求行:"+line);

            //http://localhost:8088/index.html
            //将请求行按照空格拆分为三部分,并分别赋值给上述变量
            String[] data = line.split("\\s");
            method = data[0];
            uri = data[1];
            protocol = data[2];
            parseUri();//解析请求行的三部分之后,对uri抽象路径部分进行进一部分的解析工作
            System.out.println("method:"+method);//method:GET
            System.out.println("uri:"+uri);//uri:/index.html
            System.out.println("protocol:"+protocol);//protocol:HTTP/1.1
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("HttpRequest:请求行解析完毕...");
    }

    //进一步解析uri
    private void parseUri() throws UnsupportedEncodingException {
        /*
            先转换中文,将抽象路径中的%XX还原为对应的文字
         */
        uri = URLDecoder.decode(uri, "utf-8");
        /*
            uri会存在两种情况:含有参数和不含有参数
            不含有参数的样子如:/myweb/index.html
            含有参数的样子如:/myweb/regUser?user=rookie&password=123456&nickname=whatever&age=18
            因此我们要对uri进一步拆分,需求如下:
            如果uri不含有参数,则不需要拆分,直接将uri的值赋值给requestURI即可

            如果uri含有参数,则需要进行拆分:
            1:将uri按照'?'拆分为两部分,左侧赋值给requestURI,右侧赋值给queryString
            2:再将queryString部分按照'&'拆分成一组参数,然后每一组参数再按照'='拆分为
            参数名和参数值,并将参数名作为key.参数值作为value保存到parameter这个Map中
            完成解析工作.
         */
        //判断uri是否含有参数
        if (!uri.contains("?")) {
            requestURI = uri;
        }
        else {
            String[] data = uri.split("\\?");
            requestURI = data[0];
            if (data.length>1) {
                queryString = data[1];
                parseParameter(queryString);
            }
        }
        System.out.println("requestURI:"+requestURI);
        System.out.println("queryString:"+queryString);
        System.out.println("parameter:"+parameter);

    }

    /**
     * 解析参数
     * 参数的格式:name=value&name=value&...
     * GET形式和POST形式提交表单时,参数部分都是这个格式,因此解析操作被当前方法重用
     * @param line
     */
    private void parseParameter(String line){
        String[] data = line.split("&");
        for (String para : data) {
            String[] paras = para.split("=");
            if (paras.length <2)
                parameter.put(paras[0],null);
            else
                parameter.put(paras[0],paras[1]);
        }
    }

    //2.解析消息头
    private void parserHeaders(){
        System.out.println("HttpRequest:开始解析消息头...");
        try {
            //下面读取每一个消息头后,将消息头的名字作为key,消息头的值作为value保存到headers中
//            String line;

            while (true) {
                String line = readLine();
                //读取消息头时,如果只读取到了回车加换行符就应当停止读取
                if(line.isEmpty()) {//readLine单独读取CRLF返回值应当是字符串
                    break;
                }
                System.out.println("消息头:"+line);
                //将消息头按照冒号空格拆分并存入到headers这个Maps中保存
                String[] data = line.split(":\\s");
                headers.put(data[0],data[1]);
            }
            System.out.println("headers:"+headers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("HttpRequest:消息头解析完毕...");

    }


    //3.解析消息正文
    private void parseContent(){
        System.out.println("HttpRequest:开始解析消息正文...");
        //POST请求会包含消息正文
        if("post".equalsIgnoreCase(method)){
            //获取消息正文的长度
            String len = headers.get("Content-Length");
            if (len != null){
                int length = Integer.parseInt(len);//将长度转化为int
                byte[] data = new byte[length];
                try {
                    InputStream in = socket.getInputStream();
                    in.read(data);//将消息正文对应的文字全部读取出来
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //根据消息头Content-Type了解浏览器发送过来的正文是什么并进行对应的处理
                String type = headers.get("Content-Type");
                if (type != null) {
                    //判断是否为form表单提交的数据
                    if ("application/x-www-form-urlencoded".equalsIgnoreCase(type)){
                        try {
                            //删除类型的正文实际上就是字符串,内容与GET提交时?右侧内容一样
                            String line = new String(data, "ISO8859-1");
                            line = URLDecoder.decode(line,"UTF-8");
                            System.out.println("消息正文:"+line);
                            parseParameter(line);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }//后期可以继续else if判断其他类型的正文并处理
                }
            }
        }
        System.out.println("HttpRequest:消息正文解析完毕...");

    }

    private String readLine() throws IOException {
        /*
            当socket对象相同时,无论调多少次getInputStream方法,获取回来的输入流
            总是同一个流.输出流也是一样的.
         */
        InputStream in = socket.getInputStream();
        int d;
        char cur =' ';//表示本次读取到的字符
        char pre =' ';//表示上次读取到的字符
        StringBuilder sb = new StringBuilder();//保存读取到的所有字符
        while ((d = in.read()) != -1){
            cur =(char) d;
            if (pre == 13 && cur ==10){
                break;
            }
            sb.append(cur);
            pre = cur;
        }
        return sb.toString().trim();
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

    public String getHeader(String name){
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    /**
     * 根据参数名获取参数值
     * @param name
     * @return valueOfInput
     */
    public String getParameter(String name){
        return parameter.get(name);
    }
}



