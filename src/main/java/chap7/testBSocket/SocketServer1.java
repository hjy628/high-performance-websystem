package chap7.testBSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @auther: hjy
 * @Date: 19-11-15 11:07
 * @Description:  java阻塞模式
 */

public class SocketServer1 {


    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(1088);
        try {
            while (true){
                //这里java通过JNI请求操作系统，并等待操作系统返回结果或出错
                Socket socket = serverSocket.accept();
                //下面我们收取信息(这里还是阻塞式的，一直等待，直到有数据可以接收)
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Integer sourcePort = socket.getPort();
                int maxLen = 2048;
                byte[] contextBytes = new byte[maxLen];
                int realLen;
                StringBuffer message = new StringBuffer();
                //试图读取数据的时候，程序也会被阻塞，直到操作系统把网络传来的数据准备好
                while ((realLen = in.read(contextBytes,0,maxLen))!=-1)

                    message.append(new String(contextBytes,0,realLen));
                //我们假设读取到"over"关键字表示一段内容传输完成
                //实际上还有更好的方式，后文进行说明
                if (message.indexOf("over")!=-1){
                    break;
                }
                //下面打印信息
                System.out.println("服务器收到来自于端口： "+sourcePort+"的信息： "+message);
                //下面开始发送信息
                out.write("回发响应信息！".getBytes());
                //关闭
                out.close();
                in.close();
                socket.close();
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            if (serverSocket!=null){
                serverSocket.close();
            }
        }
    }



}
