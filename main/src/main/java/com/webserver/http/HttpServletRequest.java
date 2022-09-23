package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.webserver.http.HttpContext.*;


/**
 * 請求對象
 * 該類的每一個實例都用於表示HTTP協議規定的客戶端發送過來的一個請求內容
 * 每個請求由三部分組成
 * 請求行 消息頭 消息正文
 */
public class HttpServletRequest {
    private Socket socket;
    //請求行的相關信息
    private String method;//請求方式
    private String uri;//抽象路徑(純靜態資源路徑 含有參數部分的路徑)
    private String protocol;//協議版本
    private String requestURI;//存uri中"?"左側請求部分
    private String queryString;//存uri中"?"右側請求部分
    //存儲消息頭的相關信息
    private Map<String,String>parameters=new HashMap<>();
    //存儲消息頭的相關消息
    private Map<String,String> headers=new HashMap<>();
    /*
     *  實例化請求對象的過程也是解析的過程
     */
    public HttpServletRequest(Socket socket) throws IOException, EmptyRequestException {
        this.socket=socket;
        //1.1解析請求行
        parseRequestLine();
        //1.2解析消息頭
        parseHeaders();
        //1.3解析消息正文
        parseContent();
    }

    /*
     * 解析請求行
     */
    private void parseRequestLine() throws IOException,EmptyRequestException {
        String line=readLine();
        //如果此處的line是空值，說明是空請求，對外拋出異常
        if(line.isEmpty()){
            throw new EmptyRequestException();
        }
        System.out.println("請求行"+line);
        //"GET(SP)/myweb/index.html(SP)HTTP/1.1"-->[GET,/myweb/index.html,HTTP/1.1]
        String data[] = line.split("\\s");//使用正則表達式,根據空格將讀取的內容拆分
        method = data[0];
        uri = data[1];
        protocol = data[2];
        //進一步解析uri
        parseUri();
        System.out.println("請求方式:" + method);
        System.out.println("抽象路徑:" + uri);
        System.out.println("協議版本:" + protocol);
    }

    /*
     * 進一步解析uri
     */
    private void parseUri(){
        String[] data = uri.split("\\?");
        requestURI=data[0];
        if(data.length>1){
            queryString=data[1];
            parseParameters(queryString);
        }
        System.out.println("請求部分"+requestURI);
        System.out.println("參數部分"+queryString);
        System.out.println("參數Map"+parameters);
    }

    /*
     * 解析參數
     */
    private void parseParameters(String line){
        //先進行轉碼 %E8%80%81%E7%8E%8B>>>老王
        try {
            line = URLDecoder.decode(line, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] data = line.split("&");
        for (String para : data) {
            String[] paras = para.split("=");
            parameters.put(paras[0], paras.length > 1 ? paras[1] : null);
        }
    }
    /*
     * 解析消息頭
     */
    private void parseHeaders() throws IOException {
        while(true){
            String line=readLine();
            //如果本次讀取的字符串是一個空字符串，說明讀取了回車+換行
            //if(line.length()==0){
            //if("".equals(line)){
            if(line.isEmpty()){
                break;
            }
            //將消息頭的名字和值已key,value的形式存儲到headlrs這個Map中
            //Host: localhost:8088 -> ["Host","localhost:8088"]
            String[] data=line.split(":\\s"); //"\\s"==" "這裡為了方便觀看寫成\\s的寫法
            headers.put(data[0],data[1]);
        }
        headers.forEach(
                (k,v)-> System.out.println("消息頭的名字:"+k+"消息頭的值:"+v)
        );
    }

    /*
     * 解析消息正文
     */
    private void parseContent() throws IOException {
        System.out.println("開始解析消息正文.....");
        //通過判斷消息頭中是否包含Content-Length來判斷是否有正文
        if(headers.containsKey("Content-Length")){
            String length=headers.get("Content-Length");
            int contentLength=Integer.parseInt(length);
            System.out.println("Content-Length消息正文長度:"+contentLength);
            //基於消息頭告知的長度來創建一個自結數組，用於保存正文的內容
            byte[] contentData = new byte[contentLength];
            //讀取正文中所有的字節存入到contentData中
            InputStream in = socket.getInputStream();
            in.read(contentData);
            //獲取消息頭Content-Type的值
            String contentType=headers.get("Content-Type");
            //如果類型是application/x-www-form-urlencoded這個值，說明正文內容又是參數的拚:
            if("application/x-www-form-urlencoded".equals(contentType)){
                //先將contentData字節數組轉換為字符串
                String line=new String(contentData, StandardCharsets.UTF_8);
                System.out.println("消息正文內容:"+line);
                parseParameters(line);
            }
        }
    }
    //讀取一行字符串
    private String readLine() throws IOException {
        //專門用於讀取Socket代表的客戶端的信息
        InputStream in = socket.getInputStream();
        StringBuilder builder = new StringBuilder();
        char pre = 'a';//上一次讀取的字符
        char cur = 'a';//本次讀取的字符
        int d;
        while ((d = in.read()) != -1) { //讀取不到內容的時候，返回-1
            //1.將讀到的字符整數d賦值給cur
            cur = (char) d;
            //2.判斷上一次讀取的字符和本次讀取的字符是否是回車+換行
            if (pre == CR  && cur == LF) { //13是回車 10是換行
                break;
            }
            //3.將本次讀取的字符變更為上一次讀取的字符，方便繼續讀取下一個
            pre = cur;
            //4.將本次讀取的字符拼接到bulider中
            builder.append(cur);
        }
        //5.將StringBuilder對象轉換為String
        return builder.toString().trim();
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

    /*
     * 根據給定的消息頭的名字獲取對應的值
     */
    public String getHeader(String name){
        return headers.get(name);
    }

    public String getRequestURI() {
        return requestURI;
    }

    public String getQueryString() {
        return queryString;
    }
    /*
     * 根據給定的參數名獲取對應的參數值
     */
    public String getParameter(String name){
        return parameters.get(name);
    }
}
