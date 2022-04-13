package API;


import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.*;
import com.linecorp.bot.model.response.BotApiResponse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static Systems.File.*;
import static Webhook.Json.*;


public class MessagingAPI {

    //TODO ステータスコードを取得して送信できなかったときに再送できる様にする

    //LINEBOTのtokenを使ってクライアントを作成
    public static LineMessagingClient client = LineMessagingClient.builder(BOT_TOKEN).build();

    final MessageContentResponse messageContentResponse;

    public MessagingAPI(MessageContentResponse messageContentResponse) {
        this.messageContentResponse = messageContentResponse;
    }

    // オーナーのuserId

    /**
     * ユーザにメッセージを送信する。(有料カウント)
     *
     * @param json userIdを取得するためのjson
     * @param string 送りたい文字列
     */
    public static void PushMessage(String json,String string){

        PushMessage message = new PushMessage(getUserId(json), new TextMessage(string));
        BotApiResponse response = null;
        int num = 0;
        try {
            response = client.pushMessage(message).get();
            
        } catch (InterruptedException | ExecutionException ignored) {
        }
    }

    /**
     * 返信メッセージを送る(無料カウント 通知オプションなし)
     *
     * @param json 返信用Token取得用json
     * @param message 返信するテキストメッセージ
     */
    public static void ReplyMessage(String json, String message){
        client.replyMessage(new ReplyMessage(getReplyToken(json),new TextMessage(message)));
    }

    /**
     * 返信メッセージを送る(無料カウント 通知オプションあり)
     *
     * @param json 返信Token取得用json
     * @param message 返信するテキストメッセージ
     * @param notificationDisabled true = 通知なし, false = 通知あり
     */
    public static void ReplyMessage(String json, String message, boolean notificationDisabled){
        client.replyMessage(new ReplyMessage(getReplyToken(json),new TextMessage(message),notificationDisabled));
    }

    /**
     * 特定のユーザにスタンプを送信する。
     *
     * @param json 返信Token取得用json
     * @param packageId パッケージId
     * @param stickerId スタンプId
     */
    public static void ReplyMessage(String json, String packageId, String stickerId){
        client.replyMessage(new ReplyMessage(getReplyToken(json),new StickerMessage(packageId,stickerId)));
    }

    /**
     * 特定のユーザにスタンプを送信する。(通知オプションあり)
     *
     * @param json 返信Token取得用json
     * @param packageId パッケージId
     * @param stickerId スタンプId
     * @param notificationDisabled true = 通知なし, false = 通知あり
     */
    public static void ReplyMessage(String json, String packageId, String stickerId, boolean notificationDisabled){
        client.replyMessage(new ReplyMessage(getReplyToken(json),new StickerMessage(packageId,stickerId),notificationDisabled));
    }

    /**
     * 特定のユーザに複数メッセージを送信する
     *
     * @param json 返信Token取得用json
     * @param messages メッセージ
     */
    public static void ReplyMessages(String json, Message... messages){
        client.replyMessage(new ReplyMessage(getReplyToken(json), Arrays.asList(messages)));
    }

    public static void ReplyMessages(String json, boolean notificationDisabled, Message... message1){
        client.replyMessage(new ReplyMessage(getReplyToken(json), Arrays.asList(message1),notificationDisabled));
    }

    public static String getCount(){

        try {
            return client.getMessageQuota().get().toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "null";
    }

    /**
     * 全ユーザにテキストメッセージを送信（有料カウント）
     *
     * @param string 送信するテキストメッセージ
     */
    public static void BroadcastMessage(String string){
        client.broadcast(new Broadcast(new TextMessage(string)));
    }

    /**
     * 特定のユーザに画像を送信する
     * S3から画像を取得するので事前に画像をアップロードしてる必要があります。
     *
     * @param json userId取得用json
     * @param userId LINEのuser id
     */
    public static void ReplyImage(String json, String userId){

        String AWSImageUrl = S3_URL_01 + userId + S3_URL_02;

        //メインの画像(プレビュー画像に触れるとこの画像を取得して表示される)
        final URI originalContentUrl = URI.create(AWSImageUrl);
        //プレビュー用の画像（トーク画面でそのまま表示されるやつ）
        final URI previewImageUrl = URI.create(AWSImageUrl);

        //ImageMessageの生成
        ImageMessage imageMessage = new ImageMessage(originalContentUrl, previewImageUrl);

        //S3から画像が取得できたのかチェック
        boolean check = true;
        try {
            URL url = new URL(AWSImageUrl);
            Image image = ImageIO.read(url);
        } catch (IOException e) {
            check = false;
        }

        ReplyMessage message;

        if(check){//S3から画像が取得できた
            message = new ReplyMessage(getReplyToken(json),imageMessage);
        }else{//できなかった
            TextMessage text = new TextMessage("AWS側ではじかれています。");
            message = new ReplyMessage(getReplyToken(json), Arrays.asList(imageMessage,text));
        }

        //画像を送信(有料カウント)
        client.replyMessage(message);
    }

    public static ImageMessage getImageMessage(String json, String userId){

        String AWSImageUrl = S3_URL_01 + userId + S3_URL_02;

        //メインの画像(プレビュー画像に触れるとこの画像を取得して表示される)
        final URI originalContentUrl = URI.create(AWSImageUrl);
        //プレビュー用の画像（トーク画面でそのまま表示されるやつ）
        final URI previewImageUrl = URI.create(AWSImageUrl);

        //画像が見つからなかった場合のURI
        final URI imageNotFound = URI.create(S3_NOT_FOUND);

        //ImageMessageの生成
        ImageMessage imageMessage;

        //S3から画像が取得できたのかチェック
        boolean check = true;
        try {
            URL url = new URL(AWSImageUrl);
            Image image = ImageIO.read(url);
        } catch (IOException e) {
            check = false;
        }


        if(check){//S3から画像が取得できた
            imageMessage = new ImageMessage(originalContentUrl, previewImageUrl);
        }else{//できなかった
            imageMessage = new ImageMessage(imageNotFound,imageNotFound);
        }

        //画像を送信(有料カウント)
        //client.pushMessage(message);
        return imageMessage;
    }

    /**
     * 特定のユーザに画像を送信する
     * S3から画像を取得するので事前に画像をアップロードしてる必要があります。
     *
     * @param json userId取得用json
     */
    public static void sendImage(String json, String userId){

        String AWSImageUrl = S3_URL_01 + userId + S3_URL_02;

        //メインの画像(プレビュー画像に触れるとこの画像を取得して表示される)
        final URI originalContentUrl = URI.create(AWSImageUrl);
        //プレビュー用の画像（トーク画面でそのまま表示されるやつ）
        final URI previewImageUrl = URI.create(AWSImageUrl);

        //ImageMessageの生成
        ImageMessage imageMessage = new ImageMessage(originalContentUrl, previewImageUrl);

        //S3から画像が取得できたのかチェック
        boolean check = true;
        try {
            URL url = new URL(AWSImageUrl);
            Image image = ImageIO.read(url);
        } catch (IOException e) {
            check = false;
        }

        PushMessage message;

        if(check){//S3から画像が取得できた
            message = new PushMessage(getUserId(json),imageMessage);
        }else{//できなかった
            TextMessage text = new TextMessage("AWS側ではじかれています。");
            message = new PushMessage(getUserId(json), Arrays.asList(imageMessage,text));
        }

        //画像を送信(有料カウント)
        client.pushMessage(message);
    }

    /**
     * messageIdから画像データを取得して保存
     * 2022.03.04 getMessageに結合された。
     *
     * @param messageId jsonから取得したmessageId
     * @throws IOException LINEから送られてくるデータを信用してるから例外は起こさせない
     */
    public static void getImageContents(String messageId) throws IOException {

        LineBlobClient client = LineBlobClient.builder(BOT_TOKEN).build();

        final MessageContentResponse messageContentResponse;
        try {
            messageContentResponse = client.getMessageContent(messageId).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        try(var input = messageContentResponse.getStream()) {

            BufferedImage img = ImageIO.read(input);

            //日付時刻パターンの生成
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String datetimeText = LocalDateTime.now().format(fmt);

            ImageIO.write(img, "jpg", new File("src/main/resources/Image/"+ datetimeText +".jpg"));

        }

    }

    public static void pushFlexMessage(){
        //client.pushMessage(new PushMessage(owner,new FlexMessage()));
    }






}
