package chap7.testBSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @auther: hjy
 * @Date: 19-11-15 14:09
 * @Description:通过加入线程的概念，让socket server能够在应用层面
 * 通过非阻塞的方式同时处理多个socket套接字
 */

public class SocketServer4 {

    private static Object xWait = new Object();
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer4.class);

    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(1083);
        serverSocket.setSoTimeout(100);
        try {
            while (true){
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                }catch (SocketTimeoutException e1){
                    //执行到这里，说明本次accept()方法没有结婚搜到任何数据报文
                    //主线程在这里就可以做一些事情，记为X
                    synchronized (SocketServer4.xWait){
                        LOGGER.info("这次没有接收到TCP连接，据报文，等待10毫秒，模拟事件x的处理时间");
                        SocketServer4.xWait.wait(10);
                    }
                    continue;
                }

                //业务处理过程可以交给一个线程，不过线程的创建很耗资源(可以使用线程池)
                //最终改变不了，accept()只能在被阻塞的情况一个一个接收Socket
                SocketServerThread socketServerThread = new SocketServer4().new SocketServerThread(socket);
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
                in = socket.getInputStream();
                out = socket.getOutputStream();
                Integer sourcePort = socket.getPort();
                int maxLen = 2048;
                byte[] contextBytes = new byte[maxLen];
                //同样无法改变read方法被阻塞，直到操作系统有数据准备好的现象
                int realLen;
                StringBuffer message = new StringBuffer();
                //下面我们收取信息
                this.socket.setSoTimeout(10);                while (true){
                    try {
                        while ((realLen = in.read(contextBytes,0,maxLen))!=-1){
                            message.append(new String(contextBytes,0,maxLen));
                            //我们假设读取到"over"关键字，表示业务内容传输完成
                            if (message.indexOf("over")!=-1){
                                break;
                            }
                        }

                    }catch (SocketTimeoutException e2){
                        //执行到这里，说明本次read()方法没有接受到任何数据流
                        //主线程在这里又可以做一些事情，记为Y
                        LOGGER.info("这次没有接收到任何数据包问，等待10毫秒，模拟事件Y的处理时间");
                        continue;
                    }
                    //下面打印信息
                    Long threadId = Thread.currentThread().getId();
                    LOGGER.info("服务器（线程： "+threadId+"）收到来自与端口： "+ sourcePort + "的信息： "+message);
                    //下面开始发送响应信息
                    out.write("回发响应信息！".getBytes());
                    break;

                }
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
