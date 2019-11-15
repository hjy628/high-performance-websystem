package chap7.testBSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @auther: hjy
 * @Date: 19-11-15 14:09
 * @Description:
 */

public class SocketServer2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer2.class);

    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(1083);
        try {
            while (true){
                Socket socket = serverSocket.accept();
                //业务处理过程可以交给一个线程，不过线程的创建很耗资源
                //最终改变不了，accept()只能在被阻塞的情况一个一个接收Socket
                SocketServerThread socketServerThread = new SocketServer2().new SocketServerThread(socket);
                new Thread(socketServerThread).start();
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }finally {
            if (serverSocket!=null){
                serverSocket.close();
            }
        }

    }


    //当然，接收到客户端的Socket后，业务的处理过程可以交给一个线程来做
     class SocketServerThread implements Runnable{
        private final Logger LOGGER = LoggerFactory.getLogger(SocketServerThread.class);
        private Socket socket;

        public SocketServerThread(Socket socket) {
            this.socket = socket;
        }



        public void run() {
            InputStream in = null;
            OutputStream out = null;
            try {
                //下面我们收取信息
                in = socket.getInputStream();
                out = socket.getOutputStream();
                Integer sourcePort = socket.getPort();
                int maxLen = 1024;
                byte[] contextBytes = new byte[maxLen];
                //同样无法改变read方法被阻塞，直到操作系统有数据准备好的现象
                int realLen = in.read(contextBytes,0,maxLen);
                String message = new String(contextBytes,0,realLen);
                LOGGER.info("服务器收到来自与端口： "+ sourcePort + "的信息： "+message);
                //下面开始发送响应信息
                out.write("回发响应信息！".getBytes());
            }catch (Exception e){
                this.LOGGER.error(e.getMessage(),e);
            }finally {
                try {
                    out.close();
                    in.close();
                    if (socket!=null){
                        socket.close();
                    }
                }catch (Exception e){
                    LOGGER.error("关闭连接失败!");
                }
            }

        }
    }


}
