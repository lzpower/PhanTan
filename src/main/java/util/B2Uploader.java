package util;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URI;
import java.util.UUID;

public class B2Uploader {

    // Đã điền sẵn theo ảnh màn hình của bạn
    private static final String BUCKET_NAME = "movies-thinh";
    private static final String ENDPOINT = "https://s3.us-east-005.backblazeb2.com";

    // TODO: Điền Key của bạn vào đây (Lấy ở mục Application Keys trên web Backblaze)
    private static final String ACCESS_KEY = "005e3f62fe5be260000000003";
    private static final String SECRET_KEY = "K005g2UNwKPXd60kyqbZIPkvGj1sZrU";

    private static final String PUBLIC_URL_PREFIX = "https://f005.backblazeb2.com/file/" + BUCKET_NAME + "/";

    public static String uploadImage(File file) throws Exception {
        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .region(Region.of("us-east-005"))
                .build();

        String originalName = file.getName();
        String extension = "";
        int i = originalName.lastIndexOf('.');
        if (i > 0) {
            extension = originalName.substring(i);
        }

        // Tạo tên file ngẫu nhiên để không bị trùng (ví dụ: san-pham/abc-xyz.jpg)
        String newFileName = "san-pham/" + UUID.randomUUID().toString() + extension;

        PutObjectRequest putOb = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(newFileName)
                .build();

        s3.putObject(putOb, RequestBody.fromFile(file));

        return PUBLIC_URL_PREFIX + newFileName;
    }
}