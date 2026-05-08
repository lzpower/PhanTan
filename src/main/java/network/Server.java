package network;

import dto.ChiTietHoaDonDto;
import dto.ChiTietPhieuNhapDto;
import dto.ChucVuDto;
import dto.DashboardStatsDto;
import dto.HoaDonDto;
import dto.KhachHangDto;
import dto.KhuyenMaiDto;
import dto.LoaiSanPhamDto;
import dto.NhaCungCapDto;
import dto.NhanVienDto;
import dto.PhieuNhapDto;
import dto.SanPhamDto;
import dto.TaiKhoanDto;
import entity.ChiTietHoaDon;
import service.ChucVuService;
import service.ChiTietHoaDonService;
import service.ChiTietPhieuNhapService;
import service.HoaDonService;
import service.KhachHangService;
import service.KhuyenMaiService;
import service.LoaiSanPhamService;
import service.NhaCungCapService;
import service.NhanVienService;
import service.PhieuNhapService;
import service.SanPhamService;
import service.TaiKhoanService;
import service.ThongKeService;
import service.impl.ChucVuServiceImpl;
import service.impl.ChiTietHoaDonServiceImpl;
import service.impl.ChiTietPhieuNhapServiceImpl;
import service.impl.HoaDonServiceImpl;
import service.impl.KhachHangServiceImpl;
import service.impl.KhuyenMaiServiceImpl;
import service.impl.LoaiSanPhamServiceImpl;
import service.impl.NhaCungCapServiceImpl;
import service.impl.NhanVienServiceImpl;
import service.impl.PhieuNhapServiceImpl;
import service.impl.SanPhamServiceImpl;
import service.impl.TaiKhoanServiceImpl;
import service.impl.ThongKeServiceImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
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
        private final LoaiSanPhamService loaiSanPhamService = new LoaiSanPhamServiceImpl();
        private final NhaCungCapService nhaCungCapService = new NhaCungCapServiceImpl();
        private final SanPhamService sanPhamService = new SanPhamServiceImpl();
        private final HoaDonService hoaDonService = new HoaDonServiceImpl();
        private final PhieuNhapService phieuNhapService = new PhieuNhapServiceImpl();
        private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonServiceImpl();
        private final ChiTietPhieuNhapService chiTietPhieuNhapService = new ChiTietPhieuNhapServiceImpl();
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
                    logRequest(request, response);
                    out.writeObject(response);
                    out.flush();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private Response handle(Request request) {
            try {
                if (request == null || request.getCommandType() == null) {
                    return fail("request is null");
                }

                return switch (request.getCommandType()) {
                    case PING -> ok("pong", "ok");

                    case CHUCVU_FIND_BY_ID -> ok(chucVuService.findById((String) request.getData()), "loaded");
                    case CHUCVU_LOAD_ALL -> ok(chucVuService.loadAll(), "loaded");
                    case CHUCVU_SEARCH -> ok(chucVuService.search((String) request.getData()), "loaded");
                    case CHUCVU_SAVE -> ok(chucVuService.save((ChucVuDto) request.getData()), "saved");
                    case CHUCVU_UPDATE -> ok(chucVuService.update((ChucVuDto) request.getData()), "updated");
                    case CHUCVU_DELETE -> new Response(chucVuService.delete((String) request.getData()), null, "deleted");
                    case CHUCVU_NEXT_ID -> ok(chucVuService.nextId(), "generated");

                    case LOAISANPHAM_FIND_BY_ID -> ok(loaiSanPhamService.findById((String) request.getData()), "loaded");
                    case LOAISANPHAM_LOAD_ALL -> ok(loaiSanPhamService.loadAll(), "loaded");
                    case LOAISANPHAM_SEARCH -> ok(loaiSanPhamService.search((String) request.getData()), "loaded");
                    case LOAISANPHAM_SAVE -> ok(loaiSanPhamService.save((LoaiSanPhamDto) request.getData()), "saved");
                    case LOAISANPHAM_UPDATE -> ok(loaiSanPhamService.update((LoaiSanPhamDto) request.getData()), "updated");
                    case LOAISANPHAM_DELETE -> new Response(loaiSanPhamService.delete((String) request.getData()), null, "deleted");
                    case LOAISANPHAM_NEXT_ID -> ok(loaiSanPhamService.nextId(), "generated");

                    case NHACUNGCAP_FIND_BY_ID -> ok(nhaCungCapService.findById((String) request.getData()), "loaded");
                    case NHACUNGCAP_LOAD_ALL -> ok(nhaCungCapService.loadAll(), "loaded");
                    case NHACUNGCAP_SEARCH -> ok(nhaCungCapService.search((String) request.getData()), "loaded");
                    case NHACUNGCAP_SAVE -> ok(nhaCungCapService.save((NhaCungCapDto) request.getData()), "saved");
                    case NHACUNGCAP_UPDATE -> ok(nhaCungCapService.update((NhaCungCapDto) request.getData()), "updated");
                    case NHACUNGCAP_DELETE -> new Response(nhaCungCapService.delete((String) request.getData()), null, "deleted");
                    case NHACUNGCAP_NEXT_ID -> ok(nhaCungCapService.nextId(), "generated");

                    case SANPHAM_FIND_BY_ID -> ok(sanPhamService.findById((String) request.getData()), "loaded");
                    case SANPHAM_LOAD_ALL -> ok(sanPhamService.loadAll(), "loaded");
                    case SANPHAM_SEARCH -> ok(sanPhamService.search((String) request.getData()), "loaded");
                    case SANPHAM_SAVE -> ok(sanPhamService.save((SanPhamDto) request.getData()), "saved");
                    case SANPHAM_UPDATE -> ok(sanPhamService.update((SanPhamDto) request.getData()), "updated");
                    case SANPHAM_DELETE -> new Response(sanPhamService.delete((String) request.getData()), null, "deleted");
                    case SANPHAM_NEXT_ID -> ok(sanPhamService.nextId(), "generated");

                    case HOADON_FIND_BY_ID -> ok(hoaDonService.findById((String) request.getData()), "loaded");
                    case HOADON_LOAD_ALL -> ok(hoaDonService.loadAll(), "loaded");
                    case HOADON_SEARCH -> ok(hoaDonService.search((String) request.getData()), "loaded");
                    case HOADON_SAVE -> ok(hoaDonService.save((HoaDonDto) request.getData()), "saved");
                    case HOADON_UPDATE -> ok(hoaDonService.update((HoaDonDto) request.getData()), "updated");
                    case HOADON_DELETE -> new Response(hoaDonService.delete((String) request.getData()), null, "deleted");
                    case HOADON_NEXT_ID -> ok(hoaDonService.nextId(), "generated");

                    case PHIEUNHAP_FIND_BY_ID -> ok(phieuNhapService.findById((String) request.getData()), "loaded");
                    case PHIEUNHAP_LOAD_ALL -> ok(phieuNhapService.loadAll(), "loaded");
                    case PHIEUNHAP_SEARCH -> ok(phieuNhapService.search((String) request.getData()), "loaded");
                    case PHIEUNHAP_SAVE -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        yield ok(phieuNhapService.save((PhieuNhapDto) payload.get("dto"), (List<ChiTietPhieuNhapDto>) payload.get("chiTietList")), "saved");
                    }
                    case PHIEUNHAP_DELETE -> new Response(phieuNhapService.delete((String) request.getData()), null, "deleted");
                    case PHIEUNHAP_NEXT_ID -> ok(phieuNhapService.nextId(), "generated");
                    case PHIEUNHAP_LOAD_DETAILS -> ok(phieuNhapService.loadDetails((String) request.getData()), "loaded");

                    case CHITIETHOADON_FIND_BY_ID -> ok(chiTietHoaDonService.findById((ChiTietHoaDon.ChiTietHoaDonId) request.getData()), "loaded");
                    case CHITIETHOADON_LOAD_ALL -> ok(chiTietHoaDonService.loadAll(), "loaded");
                    case CHITIETHOADON_LOAD_BY_HOA_DON -> ok(chiTietHoaDonService.loadByHoaDon((String) request.getData()), "loaded");
                    case CHITIETHOADON_SAVE -> ok(chiTietHoaDonService.save((ChiTietHoaDonDto) request.getData()), "saved");
                    case CHITIETHOADON_UPDATE -> ok(chiTietHoaDonService.update((ChiTietHoaDonDto) request.getData()), "updated");
                    case CHITIETHOADON_DELETE -> new Response(chiTietHoaDonService.delete((ChiTietHoaDon.ChiTietHoaDonId) request.getData()), null, "deleted");

                    case CHITIETPHIEUNHAP_LOAD_BY_PHIEU_NHAP -> ok(chiTietPhieuNhapService.loadByPhieuNhap((String) request.getData()), "loaded");

                    case KHACHHANG_FIND_BY_ID -> ok(khachHangService.findById((String) request.getData()), "loaded");
                    case KHACHHANG_LOAD_ALL -> ok(khachHangService.loadAll(), "loaded");
                    case KHACHHANG_SEARCH -> ok(khachHangService.search((String) request.getData()), "loaded");
                    case KHACHHANG_SAVE -> ok(khachHangService.save((KhachHangDto) request.getData()), "saved");
                    case KHACHHANG_UPDATE -> ok(khachHangService.update((KhachHangDto) request.getData()), "updated");
                    case KHACHHANG_DELETE -> new Response(khachHangService.delete((String) request.getData()), null, "deleted");
                    case KHACHHANG_NEXT_ID -> ok(khachHangService.nextId(), "generated");

                    case NHANVIEN_FIND_BY_ID -> ok(nhanVienService.findById((String) request.getData()), "loaded");
                    case NHANVIEN_LOAD_ALL -> ok(nhanVienService.loadAll(), "loaded");
                    case NHANVIEN_SEARCH -> ok(nhanVienService.search((String) request.getData()), "loaded");
                    case NHANVIEN_SAVE -> ok(nhanVienService.save((NhanVienDto) request.getData()), "saved");
                    case NHANVIEN_UPDATE -> ok(nhanVienService.update((NhanVienDto) request.getData()), "updated");
                    case NHANVIEN_DELETE -> new Response(nhanVienService.delete((String) request.getData()), null, "deleted");
                    case NHANVIEN_NEXT_ID -> ok(nhanVienService.nextId(), "generated");
                    case NHANVIEN_BUILD_USERNAME -> {
                        Map<String, String> payload = (Map<String, String>) request.getData();
                        yield ok(nhanVienService.buildUsername(payload.get("maNhanVien"), payload.get("tenNhanVien")), "generated");
                    }

                    case TAIKHOAN_FIND_BY_ID -> ok(taiKhoanService.findById((String) request.getData()), "loaded");
                    case TAIKHOAN_LOAD_ALL -> ok(taiKhoanService.loadAll(), "loaded");
                    case TAIKHOAN_SEARCH -> ok(taiKhoanService.search((String) request.getData()), "loaded");
                    case TAIKHOAN_UPDATE -> ok(taiKhoanService.update((TaiKhoanDto) request.getData()), "updated");
                    case TAIKHOAN_DELETE -> new Response(taiKhoanService.delete((String) request.getData()), null, "deleted");
                    case TAIKHOAN_CREATE_DEFAULT -> {
                        Map<String, String> payload = (Map<String, String>) request.getData();
                        yield ok(taiKhoanService.createDefaultAccount(
                                payload.get("tenDangNhap"),
                                payload.get("matKhau"),
                                payload.get("maChucVu"),
                                payload.get("maNhanVien")), "created");
                    }
                    case TAIKHOAN_RESET_PASSWORD -> {
                        Map<String, String> payload = (Map<String, String>) request.getData();
                        boolean success = taiKhoanService.resetPassword(payload.get("tenDangNhap"), payload.get("matKhau"));
                        yield new Response(success, null, success ? "reset" : "not found");
                    }

                    case KHUYENMAI_FIND_BY_ID -> ok(khuyenMaiService.findById((String) request.getData()), "loaded");
                    case KHUYENMAI_LOAD_ALL -> ok(khuyenMaiService.loadAll(), "loaded");
                    case KHUYENMAI_SEARCH -> ok(khuyenMaiService.search((String) request.getData()), "loaded");
                    case KHUYENMAI_SAVE -> ok(khuyenMaiService.save((KhuyenMaiDto) request.getData()), "saved");
                    case KHUYENMAI_UPDATE -> ok(khuyenMaiService.update((KhuyenMaiDto) request.getData()), "updated");
                    case KHUYENMAI_DELETE -> new Response(khuyenMaiService.delete((String) request.getData()), null, "deleted");
                    case KHUYENMAI_NEXT_ID -> ok(khuyenMaiService.nextId(), "generated");

                                        case THONGKE_GET_STATS -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        yield ok(thongKeService.getStats((LocalDate) payload.get("start"), (LocalDate) payload.get("end"), (String) payload.get("employeeId")), "loaded");
                    }
                    case THONGKE_GET_REVENUE_BY_DATE_RANGE -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        yield ok(thongKeService.getRevenueByDateRange((LocalDate) payload.get("start"), (LocalDate) payload.get("end")), "loaded");
                    }
                    case THONGKE_GET_TOP_PRODUCTS -> {
                        Map<String, Object> payload = (Map<String, Object>) request.getData();
                        int limit = payload.get("limit") != null ? ((Number) payload.get("limit")).intValue() : 10;
                        yield ok(thongKeService.getTopProducts((LocalDate) payload.get("start"), (LocalDate) payload.get("end"), limit), "loaded");
                    }
                    case THONGKE_GET_LOW_STOCK_PRODUCTS -> ok(thongKeService.getLowStockProducts(((Number) request.getData()).intValue()), "loaded");
                };
            } catch (Exception ex) {
                return fail(ex.getMessage());
            }
        }

        private void logRequest(Request request, Response response) {
            String clientName = request != null && request.getClientName() != null && !request.getClientName().isBlank()
                    ? request.getClientName().trim()
                    : "unknown-client";
            String command = request != null && request.getCommandType() != null ? request.getCommandType().name() : "UNKNOWN";
            System.out.printf("[client=%s][remote=%s] request=%s data=%s%n", clientName, socket.getRemoteSocketAddress(), command, request == null ? null : request.getData());
            System.out.printf("[client=%s][remote=%s] response=%s%n", clientName, socket.getRemoteSocketAddress(), response);
        }

        private Response ok(Object data, String message) {
            return new Response(true, data, message);
        }

        private Response fail(String message) {
            return new Response(false, null, message);
        }
    }
}


