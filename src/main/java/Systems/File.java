package Systems;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Properties;

public class File {

    public static void createFile(String fileName) {
        Path p = Paths.get("src/main/resources/Image/"+fileName);
        try {
            Files.createDirectory(p);
        } catch (IOException ignored) {
        }
    }

    public static String S3_ACCESS_KEY;
    public static String S3_SECRET_KEY;
    public static String S3_SERVICE_END_POINT;
    public static String S3_REGION;
    public static String S3_BUCKET_NAME;

    public static String S3_URL_01;
    public static String S3_URL_02;
    public static String S3_NOT_FOUND;
    public static String S3_DOWNLOAD_IMAGE;

    public static String BOT_TOKEN;
    public static String BOT_SECRET;

    public static void setUp(){
        Properties properties = new Properties();

        //プロパティファイルのパスを指定する
        String pass = "src/main/resources/secret.properties";

        try {
            InputStream stream = new FileInputStream(pass);
            properties.load(stream);

            S3_ACCESS_KEY = properties.getProperty("S3_ACCESS_KEY");
            S3_SECRET_KEY = properties.getProperty("S3_SECRET_KEY");
            S3_REGION = properties.getProperty("S3_REGION");
            S3_BUCKET_NAME = properties.getProperty("S3_BUCKET_NAME");
            S3_URL_01 = properties.getProperty("S3_URL_1");
            S3_URL_02 = properties.getProperty("S3_URL_2");
            S3_NOT_FOUND = properties.getProperty("S3_NOT_FOUND");
            S3_DOWNLOAD_IMAGE = properties.getProperty("S3_DOWNLOAD_IMAGE");
            BOT_SECRET = properties.getProperty("bot.channelSecret");
            BOT_TOKEN = properties.getProperty("bot.channelToken");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
