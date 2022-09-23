package com.webserver.controller;

import com.webserver.annotation.Controller;
import com.webserver.annotation.RequestMapping;
import com.webserver.core.ClientHandler;
import com.webserver.entity.User;
import com.webserver.http.HttpServletRequest;
import com.webserver.http.HttpServletResponse;

import java.io.*;
import java.lang.annotation.Retention;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/*
 * MVC模型
 * M model
 * V view
 * C controller
 * 處理與用戶相關的業務操作
 */
@Controller
public class UserController {
    //該目錄用於保存所有的用戶信息
    private static File USER_DIR = new File("./user");

    static {
        //如果目录不存在就创建目录
        if (!USER_DIR.exists()) {//!USER_DIR.exists()=true USER_DIR.exists()=false
            USER_DIR.mkdirs();
        }
    }
    /*
     * 處理用戶註冊
     */
    @RequestMapping("/myweb/reg")
    public void reg(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("開始處理用戶註冊....");
        //實現了用戶註冊的功能
        //1.獲取用戶通過表單提交上來的數據
        //可以利用表單對應的name屬性的參數名獲取提交的參數值
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String nickname = request.getParameter("nickname");
        String age = request.getParameter("age");
        System.out.println(
                "用戶提交的參數" + username+
                "，" + password +
                "，" + nickname +
                "，" + age);
        //表單錄入內容不能為空，並且年齡輸入的內容必須是包含的內容都是整數的字符串
        if(username==null||password==null||nickname==null||
                age==null||!age.matches("[0-9]+")){
            //將註冊失敗的頁面返回給瀏覽器
            response.sendRediect("/myweb/reg_fail.html");
            //如果進入當前判斷，說明錄入信息有問題，不允許繼續向下執行程序了
            return;
        }
        //2.將用戶信息封裝到一個User對象，並序列化到文件中
        User user = new User(username,password,nickname,Integer.parseInt(age));
        //將用戶信息以用戶名.obj的形式存儲到users目錄中
        File userFile = new File(USER_DIR, username + ".obj");
        //判斷當前文件是否已存在，如果存在，可以判斷是重複用戶
        if(userFile.exists()){
            response.sendRediect("/myweb/reg_have_user.html");
            return;
        }
        try (
                FileOutputStream fos = new FileOutputStream(userFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            //将user对象序列化到userFile这个File对象所绑定的文件
            oos.writeObject(user);
            //註冊成功
            response.sendRediect("/myweb/reg_success.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("處理用戶註冊完畢!!!");
    }
    //用於處理登入功能
    @RequestMapping("/myweb/login")
    public void login(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("開始處理用戶登入....");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println(username + "," + password);
        if(username==null||password==null){
            response.sendRediect("/myweb/login_info_error.html");
            return;
        }
        //2
        File userFile = new File(USER_DIR,username+".obj");
        if(userFile.exists()){//用户名输入正确
            try (
                    FileInputStream fis = new FileInputStream(userFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ){
                //读取该注册用户信息
                User user = (User)ois.readObject();
                //如果用戶提交的密碼和註冊用戶紀錄的密碼相同，則允許登入
                if(user.getPassword().equals(password)){//密码正确
                    //登入成功
                    response.sendRediect("/myweb/login_success.html");
                    return;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        //如果程序走到這裡，用戶名或密碼有誤
        response.sendRediect("/myweb/login_fail.html");
        System.out.println("處理用戶登入完畢!!!");
    }
    /*
     * 生成顯示所有用戶信息的動態頁面
     */
    @RequestMapping("/myweb/showAllUser")
    public void showAllUser(HttpServletRequest request,HttpServletResponse response){
        System.out.println("生成動態頁面...");
        //將Users目錄下所有的obj文件進行反序列化，並將得到的User對象存入一個List集合中備用
        List<User> userlist=new ArrayList<>();
        //將Users下所有的.obj文件獲取出來
        File[] subs = USER_DIR.listFiles(f -> f.getName().endsWith(".obj"));
        //遍歷每一個.obj文件並進行反序列化得到User對象
        for (File userFile : subs) {
            try(
                FileInputStream fis = new FileInputStream(userFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
            ){
                User user=(User) ois.readObject();
                userlist.add(user);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        //生成頁面
        File file = new File("./userList.html");
        try(
            PrintWriter pw = new PrintWriter(file, "UTF-8");
        ) {
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"en\">");
            pw.println("<head>");
            pw.println("<meta charset=\"UTF-8\">");
            pw.println("<title>用戶列表</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<center>");
            pw.println("<table border=\"1\">");
            pw.println("<tr>");
            pw.println("<td>用戶名</td>");
            pw.println("<td>密碼</td>");
            pw.println("<td>暱稱</td>");
            pw.println("<td>年齡</td>");
            pw.println("</tr>");
            for (User user : userlist) {
                pw.println("<tr>");
                pw.println("<td>"+user.getUsername()+"</td>");
                pw.println("<td>"+user.getPassword()+"</td>");
                pw.println("<td>"+user.getNickname()+"</td>");
                pw.println("<td>"+user.getAge()+"</td>");
                pw.println("</tr>");
            }
            pw.println("</table>");
            pw.println("</center>");
            pw.println("</body>");
            pw.println("</html>");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //將頁面作為響應正文發送給瀏覽器
        response.setContentFile(file);
        System.out.println("生成動態頁面完畢!!");
    }
}
