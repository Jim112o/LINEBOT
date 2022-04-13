package AWS;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

import static Systems.File.*;

public class S3 {

    static final AWSCredentials credentials = new BasicAWSCredentials(S3_ACCESS_KEY,S3_SECRET_KEY);


    /**
     * ファイルをS3にアップロード
     *
     * @param file アップロードするファイル
     */
    public static void putObject(File file,String userId) {
        // S3クライアントの生成
        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        PutObjectRequest request = new PutObjectRequest(S3_BUCKET_NAME, "image/"+userId+"/" + S3_DOWNLOAD_IMAGE,file).withCannedAcl(CannedAccessControlList.PublicRead);

        // ファイルをアップロード
        s3Client.putObject(request);
    }

    /**
     * ファイル名を指定してS3にアップロード
     *
     * @param file アップロードするファイル
     * @param fileName ファイル名
     */
    public static void putObject(File file,String fileName,String userId) {
        // S3クライアントの生成
        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        PutObjectRequest request = new PutObjectRequest(S3_BUCKET_NAME, "image/"+ fileName +".jpg",file).withCannedAcl(CannedAccessControlList.PublicRead);

        // ファイルをアップロード
        s3Client.putObject(request);
    }


    /**
     *  S3からファイル名を指定して取得する
     *
     * @param fileName ファイル名
     * @return 取得したファイル
     */
    public File getObject(String fileName, String userId) {
        // S3クライアントの生成
        AmazonS3 s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
        // バケット名とS3のファイルパス（キー値）を指定
        GetObjectRequest request = new GetObjectRequest(S3_BUCKET_NAME, "image/"+userId+"/"+fileName);
        // ファイル保存先
        File file = new File("src/main/resources/Image/"+ userId + "/" +fileName);
        // ファイルダウンロード
        s3Client.getObject(request, file);
        return file;
    }
}
