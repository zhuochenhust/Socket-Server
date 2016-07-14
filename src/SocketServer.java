
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天服务器，支持群聊,相当于openfire
 * 
 * 有一个新用户，会建一个socket连接，会启一个线程
 * socket连接只有在用户退出的时候才会关闭。
 * @author pjy
 *
 */
public class SocketServer {
	//每个班只部署一份，
	//其他同学 telnet ip 15898
	//0-1023，端口号最大值65535
	//一个组只能有一个server
    private static final int PORT = 15898;//大于1024
    private List<Socket> socketList = new ArrayList<Socket>();
    private ServerSocket server = null;
    private ExecutorService threadPool = null; //thread pool
    
    public static void main(String[] args) {
        new SocketServer();
    }
    public SocketServer() {
        try {
        	//如果有客户端发信息到15898这个端口，操作系统会把信息交给我们这个程序处理
            server = new ServerSocket(PORT);
            threadPool = Executors.newCachedThreadPool();  //create a thread pool
            System.out.println("server start ...");
            //socketClient代表的是与某一个客户端的连接            
            Socket socketClient = null;
            while(true) {
            	//客户端跟我们服务器端建立连接之后，创建socketClient对象
                socketClient = server.accept();
                
                socketList.add(socketClient);
              //  System.out.println("有"+mList.size()+"个连接 ");
                System.out.println("有连接 ");
                threadPool.execute(new Service(socketClient)); //start a new thread to handle the connection
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    class Service implements Runnable {
            private Socket socketClient;
            private BufferedReader in = null;
            private String msg = "";
            
            public Service(Socket socketClient) {
                this.socketClient = socketClient;
                try {
                    in = new BufferedReader(new InputStreamReader(
                    		socketClient.getInputStream()));
                    
                    msg = "user" +this.socketClient.getInetAddress() + "客户端个数:"
                        +socketList.size()+"线程id:"+Thread.currentThread().getId();
                    this.sendmsg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    while(true) {
                        if((msg = in.readLine())!= null) {
                            if(msg.equals("exit")) {
                                System.out.println("有客户端退出");
                                socketList.remove(socketClient);
                                in.close();
                                msg = "user:" + socketClient.getInetAddress()
                                    + "exit total:" + socketList.size();
                                socketClient.close();
                                this.sendmsg();
                                break;
                            } else {
                            	//状态码|消息类型|消息json数据
                                msg = "{'user ip':'"+socketClient.getInetAddress() + "','msg':'" + msg+"'} ";
                               System.out.println("sever 收到"+msg);
                                this.sendmsg();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
          //socket对象在内存中。客户端不发请求，服务器端也可以主动向客户端发数据
           //http的特点是必须是客户端先发请求，服务器端才能返加数据。
           public void sendmsg() {
               
               int socketClinetNumber =socketList.size();
               //给所有的客户端发数据,说明1是推送 2，是群聊
               for (int index = 0; index < socketClinetNumber; index ++) {
                   //Socket产生一个ip[tcp[msg]]
            	   Socket socketClient = socketList.get(index);
                   PrintWriter pout = null;
                   try {
                       pout = new PrintWriter(new BufferedWriter(
                               new OutputStreamWriter(
                            		   socketClient.getOutputStream())),true);
                       pout.println(msg);
                   }catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           }
        }    
}