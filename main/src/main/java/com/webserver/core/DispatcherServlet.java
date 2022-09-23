package com.webserver.core;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.controller.ArticleController;
import com.webserver.controller.UserController;
import com.webserver.http.HttpContext;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * 處理請求的環節
 */
public class DispatcherServlet {
    private static File staticDir;

    static { //類加載時，會最先執行靜態代碼塊的內容，並且只執行一次
        try {
            staticDir = new File(
                    ClientHandler.class.getClassLoader().getResource(
                            "./static"
                    ).toURI()
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void service(HttpServletRequest request, HttpServletResponse response) {
        /*
         * 由於URL中的抽象路徑此處正好可以匹配我們的服務器資源
         * http://localhost:8088/myweb/classTable.html
         * 抽象路徑:/myweb.classTable.html,可以去項目的static 目錄下尋找/myweb/classTable.html
         * http://localhost:8088/myweb/index.html
         * 抽象路徑:/myweb/index.html,可以去項目的static目錄下尋找/myweb/index.html
         */
        //本次請求的抽象路徑
        String path = request.getRequestURI();
        System.out.println("抽象路徑:" + path);
        //判斷該請求是否是一個業務請求
        /*
         * 掃描com.webserver.controller包下所有的類，查看那些類貝@Controller註解修飾了，
         * 貝修飾的類就是業務類，其中的方法是業務方法，可以貝調用，
         * 並獲取該業務類中的所有方法，並判斷哪個方法上貝@RequestMapping註解修飾了，
         * 貝修飾的方法就是業務方法，然後獲取該註解中的值，和本次請求的path進行比較，
         * 如果相同，就說明是本次請求的業務方法，就直接利用反射調用即可
         * 舉例子:
         * 訪問的URL:http://localhost:8088/myweb/reg?username=laoan&password=123
         * 進行URL的解析後，傳入DispatcherServlet中的path值是:/myweb/reg
         * 對應的業務方法是UserController中的reg方法，這個方法上會標註該註解:
         * @RequestMapping("/myweb/reg")
         */
        try {
            //定位controller目錄
            File dir = new File(DispatcherServlet.class.getClassLoader().getResource("./com/webserver/controller").toURI());
            //掃描這個目錄，過濾出其中class文件
            File subs[] = dir.listFiles(f -> f.getName().endsWith(".class"));
            for (File sub : subs) {
                String fileName = sub.getName();//獲取文件名 UserController.class
                String className = fileName.substring(0, fileName.indexOf("."));//獲取類明UserController
                //利用全路徑獲取該Class實例
                Class cls = Class.forName("com.webserver.controller." + className);
                //判斷該類是否被@Controller註解修飾
                if (cls.isAnnotationPresent(Controller.class)) {
                    //如果被修飾說明是業務類，然後獲取所有的業務方法
                    Method methods[] = cls.getDeclaredMethods();
                    //遍歷每一個方法，並且判斷該方法上是否被@RequestMapping註解修飾了
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            //如果被修飾，說明是業務方法，獲取註解中的參數，進行和path的匹配
                            RequestMapping anno = method.getAnnotation(RequestMapping.class);
                            String value = anno.value();
                            if (path.equals(value)) {
                                //實例化業務
                                Object o = cls.newInstance();
                                //執行該方法
                                method.invoke(o, request, response);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //去static 目錄下尋找用戶請求的資源文件
        //new File(父級目錄，子級目錄)從父級目錄尋找子級目錄
        //如果子級文件不存在，會返回null
        File file = new File(staticDir, path);
        if (file.isFile()) {//實際存在的文件
            //由於此處是正常處理，所以無需設置狀態行，但是需要設置響應正文
            response.setContentFile(file);
        } else {//1:文件不存在2:是一個目錄
            response.setStatusCode(404);
            response.setStatusReason("NotFound");
            file = new File(staticDir, "/root/404.html");
            response.setContentFile(file);
        }
    }
}
