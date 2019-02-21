package Wechaet;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 基于多线程的客户端
 *
 */
class ReadFromsever implements Runnable{
    private Socket client;
    public  ReadFromsever(Socket client){
        this.client=client;
    }

    @Override
    public void run() {
        //获取输入流来取得服务器发来的信息
        try {
            Scanner in=new Scanner(client.getInputStream());
            while(true){
                if(client.isClosed()){
                    System.out.println("客户端已经关闭");
                    in.close();
                    break;
                }
                if(in.hasNext()){
                    String msgFromSever=in.nextLine();
                    System.out.println("服务器发来的信息:"+msgFromSever);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

/**
 * 向服务器发送信息小线程
 */
class SendMsgToSever implements Runnable{
    private Socket client;
    public SendMsgToSever(Socket client){
        this.client=client;
    }

    @Override
    public void run() {
        //获取输出流，向服务器发送信息
        try {

            PrintStream printStream=new PrintStream(client.getOutputStream(),true,"UTF-8");
            //获取用户输入
            Scanner in =new Scanner(System.in);
            while(true){
                String strFromUser="";
                if(in.hasNext()) {
                    strFromUser = in.nextLine();
                }
                //向服务器发送信息
                printStream.println(strFromUser);
                //判断退出，字符串包含byebye
                    if(strFromUser.contains("byebye")){
                        System.out.println("客户端退出聊天室");
                        in.close();
                        client.close();
                        break;

                    }
                }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
public class client1 {
    public static void main(String[] args) throws IOException {
        Socket client =new Socket("127.0.0.1",6666);
        Thread readThread =new Thread(new ReadFromsever(client));
        Thread sendThread=new Thread(new SendMsgToSever(client));
        readThread.start();
        sendThread.start();
    }


}
