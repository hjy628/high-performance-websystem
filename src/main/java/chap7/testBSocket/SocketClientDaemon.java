package chap7.testBSocket;

import java.util.concurrent.CountDownLatch;

/**
 * @auther: hjy
 * @Date: 19-11-15 13:46
 * @Description:  客户端
 */

public class SocketClientDaemon {

    public static void main(String[] args) throws Exception{

        Integer clientNumber = 20;
        CountDownLatch countDownLatch = new CountDownLatch(clientNumber);
        //分别开始启动这20个客户端
        for (int index = 0; index < clientNumber; index++,countDownLatch.countDown()) {
            SocketClientRequestThread client = new SocketClientRequestThread(countDownLatch,index);
            new Thread(client).start();
        }

        //这个同步锁不涉及具体的实验逻辑，只是保证守护线程在启动所有线程后，不会退出
        synchronized (SocketClientDaemon.class){
            SocketClientDaemon.class.wait();
        }
    }
}
