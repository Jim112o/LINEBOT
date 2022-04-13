package Webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.profile.UserProfileResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static API.MessagingAPI.client;
import static Systems.File.BOT_TOKEN;

public class Json {

    /**
     * Lineからきたイベントの種類を返す。
     *
     * @param json lineから送られてきたjson
     * @return 例：テキストや画像などは message が返され、追加やブロックなどは違うものが返される。
     */
    public static String getEventType(String json){
        ObjectMapper mapper = new ObjectMapper();
        String eventType = null;
        try {
            eventType = String.valueOf(mapper.readTree(json).findValue("events").findValue("type")).replaceAll("\"","");
        } catch (JsonProcessingException ignored) {
        }
        return eventType;
    }

    /**
     * 送られてきたメッセージの種類を返す。
     *
     * @param json lineから送られてきたjson
     * @return 例：テキストの場合は text で画像の場合は image が返される。
     */
    public static String getMessageType(String json){
        ObjectMapper mapper = new ObjectMapper();
        String messageType = null;
        try {
            messageType = String.valueOf(mapper.readTree(json).findValue("events").findValue("message").findValue("type")).replaceAll("\"","");
        } catch (JsonProcessingException ignored) {
        }
        return messageType;
    }

    /**
     * どのトークルームから送られてきているのかを返す。
     *
     * @param json lineから送られてきたjson
     * @return 例: 個人ルームの場合は user  (個人ルームからしかまだ試していない。)
     */
    public static String getSourceType(String json){
        ObjectMapper mapper = new ObjectMapper();
        String sourceType = null;
        try {
            sourceType = String.valueOf(mapper.readTree(json).findValue("events").findValue("source").findValue("type")).replaceAll("\"","");
        } catch (JsonProcessingException ignored) {
        }
        return sourceType;
    }

    public static BufferedImage imageIsNotFound;

    static {
        try {
            imageIsNotFound = ImageIO.read(new File("src/main/resources/Image/imageIsNotFound.jpg"));
        } catch (IOException ignored) {
        }
    }

    /**
     * 送られてきたメッセージを各データに変換する
     *
     * @param json lineから送られてきたjson
     * @return 変換済みのデータ
     */
    public static Object getMessage(String json){

        ObjectMapper mapper = new ObjectMapper();

        String text = "";

        if(isMessage(json)){
            try {
                text = String.valueOf(mapper.readTree(json).findValue("events").findValue("message").findValue("text")).replaceAll("\"", "");
            } catch (JsonProcessingException ignored) {}
            return text;
        }else if(isImage(json)){

            BufferedImage img = null;
            LineBlobClient client = LineBlobClient.builder(BOT_TOKEN).build();

            MessageContentResponse messageContentResponse = null;
            try {
                messageContentResponse = client.getMessageContent(getMessageId(json)).get();
            } catch (InterruptedException | ExecutionException ignored) {
            }

            try(var input = Objects.requireNonNull(messageContentResponse).getStream()) {

                 img = ImageIO.read(input);

            } catch (IOException ignored) {
            }

            return img != null ? img : imageIsNotFound;

        }else{
            return "対応されていないメッセージが送信されたよ";
        }
    }

    /**
     * jsonからuserIdを取得する
     *
     * @param json lineから送られてきたjson
     * @return userId
     */
    public static String getUserId(String json){
        ObjectMapper mapper = new ObjectMapper();
        String userId = "";
        if(getSourceType(json).equalsIgnoreCase("user")){
            try {
                userId = String.valueOf(mapper.readTree(json).findValue("events").findValue("source").findValue("userId")).replaceAll("\"","");
            }catch (JsonProcessingException ignored){

            }
            return userId;
        }else{
            System.out.println("user 以外");
            try {
                userId = String.valueOf(mapper.readTree(json).findValue("events").findValue("source").findValue("userId")).replaceAll("\"","");
            }catch (JsonProcessingException ignored){

            }
            return userId;
        }
    }

    /**
     * 送られてきた画像などコンテンツを取得するために使用するMessageIdを返す。
     *
     * @param json lineから送られてきたjson
     * @return jsonから取得したMessageId
     */
    public static String getMessageId(String json){
        ObjectMapper mapper = new ObjectMapper();
        String messageId = null;
        try {
            messageId = String.valueOf(mapper.readTree(json).findValue("events").findValue("message").findValue("id")).replaceAll("\"", "");
        } catch (JsonProcessingException ignored) {}
        return messageId;
    }

    /**
     * 返信用トークンをjsonから取得して返す。
     *
     * @param json lineから送られてきたjson
     * @return 返信用トークン
     */
    public static String getReplyToken(String json){
        ObjectMapper mapper = new ObjectMapper();
        String replyToken = null;
        try {
            replyToken = String.valueOf(mapper.readTree(json).findValue("events").findValue("replyToken")).replaceAll("\"", "");
        } catch (JsonProcessingException ignored) {}
        return replyToken;
    }

    /**
     * 送信したユーザの情報をuserIdから取得する
     *
     * @param json lineから送られてきたjson
     * @return ユーザー情報
     */
    public static UserProfileResponse getUserProfile(String json){
        UserProfileResponse userProfileResponse = null;
        try {
            userProfileResponse = client.getProfile(getUserId(json)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return userProfileResponse;
    }

    /**
     * ユーザから送られてきたものがテキストメッセージかどうかを判断する。
     *
     * @param json lineから送られてきたjson
     * @return テキストメッセージなら true それ以外は false
     */
    public static boolean isMessage(String json){
        return getMessageType(json).equalsIgnoreCase("text");
    }

    /**
     * ユーザから送られてきたものがスタンプかどうかを判断する。
     *
     * @param json lineから送られてきたjson
     * @return スタンプなら true それ以外は false
     */
    public static boolean isSticker(String json){
        return getMessageType(json).equalsIgnoreCase("sticker");
    }

    /**
     * ユーザから送られてきたものが画像かどうかを判断する。
     *
     * @param json lineから送られてきたjson
     * @return 画像なら true それ以外は false
     */
    public static boolean isImage(String json){
        return getMessageType(json).equalsIgnoreCase("image");
    }

}
