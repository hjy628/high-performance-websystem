package chap7.testBSocket;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * @auther: hjy
 * @Date: 19-11-15 13:49
 * @Description:  客户端线程
 * 一个SocketClientRequestThread线程模拟一个客户端请求
 */


public class SocketClientRequestThread implements Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketClientRequestThread.class);

    private CountDownLatch countDownLatch;

    //这个线程的编号
    private Integer clientIndex;
    //countDownLatch是Java提供的线程同步计数器。
    //当计数器值减为0时，所有受其影响的而阻塞的线程将会被激活。尽可能模拟并发请求的真实性(但实际上也并不是完全并发的)


    public SocketClientRequestThread(CountDownLatch countDownLatch, Integer clientIndex) {
        this.countDownLatch = countDownLatch;
        this.clientIndex = clientIndex;
    }

    public void run() {
        Socket socket = null;
        OutputStream clientRequest = null;
        InputStream clientResponse = null;

        try {
            socket = new Socket("localhost",1083);
            clientRequest = socket.getOutputStream();
            clientResponse = socket.getInputStream();
            //阻塞，直到SocketClientDaemon完成所有线程的启动，然后所有线程一起发送请求
            this.countDownLatch.await();
            //发送请求信息
            clientRequest.write(("这是第"+this.clientIndex+"  个客户端的请求,over").getBytes());
            clientRequest.flush();
            //在这里等待，直到服务器返回信息
            SocketClientRequestThread.LOGGER.info("第"+this.clientIndex+"个客户端的请求发送完成，等待服务器返回信息");
            int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];
            int realLen;
            String message = "";
            //程序执行到这里，会h一直等待服务器返回信息
            //(注意，前提是in和out都不能关闭，如果关闭了就收不到)
            while ((realLen = clientResponse.read(contextBytes,0,maxLen))!=-1){
                message += new String(contextBytes,0,realLen);
            }
            SocketClientRequestThread.LOGGER.info("接收到来自服务器的信息："+message);

        }catch (Exception e){
            SocketClientRequestThread.LOGGER.error(e.getMessage(),e);
        }finally {
            //记得关闭连接
            try {

            clientResponse.close();
            clientRequest.close();
            socket.close();
            }catch (Exception e){
                SocketClientRequestThread.LOGGER.error("关闭连接出错!"+e.getMessage());
            }
        }
    }
}
