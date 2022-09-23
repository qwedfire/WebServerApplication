package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.webserver.http.HttpContext.*;

/**
 * 響應對象
 * 該類的每一個實例用瑜表示一個HTTP協議規定的響應內容
 * 每個響應都是由三部分組成
 * 狀態行，響應行，響應正文
 */
public class HttpServletResponse {
    private Socket socket;
    //狀態行相關信息
    private int statusCode=200;//狀態碼
    private String statusReason="OK";//描述短語
    //響應頭相關信息
    private Map<String ,String> headers=new HashMap<>();
    //響應行正文相關信息
    private File contentFile;

    public HttpServletResponse(Socket socket) {
        this.socket = socket;
    }
    /*
     * 發送響應
     * 將當前響應對象內容，按照標準的格式發送給客戶端
     */
    public void response() throws IOException {
        //3.1發送狀態行
        sendStatusLine();
        //3.2發送響應頭
        sendHeaders();
        //3.3發送響應正文
        snedContent();
    }
    //發送狀態行
    private void sendStatusLine() throws IOException {
        String line="HTTP/1.1"+" "+statusCode+" "+statusReason;
        println(line);
    }
    //發送響應頭
    private void sendHeaders() throws IOException {
        //遍歷headers，將其中所有的響應頭發送給瀏覽器
        //獲取headers的所有的entry對象封裝到Set集合中
        Set<Map.Entry<String, String>> entries = headers.entrySet();
        //遍歷entrySet()集合，獲取每一key和value
        for (Map.Entry<String, String> entry : entries) {
            //獲取entry中的key值，就是響應頭的名字
            String name= entry.getKey();
            //獲取entry中的value值，就是響應頭對應的值
            String value=entry.getValue();
            //將name和value拼接成按照HTTP協議要求的響應頭的格式
            //此處拼接時，一定要拼接的是冒號+空格!!!!!!
            String line=name+": "+value;
            println(line);
            System.out.println("發送的響應頭是:"+line);
        }
        //單獨發送回車+換行，表示響應頭結束
        println("");
    }
    //發送響應正文
    private void snedContent() throws IOException {
        OutputStream out=socket.getOutputStream();
        if(contentFile!=null) {
            byte[] buf = new byte[10 * 1024];//定義緩衝區，10kb，加速讀取的速率
            int len;//存儲每次讀取的字節量
            try (
                    FileInputStream fis = new FileInputStream(contentFile);//定義字節輸入流，用於讀取頁面資源
            ) {
                //每次讀取10kb的資源，如果讀取到了，就返回實際讀取的自量，並且將讀取的數據存儲到buf
                //如果讀取不到內容，說明讀取完畢了，就返回-1
                while ((len = fis.read(buf)) != -1) {
                    //像瀏覽器發送讀取的內容，讀到多少字節量，就發送多少字節量的內容
                    out.write(buf, 0, len);
                }
            }
        }
    }
    //向訪問的客戶端發送一行字串符
    public void println(String line) throws IOException {
        OutputStream out=socket.getOutputStream();
        byte[] data=line.getBytes(StandardCharsets.ISO_8859_1);
        out.write(data);
        out.write(CR);//回車符
        out.write(LF);//換行符
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public File getContentFile() {
        return contentFile;
    }

    public void setContentFile(File contentFile) {
        this.contentFile = contentFile;
        String fileName=contentFile.getName();//獲取文件名
        String ext=fileName.substring(fileName.lastIndexOf(".")+1);//獲取後綴名
        String mime= HttpContext.getMimeType(ext);
        addHeader("Content-Type",mime);
        addHeader("Content-Length",contentFile.length()+"");
    }
    /*
     * 添加一個要發送響應頭的方法
     */
    public void addHeader(String name,String value){
        this.headers.put(name,value);
    }

    /*
     * 要求客戶端重定向到指定的路徑
     */
    public void sendRediect(String uri){
        /**
         * 重定向的響應中，狀態代碼為302
         * 並且應該包含一個響應頭Location，
         * 用來指定瀏覽器需要重定向的路徑
         */
        statusCode=302;
        statusReason="Moved Temporarily";
        addHeader("Location",uri);
    }
}
