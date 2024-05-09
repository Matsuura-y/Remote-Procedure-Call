
public class RpcServerMain{
    public static void main(String[] args) {
        
        /*
         * レクエストjson
         * {
         *  "method":"function name",//関数の名称
         *  "params":[49],//関数に渡すパラメータ
         *  "id":"0"//リクエスト
         * }
         */

        //bind()メソッドでソケットに接続する。
        //このメソッドの内部は、リクエスを受け取り、レスポンスを返す処理も含む。
        ConnectServer.bind();

    }
}
