//リクエストを受け取り、何らかの処理をし、レスポンスを返すクラス。
import java.util.*;
import java.util.function.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.*;//json読み取り用
import com.fasterxml.jackson.databind.node.*;//json出力用
import com.fasterxml.jackson.core.type.TypeReference;

public class ProcessServer {
    public static final String FLOOR = "floor";
    public static final String NROOT = "nroot";
    public static final String REVERSE = "reverse";
    public static final String VALIDANAGRAM = "validAnagram";
    public static final String SORT = "sort";

    // リクエストに基づき計算するメソッド
    private static Response calucProcess(Request req){

        switch (req.getMethod()) {
            case FLOOR -> {
                if(req instanceof Request1 req1){
                    Function<Double, Integer> f = d -> (int)Math.floor(d);
                    String result = (String.valueOf(f.apply(Double.valueOf(req1.getParams()))));
                    return new Response1(req.getMethod(), "int", req1.getId(), result);
                    
                }
            }
            case NROOT -> {
                if(req instanceof Request2 req2){
                    BiFunction<Integer, Integer, Double> f = (n, x) -> Math.exp(Math.log(x)/n);
                    String[] params = req2.getParams();
                    if(Integer.valueOf(params[1]) <= 0){System.out.println("第二引数が0または負の値です。正しく計算することができません。");}
                    String result = String.valueOf(f.apply(Integer.valueOf(params[0]), Integer.valueOf(params[1])));
                    return new Response1(NROOT, "double", req2.getId(), result);
                    
                }
            }
            case REVERSE ->{
                if(req instanceof Request1 req1){
                    Function<String, String> f = s -> {
                        StringBuilder sb = new StringBuilder(s);
                        return sb.reverse().toString();
                    };
                    String result = f.apply(req1.getParams());
                    return new Response1(REVERSE, "string", req1.getId(), result);
                }
            }
            case VALIDANAGRAM ->{
                if(req instanceof Request2 req2){
                    BiPredicate<String, String> f = (s1, s2) ->{
                        char[] c1 = s1.toLowerCase().replaceAll(" ", "").toCharArray();
                        char[] c2 = s2.toLowerCase().replaceAll(" ", "").toCharArray();
                        if(c1.length != c2.length){return false;}
                        Arrays.sort(c1);
                        Arrays.sort(c2);
                        return Arrays.compare(c1, c2) == 0;
                    };
                    String[] arr = req2.getParams();
                    String result = String.valueOf(f.test(arr[0], arr[1]));
                    return new Response1(VALIDANAGRAM, "string", req2.getId(), result);
                    
                }
            }
            case SORT ->{
                if(req instanceof Request2 req2){
                    Function<String[], String[]> f = arr ->{
                        Arrays.sort(arr);
                        return arr;
                    };
                    String[] result = f.apply(req2.getParams());
                    return new Response2(SORT, "StringArray", req2.getId(), result);
                    
                }
            }
        }
        return new Response1(req.getMethod(), "non", req.getId(), "Sorry, can not calculate.");
    }

    //リクエスの受け取りと、レスポンスの返しを行うメソッド。
    public static void requestAndResponse(SocketChannel socketChannel){
        ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
        ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
        
        try {
            //リクエストを読み込む。
            socketChannel.read(requestBuffer);
            requestBuffer.flip();
            String requestStr = StandardCharsets.UTF_8.decode(requestBuffer).toString();//jsonファイルの文字列

            //jsonファイルの文字列をRequestクラスのインスタンスに変換する。
            ObjectMapper mapper = new ObjectMapper();
            
            String responseStr = "";
            try {
                Request1 req1 = mapper.readValue(requestStr, Request1.class);
                System.out.println("以下のリクエストを読み込みます。" + System.lineSeparator() 
                    + mapper.writeValueAsString(req1));
                //Responseクラスについて、json文字列に変換する。
                responseStr = mapper.writeValueAsString(calucProcess(req1));
            } catch (DatabindException e) {
                Request2 req2 = mapper.readValue(requestStr, Request2.class);
                System.out.println("以下のリクエストを読み込みます。" + System.lineSeparator() 
                    + mapper.writeValueAsString(req2));
                responseStr = mapper.writeValueAsString(calucProcess(req2));
            }

            //文字列をByteBufferに流す。
            responseBuffer = StandardCharsets.UTF_8.encode(responseStr);

            //socketChannelにレスポンスを書き込む。
            socketChannel.write(responseBuffer);
            System.out.println("以下のレスポンスを返しました。" + System.lineSeparator() 
                + responseStr);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        
    }
}

class Request{
    private String method;
    private String id;
    
    public String getMethod(){return this.method;}
    public String getId(){return this.id;}
    
}

class Request1 extends Request{
    private String params;

    public String getParams(){return this.params;}
    public String toString(){
        return "method: " + super.getMethod() + " params: " + this.getParams() + " id: " + super.getId();
    }
}

class Request2 extends Request{
    private String[] params;

    public String[] getParams(){return this.params;}
    public String toString(){
        return "method: " + super.getMethod() + " params: " + Arrays.toString(this.getParams()) + " id: " + super.getId();
    }
}

class Response{
    private String method;
    private String result_type;
    private String id;
    public Response(String method, String result_type, String id){
        setMethod(method);
        setResult_type(result_type);
        setId(id);
    }

    public String getResult_type(){return this.result_type;}
    public String getId(){return this.id;}
    public String getMethod(){return this.method;}
    public void setResult_type(String s){this.result_type = s;}
    public void setId(String s){this.id = s;}
    public void setMethod(String s){this.method = s;}
}

class Response1 extends Response{
    private String result;
    public Response1(String method, String result_type, String id, String resul){
        super(method, result_type, id);
        setResult(resul);
    }

    public String getResult(){return this.result;}
    public void setResult(String s){this.result = s;}
    public String toString(){
        return "method: " + super.getMethod() + " result_type: " + super.getResult_type() + " id: " + super.getId() + "result: " + this.getResult();
    }
}

class Response2 extends Response{
    private String[] result;
    public Response2(String method, String result_type, String id, String[] result){
        super(method, result_type, id);
        setResult(result);
    }

    public String[] getResult(){return this.result;}
    public void setResult(String[] arr){this.result = arr;}
    public String toString(){
        return "method: " + super.getMethod() + " result_type: " + super.getResult_type() + " id: " + super.getId() + "result: " + Arrays.toString(this.getResult());
    }
}

/*class Calculation{
    //関数を定義したメソッド
    public static final Map<String, Object> functions(){
        
        Function<Double, Integer> floor = d -> (int)Math.floor(d);
        BiFunction<Integer, Integer, Double> nroot = (n, x) -> Math.exp(Math.log(x)/n);
        Function<String, String> reverse = s -> {
            StringBuilder sb = new StringBuilder(s);
            return sb.reverse().toString();
        };
        BiPredicate<String, String> validAnagram = (s1, s2) ->{
            return s1.compareTo(s2) == 0;
        };
        Function<List<String>, List<String>> sort = list ->{
            list.sort(null);
            return list;
        };
        
        Map<String, Object> functionsMap = new HashMap<String, Object>(){
            {
                put(ProcessServer.FLOOR, floor);
                put(ProcessServer.NROOT, nroot);
                put(ProcessServer.REVERSE, reverse);
                put(ProcessServer.VALIDANAGRAM, validAnagram);
                put(ProcessServer.SORT, sort);
            }
        };
        return functionsMap;
   }

   

}*/

