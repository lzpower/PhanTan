package network;

/**
 * Tiện ích phía Client để upload ảnh lên Server qua socket.
 *
 * Cách dùng trong Gui_SanPham:
 *   String fileName = ImageUploader.upload(file);  // gửi bytes → nhận tên file
 *   dto.setUrlHinhAnh(fileName);                   // lưu tên file vào DB
 */
public final class ImageUploader {

    private ImageUploader() {}

    /**
     * Đọc bytes từ file cục bộ, gửi tới Server lưu vào resources/img/.
     *
     * @param file file ảnh người dùng vừa chọn
     * @return tên file trên server (ví dụ: "a1b2c3.png"), lưu vào cột urlHinhAnh
     * @throws Exception nếu đọc file hoặc giao tiếp socket thất bại
     */
    public static String upload(java.io.File file) throws Exception {
        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());

        network.ImageUploadPayload payload = new network.ImageUploadPayload(file.getName(), bytes);
        Request request = new Request(
                ClientSession.getClientName(),
                CommandType.SANPHAM_UPLOAD_IMAGE,
                payload
        );

        Response response = SocketRequestClient.send(request);
        if (!response.isSuccess()) {
            throw new RuntimeException("Server từ chối upload ảnh: " + response.getMessage());
        }

        return (String) response.getData(); // tên file server đã lưu
    }
}
