package OCR;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static Systems.File.S3_URL_01;
import static Systems.File.S3_URL_02;

public class OCRTest {

    public static void main(String[] args){
        // tesseract 2022-03-05-01-59-59-straight.jpg - -l jpn+eng
        ProcessBuilder pb = new ProcessBuilder("tesseract", "/Users/jim/Java/LINEBOT/src/main/resources/Image/2022-03-05-01-59-59-straight.jpg","-","-l","jpn+eng");
        try {
            Process process = pb.start();

            process.waitFor();

            InputStream is = process.getInputStream();	//標準出力
            //List<String> output = printInputStream(is);
            InputStream es = process.getErrorStream();	//標準エラー
            //List<String> error = printInputStream(es);

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static String getStringReadFromImage(String userId){
        ProcessBuilder pb = new ProcessBuilder("tesseract", S3_URL_01 + userId + S3_URL_02,"-","-l","jpn+eng");
        String output = "";
        String error = "";
        try {
            Process process = pb.start();

            process.waitFor();

            InputStream is = process.getInputStream();	//標準出力
            output = printInputStream(is);
            InputStream es = process.getErrorStream();	//標準エラー
            error = printInputStream(es);

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if(output.equalsIgnoreCase("")){
            return "写真はイメージです。";
        }
        return output;
    }


    public static String printInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> strings = new ArrayList<String>();
        try {
            for (;;) {
                String line = br.readLine();
                if (line == null) break;
                //System.out.println(line);
                strings.add(line);
            }
        } finally {
            br.close();
        }
        return createStringMessage(strings);
    }

    public static String createStringMessage(List<String> message){
        StringBuilder stringMessage = new StringBuilder();
        for(int i = 0; i < message.size(); i++){
            if(i == message.size()-1){
                stringMessage.append(message.get(i));
            }else{
                stringMessage.append(message.get(i))/*.append("\n")*/;
            }
        }
        return stringMessage.toString();
    }

    /**

    public static void main(String[] args) {

        // 画像を読み込む
        File file = new File("src/main/resources/Image/2022-03-05-01-59-59-straight.jpg");

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/traineddata"); // 言語ファイル（jpn.traineddata））の場所を指定
        tesseract.setLanguage("jpn"); // 解析言語は「日本語」を指定

        BufferedImage img = null;
        String str = "";
        try {
            img = ImageIO.read(file);

            // 解析
            str = tesseract.doOCR(img);
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }

        // 結果
        System.out.println(str);
    }

    public static void main(String[] args) {
        File imageFile = new File("src/main/resources/Image/2022-03-05-01-59-59-straight.jpg");
        //ITesseract instance = new Tesseract();
        Tesseract instance = new Tesseract();

        try {
            long start = System.currentTimeMillis();
            instance.setLanguage("jpn"); // ★英語だけならここをコメントアウト★
            String result = instance.doOCR(imageFile);

            long end = System.currentTimeMillis();
            System.out.println(result);

            System.out.println((end - start) / 1000  + "[sec]");
        } catch (TesseractException ex) {
            ex.printStackTrace();
        }
    }*/
}
