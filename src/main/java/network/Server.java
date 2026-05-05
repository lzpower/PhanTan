package network;

import dto.ChucVuDto;
import dto.DashboardStatsDto;
import dto.KhachHangDto;
import dto.KhuyenMaiDto;
import dto.NhanVienDto;
import dto.TaiKhoanDto;
import service.ChucVuService;
import service.KhachHangService;
import service.KhuyenMaiService;
import service.NhanVienService;
import service.TaiKhoanService;
import service.ThongKeService;
import service.impl.ChucVuServiceImpl;
import service.impl.KhachHangServiceImpl;
import service.impl.KhuyenMaiServiceImpl;
import service.impl.NhanVienServiceImpl;
import service.impl.TaiKhoanServiceImpl;
import service.impl.ThongKeServiceImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9090;
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("CuaHangTienLoi server listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                pool.submit(new ClientHandler(socket));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final ChucVuService chucVuService = new ChucVuServiceImpl();
        private final KhachHangService khachHangService = new KhachHangServiceImpl();
        private final NhanVienService nhanVienService = new NhanVienServiceImpl();
        private final TaiKhoanService taiKhoanService = new TaiKhoanServiceImpl();
        private final KhuyenMaiService khuyenMaiService = new KhuyenMaiServiceImpl();
        private final ThongKeService thongKeService = new ThongKeServiceImpl();

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                while (true) {
                    Request request = (Request) in.readObject();
                    Response response = handle(request);
                    out.writeObject(response);
                    out.flush();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private Response handle(Request request) {
            try {
                return switch (request.getCommandType()) {
                    case PING -> new Response(true, "pong", "ok");
                    case CHUCVU_LOAD_ALL -> new Response(true, chucVuService.loadAll(), "loaded");

                    case KHACHHANG_FIND_BY_ID -> new Response(true, khachHangService.findById((String) request.getData()), "loaded");
                    case KHACHHANG_LOAD_ALL -> new Response(true, khachHangService.loadAll(), "loaded");
                    case KHACHHANG_SEARCH -> new Response(true, khachHangService.search((String) request.getData()), "loaded");
                    case KHACHHANG_SAVE -> new Response(true, khachHangService.save((KhachHangDto) request.getData()), "saved");
                    case KHACHHANG_UPDATE -> new Response(true, khachHangService.update((KhachHangDto) request.getData()), "updated");
                    case KHACHHANG_DELETE -> new Response(khachHangService.delete((String) request.getData()), null, "deleted");

                    case NHANVIEN_FIND_BY_ID -> new Response(true, nhanVienService.findById((String) request.getData()), "loaded");
                    case NHANVIEN_LOAD_ALL -> new Response(true, nhanVienService.loadAll(), "loaded");
                    case NHANVIEN_SEARCH -> new Response(true, nhanVienService.search((String) request.getData()), "loaded");
                    case NHANVIEN_SAVE -> new Response(true, nhanVienService.save((NhanVienDto) request.getData()), "saved");
                    case NHANVIEN_UPDATE -> new Response(true, nhanVienService.update((NhanVienDto) request.getData()), "updated");
                    case NHANVIEN_DELETE -> new Response(nhanVienService.delete((String) request.getData()), null, "deleted");

                    case TAIKHOAN_FIND_BY_ID -> new Response(true, taiKhoanService.findById((String) request.getData()), "loaded");
                    case TAIKHOAN_LOAD_ALL -> new Response(true, taiKhoanService.loadAll(), "loaded");
                    case TAIKHOAN_SEARCH -> new Response(true, taiKhoanService.search((String) request.getData()), "loaded");
                    case TAIKHOAN_UPDATE -> new Response(true, taiKhoanService.update((TaiKhoanDto) request.getData()), "updated");
                    case TAIKHOAN_DELETE -> new Response(taiKhoanService.delete((String) request.getData()), null, "deleted");
                    case TAIKHOAN_RESET_PASSWORD -> {
                        Map<String, String> payload = (Map<String, String>) request.getData();
                        boolean success = taiKhoanService.resetPassword(payload.get("tenDangNhap"), payload.get("matKhau"));
                        yield new Response(success, null, success ? "reset" : "not found");
                    }

                    case KHUYENMAI_FIND_BY_ID -> new Response(true, khuyenMaiService.findById((String) request.getData()), "loaded");
                    case KHUYENMAI_LOAD_ALL -> new Response(true, khuyenMaiService.loadAll(), "loaded");
                    case KHUYENMAI_SEARCH -> new Response(true, khuyenMaiService.search((String) request.getData()), "loaded");
                    case KHUYENMAI_SAVE -> new Response(true, khuyenMaiService.save((KhuyenMaiDto) request.getData()), "saved");
                    case KHUYENMAI_UPDATE -> new Response(true, khuyenMaiService.update((KhuyenMaiDto) request.getData()), "updated");
                    case KHUYENMAI_DELETE -> new Response(khuyenMaiService.delete((String) request.getData()), null, "deleted");

                    case THONGKE_DOANHTHU -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        int year = ((Number) payload.get("year")).intValue();
                        Integer compareYear = payload.get("compareYear") != null ? ((Number) payload.get("compareYear")).intValue() : null;
                        DashboardStatsDto stats = thongKeService.thongKeDoanhThu(year, compareYear);
                        yield new Response(true, stats, "loaded");
                    }
                    case THONGKE_TOP_KHACH_HANG -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        int year = ((Number) payload.get("year")).intValue();
                        int limit = ((Number) payload.getOrDefault("limit", 5)).intValue();
                        yield new Response(true, thongKeService.topKhachHang(year, limit), "loaded");
                    }
                    case THONGKE_TOP_SAN_PHAM -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        int year = ((Number) payload.get("year")).intValue();
                        int limit = ((Number) payload.getOrDefault("limit", 5)).intValue();
                        yield new Response(true, thongKeService.topSanPham(year, limit), "loaded");
                    }
                };
            } catch (Exception ex) {
                return new Response(false, null, ex.getMessage());
            }
        }
    }
}