package Wechaet;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.SQLOutput;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sever1 {
    private static Map<String, Socket> clientLists = new ConcurrentHashMap<>();

    /**
     * 专门用来处理每个客户端的输入与输出
     */
    private static class ExecuteClientRequest implements Runnable {
        private Socket client;

        private ExecuteClientRequest(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            //获取用户输入流，读取用户发来的信息
            try {
                Scanner in = new Scanner(client.getInputStream());
                String strFromClient = "";
                while (true) {
                    System.out.println("请输入向服务器输入的信息");
                    String strFromUser = "";

                    if (in.hasNext()) {
                        strFromClient = in.nextLine();
                        //Windoes下消除用户输入信息自带的\r
                        //将/r->“ ”
                        Pattern pattern = Pattern.compile("\r");
                        Matcher matcher = pattern.matcher(strFromClient);
                        strFromClient = matcher.replaceAll("");
                    }
                    //注册流程

                    if (strFromClient.startsWith("userName")) {
                        String userName = strFromClient.split("\\:")[1];
                        userRegister(userName, client);
                    }
                    //群聊流程
                    if (strFromClient.startsWith("G:")) {
                        String groupMsg=strFromClient.split("\\:")[1];

                        groupChat(groupMsg);

                    }
                    //私聊流程
                    if (strFromClient.startsWith("P:")) {
                        String userName=strFromUser.split("\\:")[1]
                                .split("\\:")[0];
                        String privateMsg=strFromClient.split("\\:")[1]
                                .split("\\-")[1];
                        privateChat(userName,privateMsg);


                    }
                    //用户退出
                    if (strFromClient.contains("byebye")) {
                        String userName=strFromClient.split("\\:")[0];

                        userOfflie(userName);
                        break;

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


            /**
             * 注册方法
             * @param userName//要注册的用户名
             * @param socket//用户名对应的Socket
             */
            private void userRegister (String userName, Socket socket){
                clientLists.put(userName, socket);
                System.out.println("用户" + userName + "上线了");
                System.out.println("当前聊天室人数为" + clientLists.size());
                try {
                    PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
                    out.println("注册成功");
                    out.println("当前聊天室人数为"+clientLists.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            private void groupChat(String groupMsg){
                Set<Map.Entry<String,Socket>> clientEntry=clientLists.entrySet();
                Iterator<Map.Entry<String,Socket>> iterator=clientEntry.iterator();
                while(iterator.hasNext()){
                    //取出每一个客户端实体
                    Map.Entry<String ,Socket> client =iterator.next();
                    //拿到客户端输出流输出群聊信息
                    PrintStream out= null;
                    try {
                        out = new PrintStream(client.getValue().getOutputStream(),true,"UTF-8");
                        out.println("群聊信息为:"+groupMsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
            private void privateChat(String userName,String privateMag){
                //取出userName对应的Socket
                Socket client=clientLists.get(userName);
                try {
                    //获取输出流
                    PrintStream out=new PrintStream(client.getOutputStream(),true,"UTF-8");
                    out.println("私聊信息为:"+privateMag);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            private void userOfflie(String userName){
                //删除Map中的用户实体
                clientLists.remove(userName);
                System.out.println("用户"+userName+"已下线");
                System.out.println("当前聊天室的人数为:"+clientLists.size());

            }
        }

        public static void main(String[] args) throws Exception {

            ServerSocket serverSocket = new ServerSocket(6666);
            //使用线程池同时处理多个客户端的连接
            ExecutorService excutorSeverice = Executors.newFixedThreadPool(20);
            System.out.println("等待客户端连接");
            for (int i = 0; i < 20; i++) {
                Socket client =serverSocket.accept();
                System.out.println("有新的客户端连接，端口号为:" + client.getPort());
                excutorSeverice.submit(new ExecuteClientRequest(client));
            }
            //关闭线程池与服务端
            excutorSeverice.shutdown();
            serverSocket.close();
        }

    }