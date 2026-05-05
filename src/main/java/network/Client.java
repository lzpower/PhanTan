package network;

import dto.KhachHangDto;
import dto.NhanVienDto;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Map;

public class Client {

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9090;

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new Request(CommandType.PING, null));
            out.flush();
            System.out.println(in.readObject());

            out.writeObject(new Request(CommandType.KHACHHANG_LOAD_ALL, null));
            out.flush();
            System.out.println(in.readObject());

            NhanVienDto sampleEmployee = new NhanVienDto();
            sampleEmployee.setMaNhanVien("NV999");
            sampleEmployee.setTenNhanVien("Demo Nhan Vien");
            sampleEmployee.setNgaySinh(LocalDate.of(2000, 1, 1));
            sampleEmployee.setGioiTinh("Nam");
            sampleEmployee.setEmail("demo@example.com");
            sampleEmployee.setMaChucVu("CV002");
            sampleEmployee.setSoDienThoai("0999999999");
            out.writeObject(new Request(CommandType.NHANVIEN_SAVE, sampleEmployee));
            out.flush();
            System.out.println(in.readObject());

            out.writeObject(new Request(CommandType.KHACHHANG_SAVE, new KhachHangDto("KH999", "Demo Khach Hang", "0988888888", 0)));
            out.flush();
            System.out.println(in.readObject());

            out.writeObject(new Request(CommandType.THONGKE_DOANHTHU, Map.of("year", LocalDate.now().getYear(), "compareYear", LocalDate.now().getYear() - 1)));
            out.flush();
            System.out.println(in.readObject());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}