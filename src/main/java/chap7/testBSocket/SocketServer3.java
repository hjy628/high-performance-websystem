package chap7.testBSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * @auther: hjy
 * @Date: 19-11-15 14:09
 * @Description:
 */

public class SocketServer3 {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketServer3.class);

    private static Object xWait = new Object();

    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1083);
            serverSocket.setSoTimeout(100);
            while (true){
                Socket socket = null;
                try {
                    //程序不会一直在这里阻塞了
                    socket = serverSocket.accept();
                }catch (SocketTimeoutException e1){
                    //执行到这里，说明本次accept()方法没有结婚搜到任何数据报文
                    //主线程在这里就可以做一些事情，记为X
                    synchronized (SocketServer3.xWait){
                        LOGGER.info("这次没有接收到TCP连接，据报文，等待10毫秒，模拟事件x的处理时间");
                        SocketServer3.xWait.wait(10);
                    }
                    continue;
                }
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Integer sourcePort = socket.getPort();
                int maxLen = 2048;
                byte[] contextBytes = new byte[maxLen];
                int realLen;
                StringBuffer message = new StringBuffer();
                //下面我们收取信息(非阻塞方式，read()方法的等待超时时间)
                socket.setSoTimeout(10);

                while (true){
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
                    LOGGER.info("服务器收到来自与端口： "+ sourcePort + "的信息： "+message);
                    //下面开始发送响应信息
                    out.write("回发响应信息！".getBytes());
                    break;

                }
                out.close();
                in.close();
                if (socket!=null){
                    socket.close();
                }
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }finally {
            if (serverSocket!=null){
                serverSocket.close();
            }

        }

    }


}
