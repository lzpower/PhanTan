package network;

import dto.ChiTietHoaDonDto;
import dto.ChiTietPhieuNhapDto;
import dto.ChucVuDto;
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RemoteServiceProxy {

    private RemoteServiceProxy() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(),
                new Class<?>[]{serviceType},
                new RemoteInvocationHandler(serviceType));
    }

    private static final class RemoteInvocationHandler implements InvocationHandler {
        private final Class<?> serviceType;

        private RemoteInvocationHandler(Class<?> serviceType) {
            this.serviceType = serviceType;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "RemoteServiceProxy{" + serviceType.getSimpleName() + '}';
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }

            CommandType commandType = mapCommand(method.getName(), args);
            Object payload = buildPayload(method.getName(), args);
            Response response = SocketRequestClient.send(new Request(ClientSession.getClientName(), commandType, payload));
            if (!response.isSuccess()) {
                if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                    return false;
                }
                if (method.getReturnType() == void.class) {
                    return null;
                }
                throw new RuntimeException(response.getMessage());
            }

            if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                return response.getData() instanceof Boolean b ? b : response.isSuccess();
            }
            return response.getData();
        }

        private CommandType mapCommand(String methodName, Object[] args) {
            String prefix = prefixOf(serviceType);
            return switch (prefix) {
                case "CHUCVU" -> mapBasic(prefix, methodName);
                case "LOAISANPHAM" -> mapBasic(prefix, methodName);
                case "NHACUNGCAP" -> mapBasic(prefix, methodName);
                case "SANPHAM" -> mapBasic(prefix, methodName);
                case "HOADON" -> mapBasic(prefix, methodName);
                case "PHIEUNHAP" -> mapPhieuNhap(methodName, args);
                case "CHITIETHOADON" -> mapChiTietHoaDon(methodName, args);
                case "CHITIETPHIEUNHAP" -> mapChiTietPhieuNhap(methodName);
                case "KHACHHANG" -> mapBasic(prefix, methodName);
                case "NHANVIEN" -> mapNhanVien(methodName);
                case "TAIKHOAN" -> mapTaiKhoan(methodName);
                case "KHUYENMAI" -> mapBasic(prefix, methodName);
                case "THONGKE" -> mapThongKe(methodName);
                default -> throw new IllegalArgumentException("Unsupported service: " + serviceType.getName());
            };
        }

        private String prefixOf(Class<?> type) {
            if (type == ChucVuService.class) return "CHUCVU";
            if (type == LoaiSanPhamService.class) return "LOAISANPHAM";
            if (type == NhaCungCapService.class) return "NHACUNGCAP";
            if (type == SanPhamService.class) return "SANPHAM";
            if (type == HoaDonService.class) return "HOADON";
            if (type == PhieuNhapService.class) return "PHIEUNHAP";
            if (type == ChiTietHoaDonService.class) return "CHITIETHOADON";
            if (type == ChiTietPhieuNhapService.class) return "CHITIETPHIEUNHAP";
            if (type == KhachHangService.class) return "KHACHHANG";
            if (type == NhanVienService.class) return "NHANVIEN";
            if (type == TaiKhoanService.class) return "TAIKHOAN";
            if (type == KhuyenMaiService.class) return "KHUYENMAI";
            if (type == ThongKeService.class) return "THONGKE";
            throw new IllegalArgumentException("Unsupported service type: " + type.getName());
        }

        private CommandType mapBasic(String prefix, String methodName) {
            return switch (methodName) {
                case "findById" -> CommandType.valueOf(prefix + "_FIND_BY_ID");
                case "loadAll" -> CommandType.valueOf(prefix + "_LOAD_ALL");
                case "search" -> CommandType.valueOf(prefix + "_SEARCH");
                case "save" -> CommandType.valueOf(prefix + "_SAVE");
                case "update" -> CommandType.valueOf(prefix + "_UPDATE");
                case "delete" -> CommandType.valueOf(prefix + "_DELETE");
                case "nextId" -> CommandType.valueOf(prefix + "_NEXT_ID");
                default -> throw new IllegalArgumentException("Unsupported method: " + serviceType.getSimpleName() + "." + methodName);
            };
        }

        private CommandType mapPhieuNhap(String methodName, Object[] args) {
            return switch (methodName) {
                case "findById" -> CommandType.PHIEUNHAP_FIND_BY_ID;
                case "loadAll" -> CommandType.PHIEUNHAP_LOAD_ALL;
                case "search" -> CommandType.PHIEUNHAP_SEARCH;
                case "save" -> CommandType.PHIEUNHAP_SAVE;
                case "delete" -> CommandType.PHIEUNHAP_DELETE;
                case "nextId" -> CommandType.PHIEUNHAP_NEXT_ID;
                case "loadDetails" -> CommandType.PHIEUNHAP_LOAD_DETAILS;
                default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
            };
        }

        private CommandType mapChiTietHoaDon(String methodName, Object[] args) {
            return switch (methodName) {
                case "findById" -> CommandType.CHITIETHOADON_FIND_BY_ID;
                case "loadAll" -> CommandType.CHITIETHOADON_LOAD_ALL;
                case "loadByHoaDon" -> CommandType.CHITIETHOADON_LOAD_BY_HOA_DON;
                case "save" -> CommandType.CHITIETHOADON_SAVE;
                case "update" -> CommandType.CHITIETHOADON_UPDATE;
                case "delete" -> CommandType.CHITIETHOADON_DELETE;
                default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
            };
        }

        private CommandType mapChiTietPhieuNhap(String methodName) {
            if ("loadByPhieuNhap".equals(methodName)) {
                return CommandType.CHITIETPHIEUNHAP_LOAD_BY_PHIEU_NHAP;
            }
            throw new IllegalArgumentException("Unsupported method: " + methodName);
        }

        private CommandType mapNhanVien(String methodName) {
            return switch (methodName) {
                case "findById" -> CommandType.NHANVIEN_FIND_BY_ID;
                case "loadAll" -> CommandType.NHANVIEN_LOAD_ALL;
                case "search" -> CommandType.NHANVIEN_SEARCH;
                case "save" -> CommandType.NHANVIEN_SAVE;
                case "update" -> CommandType.NHANVIEN_UPDATE;
                case "delete" -> CommandType.NHANVIEN_DELETE;
                case "nextId" -> CommandType.NHANVIEN_NEXT_ID;
                case "buildUsername" -> CommandType.NHANVIEN_BUILD_USERNAME;
                default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
            };
        }

        private CommandType mapTaiKhoan(String methodName) {
            return switch (methodName) {
                case "findById" -> CommandType.TAIKHOAN_FIND_BY_ID;
                case "loadAll" -> CommandType.TAIKHOAN_LOAD_ALL;
                case "search" -> CommandType.TAIKHOAN_SEARCH;
                case "update" -> CommandType.TAIKHOAN_UPDATE;
                case "delete" -> CommandType.TAIKHOAN_DELETE;
                case "createDefaultAccount" -> CommandType.TAIKHOAN_CREATE_DEFAULT;
                case "resetPassword" -> CommandType.TAIKHOAN_RESET_PASSWORD;
                default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
            };
        }

        private CommandType mapThongKe(String methodName) {
            return switch (methodName) {
                case "thongKeDoanhThu" -> CommandType.THONGKE_DOANHTHU;
                case "topKhachHang" -> CommandType.THONGKE_TOP_KHACH_HANG;
                case "topSanPham" -> CommandType.THONGKE_TOP_SAN_PHAM;
                default -> throw new IllegalArgumentException("Unsupported method: " + methodName);
            };
        }

        private Object buildPayload(String methodName, Object[] args) {
            if (args == null || args.length == 0) {
                return null;
            }
            if (serviceType == PhieuNhapService.class && "save".equals(methodName)) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("dto", args[0]);
                payload.put("chiTietList", args.length > 1 ? args[1] : List.of());
                return payload;
            }
            if (serviceType == ChiTietHoaDonService.class && ("findById".equals(methodName) || "delete".equals(methodName))) {
                return args[0];
            }
            if (serviceType == ChiTietHoaDonService.class && args.length == 1 && ("save".equals(methodName) || "update".equals(methodName) || "loadByHoaDon".equals(methodName))) {
                return args[0];
            }
            if (serviceType == TaiKhoanService.class && "createDefaultAccount".equals(methodName)) {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("tenDangNhap", String.valueOf(args[0]));
                payload.put("matKhau", String.valueOf(args[1]));
                payload.put("maChucVu", String.valueOf(args[2]));
                payload.put("maNhanVien", String.valueOf(args[3]));
                return payload;
            }
            if (serviceType == TaiKhoanService.class && "resetPassword".equals(methodName)) {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("tenDangNhap", String.valueOf(args[0]));
                payload.put("matKhau", String.valueOf(args[1]));
                return payload;
            }
            if (serviceType == NhanVienService.class && "buildUsername".equals(methodName)) {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("maNhanVien", String.valueOf(args[0]));
                payload.put("tenNhanVien", String.valueOf(args[1]));
                return payload;
            }
            if (serviceType == PhieuNhapService.class && "loadDetails".equals(methodName)) {
                return args[0];
            }
            if (serviceType == ThongKeService.class) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("year", args[0]);
                if (args.length > 1) {
                    payload.put("compareYear", args[1]);
                }
                if (args.length > 2) {
                    payload.put("limit", args[1]);
                }
                return payload;
            }
            if (serviceType == NhanVienService.class && "save".equals(methodName)) {
                return args[0];
            }
            if (serviceType == PhieuNhapService.class && "delete".equals(methodName)) {
                return args[0];
            }
            if (serviceType == ChiTietPhieuNhapService.class && "loadByPhieuNhap".equals(methodName)) {
                return args[0];
            }
            if (serviceType == ChiTietHoaDonService.class && "findById".equals(methodName)) {
                return args[0];
            }
            if (serviceType == ChiTietHoaDonService.class && "delete".equals(methodName)) {
                return args[0];
            }
            if (serviceType == PhieuNhapService.class && "save".equals(methodName)) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("dto", args[0]);
                payload.put("chiTietList", args.length > 1 ? args[1] : List.of());
                return payload;
            }
            return args.length == 1 ? args[0] : List.of(args);
        }
    }
}