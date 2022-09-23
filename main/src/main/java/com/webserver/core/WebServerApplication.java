package com.webserver.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服務器的主類  http://localhost:8088/TeduStore/index.html
 */
public class WebServerApplication {
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private WebServerApplication(){
        try{
            System.out.println("正在啟動服務器");
            serverSocket=new ServerSocket(8088);
            threadPool = Executors.newFixedThreadPool(50);
            System.out.println("已啟動");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void start(){
        try {
            while(true) {
                System.out.println("等待客戶連接");
                Socket socket = serverSocket.accept();
                System.out.println("一個客戶以連接");
                //啟動一個線程負責與該客戶端進行交互
                ClientHandler handler = new ClientHandler(socket);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WebServerApplication server = new WebServerApplication();
        server.start();
    }
}