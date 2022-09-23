package com.webserver.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 當前類用於定義所有的Http協議規定的內容，可被複用
 */
public class HttpContext {
    /*
     * 回車符
     */
    public static final char CR=13;
    /*
     * 換行符
     */
    public static final char LF=10;
    /*
     * 資源後綴與MINE類型的對應關係
     * key:資源的後綴名 例如:png
     * value:MINE類型 例如:image/png
     */
    private static Map<String,String> mimeMapping=new HashMap();
    static {
        initMapping();
    }
    private static void initMapping(){
        /*
         * java.util.Properties工具類
         * 專門用於解析.properties文件的
         * Properties本身就是一個Map，所以可以將web.poperties的文件內容
         * 讀取出來，並存儲到自身中
         */
        Properties properties=new Properties();
        //讀取和HttpContext.class文件同一下的web.properties
        try {
            /*
             * 類名.ClientHandler.class.getClassLoader().getResource(".")
             * 此處的"."定位的是當前類所在的頂級目錄的上一層，也就是target/classes
             * 類明class.getResource(".")
             * 此處的"."定位就是當前類所在目錄，也就是http
             */
            properties.load(
                    HttpContext.class.getResourceAsStream(
                            "./web.properties"
                    )
            );
            //由於Properites沒有泛型約束，所以遍歷出來的key和value都是Object類型，
            //所以存儲到mimeMapping中時，需要轉換為字符串
            properties.forEach(
                    (k,v)->mimeMapping.put(k.toString(),v.toString())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 根據資源後綴名獲取對應的MIME值
     */
    public static String getMimeType(String ext){
        return mimeMapping.get(ext);
    }
}
