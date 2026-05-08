package network;

import network.ClientSession;
import network.CommandType;
import network.Request;
import network.Response;
import network.SocketRequestClient;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

/**
 * Tien ich phia Client de tai bytes anh tu Server qua socket roi hien thi.
 * fetchBytes() la public de ImageTableCellRenderer trong Gui_SanPham dung duoc.
 */
public final class ImageLoader {

    private ImageLoader() {}

    /**
     * Tai anh tu Server bang ten file, hien thi vao JLabel.
     * Chay trong thread rieng de khong block UI.
     */
    public static void loadIntoLabel(String fileName, JLabel label, int maxWidth, int maxHeight) {
        if (fileName == null || fileName.isBlank()) {
            SwingUtilities.invokeLater(() -> {
                label.setIcon(null);
                label.setText("Chua co anh");
            });
            return;
        }

        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            SwingUtilities.invokeLater(() -> label.setText("(anh cloud cu)"));
            return;
        }

        SwingUtilities.invokeLater(() -> {
            label.setIcon(null);
            label.setText("Dang tai anh...");
        });

        Thread t = new Thread(() -> {
            try {
                byte[] bytes = fetchBytes(fileName);
                if (bytes == null || bytes.length == 0) {
                    SwingUtilities.invokeLater(() -> label.setText("Khong tim thay anh"));
                    return;
                }
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                if (img == null) {
                    SwingUtilities.invokeLater(() -> label.setText("Khong doc duoc anh"));
                    return;
                }
                int w = Math.max(maxWidth, label.getWidth());
                int h = Math.max(maxHeight, label.getHeight());
                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaled);
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(icon);
                    label.setText("");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> label.setText("Loi tai anh"));
            }
        }, "img-loader-" + fileName);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Tai bytes anh tu Server qua socket theo ten file.
     * Public de ImageTableCellRenderer dung truc tiep.
     *
     * @param fileName ten file (vd: "a1b2c3.png")
     * @return byte[] chua noi dung anh, hoac null neu khong tim thay
     */
    public static byte[] fetchBytes(String fileName) {
        Request request = new Request(
                ClientSession.getClientName(),
                CommandType.SANPHAM_FETCH_IMAGE,
                fileName
        );
        Response response = SocketRequestClient.send(request);
        if (!response.isSuccess() || response.getData() == null) {
            return null;
        }
        return (byte[]) response.getData();
    }
}