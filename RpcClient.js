//全体の流れ。
//コマンドラインからのリクエストをオブジェクトに格納する。
//オブジェクトをjson形式の文字列に変換後、サーバーに送る。
//サーバーから返送された処理結果を、json形式の文字列で出力する。

//netモジュールをファイルパス("net")から呼び出す。
const net = require('net');
//サーバーのアドレス。
const serverAddress = "/tmp/rpc_soket_file";
const client = new net.Socket();

//リクエストを格納するオブジェクト。
const request = {
    method:"",
    params:"",
    id:"",
};

function readUserInput(question){
    const readline = require('readline').createInterface({
        input: process.stdin,//input:読み込み可能なストリーム
        output: process.stdout//readlineデータを書き込むための書き込み可能なストリームの宛先。
    });

    return new Promise((resolve, reject) => {
        readline.question(question, (answer) =>{
            resolve(answer);
            readline.close();
        });
    });
}

(async function main(){
    method = await readUserInput('Input Method -->');
    params = await readUserInput('Input params -->');
    id = await readUserInput('Input id -->');

    request.method = method == "" ? request.method : method;
    request.params = params == "" ? request.params : params;
    //paramsにスペースが含まれる場合、配列に変換する。
    if(request.params.includes(' ')){
        request.params = request.params.split(' ');
    }
    request.id = id == "" ? request.id : id;

    console.log(request);

    //サーバーに接続し、json形式の文字列に変換したリクエストをソケットに書き込み、サーバーに送る。
    client.connect(serverAddress, () =>{
        console.log('Connected to server');
        client.write(JSON.stringify(request));
    });    

    //サーバーから返送された処理結果について、json形式のオブジェクトに変換する。
    //その後、クライアントの接続を閉じる。
    client.on('data', (data) =>{
        const response = JSON.parse(data);

        if(response.error){
            console.error('Error', response.error);
        }else{
            console.log(response);
        }
        client.end();
    });

    client.on('close', () =>{
        console.log('Connection closed');
    });

    client.on('error', (error) =>{
        console.error('Error', error);
    });
})();




