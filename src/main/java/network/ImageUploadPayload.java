package network;

import java.io.Serializable;

/**
 * Payload gửi từ Client → Server khi upload ảnh sản phẩm.
 * Chứa bytes của file ảnh và tên file gốc (để giữ đúng phần mở rộng).
 */
public class ImageUploadPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private String originalFileName; // ví dụ: "ao_thun.png"
    private byte[] imageBytes;       // nội dung file nhị phân

    public ImageUploadPayload() {
    }

    public ImageUploadPayload(String originalFileName, byte[] imageBytes) {
        this.originalFileName = originalFileName;
        this.imageBytes = imageBytes;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
}
