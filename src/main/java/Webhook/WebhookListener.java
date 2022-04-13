package Webhook;

import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static API.MessagingAPI.*;
import static AWS.S3.putObject;
import static OCR.OCRTest.getStringReadFromImage;
import static Webhook.Json.*;

public class WebhookListener {

    /**
     * リクエストが来た時に処理するハンドラクラス。
     */
    private static class WebhookHandler implements HttpHandler {

        /**
         * HttpServer.createContext()で登録したパスにHTTPリクエストがあると、このメソッドが呼ばれる。
         * 参考サイト
         * https://qiita.com/thrzn41/items/276aaaf5df11e351880a
         */
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Webhook通知元の仕様にもよるが、HTTPのPOSTかGETかで通知が来る前提にして、
            // それ以外は、"204 No Content"を返す。
            String method = exchange.getRequestMethod();

            if( !method.equals("POST") && !method.equals("GET") ) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Bodyの内容を読み込んで表示するだけ。
            try(var input = exchange.getRequestBody()) {

                // 本来は、ちゃんとリクエストヘッダのエンコーディングを見た方がいいですが、
                // 今回は、"utf-8"である前提で変換しています。
                String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);


                System.out.printf("RequestData: %s%n", body);


                //------------ JSON解析
                if(getEventType(body).equalsIgnoreCase("message")){//メッセージイベント

                    UserProfileResponse userProfileResponse = getUserProfile(body); //ユーザ情報の取得
                    if(isMessage(body)){//テキストメッセージ

                        //そのまま送り返す
                        ReplyMessage(body, (String) getMessage(body));

                        /* テキストメッセージをオーナに送信しちゃう */
                        //client.pushMessage(new PushMessage(owner,new TextMessage((String) getMessage(body))));

                    }else{
                        if(isImage(body)){//画像が送られてきている

                            //送られたきた画像ファイルを保存
                            Systems.File.createFile(getUserId(body));
                            ImageIO.write((RenderedImage) getMessage(body), "jpg", new File("src/main/resources/Image/"+getUserId(body)+"/s3uploadImage.jpg"));

                            //送られたきた画像ファイルをS3にアップロード
                            putObject(new File("src/main/resources/Image/"+getUserId(body)+"/s3uploadImage.jpg"),getUserId(body));

                            //保存したことをユーザに伝える
                            //PushMessage(getUserId(body),"%%DISPLAYNAMEさんの送信した画像が保存されました。 (%%REPLACE)".replaceAll("%%DISPLAYNAME",userProfileResponse.getDisplayName()).replaceAll("%%REPLACE",ans));

                            //送ってきた画像ファイルを送り返す
                            //ReplyImage(body);

                            //送られてきた画像ファイルから文字を認識して帰ってきた文字列を返信する。
                            String stringList = getStringReadFromImage(getUserId(body));
                            //ReplyMessage(body, stringList);
                            //ReplyMessage(body,stringList);
                            ReplyMessages(body,getImageMessage(body, getUserId(body)),new TextMessage(stringList));

                            //System.out.println("-------------------------------------------------------\n\n\n\n\n\n\n\n"+getUserId(body)+"\n-------------------------------------------------------\n\n\n\n\n\n\n\n\n\n\n");

                        }else if(isSticker(body)){//スタンプが送られてきている
                            ReplyMessages(body, true, new TextMessage("大変申し訳ございませんが\nスタンプは対応していません。"),new StickerMessage("11539","52114110"));
                        }else{//それ以外のメッセージ（動画ファイルなど）
                            ReplyMessages(body, true, new TextMessage("対応していないmessage typeです。\n対応するまでもう少々お待ちください。"),new StickerMessage("11539","52114140"));
                        }
                    }
                }
                //------------ JSON解析
            }


            ////////////////////////////////////////////////////////////////
            // 以下、レスポンスで何を返すべきかは通知元のサービス側の仕様にもよる。

            // "204 No Content"を返せばいい場合は、以下にコメントアウトした1行だけでBody出力不要。
            // exchange.sendResponseHeaders(204, -1);

            // レスポンスのBodyは決め打ちで書き込む。
            var responseBytes = "{\"status\" : \"OK\"}".getBytes(StandardCharsets.UTF_8);

            try(var output = exchange.getResponseBody()) {

                // レスポンスヘッダで、Content-Type: application/jsonにする。
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);

                output.write(responseBytes);
            }
        }

    }

    public static void main(String[] args) {

        //起動方法
        //1  ./Java/lib/ngrok http --bind-tls=true　--region=ap --log /tmp/log/ngrok.log --log-format json --log-level info 8080
        // http --log /tmp/log/ngrok.log --log-format json --log-level info 9000
        //2  webhookのURLを更新
        //3  WebhookListenerを実行

        try {
            // ローカルの8080番ポート(ngrok起動時のオプションで指定した番号)で待ち受けます。
            // ngrokが同じローカルマシンで動いているので、"127.0.0.1"だけで待ち受ければよい。
            // (外部の環境からリクエストを受ける必要がない)
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), -1);

            // ローカルサーバの"/webhook"にリクエストが来た時に呼ばれるハンドラを登録。
            server.createContext("/sample", new WebhookHandler());

            // 今回はデフォルトのExecutorを利用してHTTPリクエストを処理するように指定(null)。
            server.setExecutor(null);

            // HttpServerの待ち受けを開始します。
            server.start();

            System.out.println("終了するには何かキーを押してください。");

            int i = System.in.read();

            System.out.println(i);

            // HttpServerの待ち受けを停止します。
            server.stop(0);
            System.exit(0);

        } catch(IOException ioex) {
            ioex.printStackTrace();
        }
    }

}
