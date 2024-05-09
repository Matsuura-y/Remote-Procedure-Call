
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;//ストリーム型リスニング・ソケットのためのチャネル。
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;

public class ConnectServer {
    //ファイルパスを定義。
    public static final Path PATH =Path.of("/tmp/rpc_soket_file");
    //ファイルパスをUNIXドメインのソケットに指定する。
    public static final UnixDomainSocketAddress ADDRESS = UnixDomainSocketAddress.of(PATH);

    public static void bind() {
        //全体の流れ。
        //ソケットチャネルを開き、送受信する。
        try (//UNIXドメインのプロセス通信を行うためのソケットチャネルを開く。
            ServerSocketChannel ssc = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            ) {
                Files.deleteIfExists(PATH);
                ssc.bind(ADDRESS);//ソケットにアドレスを紐付ける。
            
            //このソケットのチャネルへの接続を常駐させる。
            while(true){

                try (//このチャネルへの接続を受け付ける。
                    SocketChannel socketChannel = ssc.accept()) {
                    
                    //リクエストの受付とレスポンスの返しを行うメソッド。
                    ProcessServer.requestAndResponse(socketChannel);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}
