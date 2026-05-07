package mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.ChucVuDto;
import dto.ChiTietHoaDonDto;
import dto.ChiTietPhieuNhapDto;
import dto.HoaDonDto;
import dto.KhachHangDto;
import dto.KhuyenMaiDto;
import dto.LoaiSanPhamDto;
import dto.NhanVienDto;
import dto.NhaCungCapDto;
import dto.PhieuNhapDto;
import dto.SanPhamDto;
import dto.TaiKhoanDto;

import entity.ChucVu;
import entity.ChiTietHoaDon;
import entity.ChiTietPhieuNhap;
import entity.HoaDon;
import entity.KhachHang;
import entity.KhuyenMai;
import entity.LoaiSanPham;
import entity.NhanVien;
import entity.NhaCungCap;
import entity.PhieuNhap;
import entity.SanPham;
import entity.TaiKhoan;
import org.hibernate.Hibernate;

public final class Mapper {

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Mapper() {
    }

    public static <S, T> T map(S source, Class<T> target) {
        return objectMapper.convertValue(source, target);
    }

    public static LoaiSanPhamDto map(LoaiSanPham entity) {
        return map(entity, LoaiSanPhamDto.class);
    }

    public static ChucVuDto map(ChucVu entity) {
        return map(entity, ChucVuDto.class);
    }

    public static KhachHangDto map(KhachHang entity) {
        return map(entity, KhachHangDto.class);
    }

    public static NhaCungCapDto map(NhaCungCap entity) {
        return map(entity, NhaCungCapDto.class);
    }

    public static NhanVienDto map(NhanVien entity) {
        if (entity == null) {
            return null;
        }
        NhanVienDto dto = map(entity, NhanVienDto.class);
        if (dto == null) {
            return null;
        }
        if (entity.getChucVu() != null && Hibernate.isInitialized(entity.getChucVu())) {
            dto.setMaChucVu(entity.getChucVu().getMaChucVu());
            dto.setTenChucVu(entity.getChucVu().getTenChucVu());
        }
        if (entity.getTaiKhoan() != null && Hibernate.isInitialized(entity.getTaiKhoan())) {
            dto.setTenTaiKhoan(entity.getTaiKhoan().getTenDangNhap());
        }
        return dto;
    }

    public static TaiKhoanDto map(TaiKhoan entity) {
        if (entity == null) {
            return null;
        }
        TaiKhoanDto dto = map(entity, TaiKhoanDto.class);
        if (dto == null) {
            return null;
        }
        if (entity.getNhanVien() != null && Hibernate.isInitialized(entity.getNhanVien())) {
            dto.setMaNhanVien(entity.getNhanVien().getMaNhanVien());
            dto.setTenNhanVien(entity.getNhanVien().getTenNhanVien());
            dto.setEmail(entity.getNhanVien().getEmail());
        }
        if (entity.getNhanVien() != null && Hibernate.isInitialized(entity.getNhanVien())
                && entity.getNhanVien().getChucVu() != null && Hibernate.isInitialized(entity.getNhanVien().getChucVu())) {
            dto.setMaChucVu(entity.getNhanVien().getChucVu().getMaChucVu());
            dto.setTenChucVu(entity.getNhanVien().getChucVu().getTenChucVu());
        }
        return dto;
    }

    public static KhuyenMaiDto map(KhuyenMai entity) {
        return map(entity, KhuyenMaiDto.class);
    }

    public static SanPhamDto map(SanPham entity) {
        if (entity == null) {
            return null;
        }
        SanPhamDto dto = map(entity, SanPhamDto.class);
        if (dto == null) {
            return null;
        }
        if (entity.getLoaiSanPham() != null && Hibernate.isInitialized(entity.getLoaiSanPham())) {
            dto.setMaLoaiSanPham(entity.getLoaiSanPham().getMaLoaiSanPham());
            dto.setTenLoaiSanPham(entity.getLoaiSanPham().getTenLoaiSanPham());
        }
        return dto;
    }

    public static HoaDonDto map(HoaDon entity) {
        if (entity == null) {
            return null;
        }
        HoaDonDto dto = new HoaDonDto();
        dto.setMaHoaDon(entity.getMaHoaDon());
        dto.setNgayLap(entity.getNgayLap());
        dto.setTongTien(entity.getTongTien());
        if (entity.getNhanVien() != null) {
            dto.setMaNhanVien(entity.getNhanVien().getMaNhanVien());
            dto.setTenNhanVien(entity.getNhanVien().getTenNhanVien());
        }
        if (entity.getKhachHang() != null) {
            dto.setMaKhachHang(entity.getKhachHang().getMaKhachHang());
            dto.setTenKhachHang(entity.getKhachHang().getTenKhachHang());
        }
        if (entity.getKhuyenMai() != null) {
            dto.setMaKhuyenMai(entity.getKhuyenMai().getMaKhuyenMai());
            dto.setTenKhuyenMai(entity.getKhuyenMai().getTenKhuyenMai());
        }
        dto.setPhuongThucThanhToan(entity.getPhuongThucThanhToan());
        return dto;
    }

    public static PhieuNhapDto map(PhieuNhap entity) {
        if (entity == null) {
            return null;
        }
        PhieuNhapDto dto = new PhieuNhapDto();
        dto.setMaPhieuNhap(entity.getMaPhieuNhap());
        dto.setNgayNhap(entity.getNgayNhap());
        if (entity.getNhaCungCap() != null) {
            dto.setMaNhaCungCap(entity.getNhaCungCap().getMaNhaCungCap());
            dto.setTenNhaCungCap(entity.getNhaCungCap().getTenNhaCungCap());
        }
        if (entity.getNhanVien() != null) {
            dto.setMaNhanVien(entity.getNhanVien().getMaNhanVien());
            dto.setTenNhanVien(entity.getNhanVien().getTenNhanVien());
        }
        dto.setGhiChu(entity.getGhiChu());
        dto.setTongTien(entity.getTongTien());
        dto.setSoMatHang(entity.getDanhSachChiTiet() == null ? 0 : entity.getDanhSachChiTiet().size());
        return dto;
    }

    public static ChiTietHoaDonDto map(ChiTietHoaDon entity) {
        if (entity == null) {
            return null;
        }
        ChiTietHoaDonDto dto = map(entity, ChiTietHoaDonDto.class);
        if (dto == null) {
            return null;
        }
        if (entity.getId() != null) {
            dto.setMaHoaDon(entity.getId().getMaHoaDon());
            dto.setMaSanPham(entity.getId().getMaSanPham());
        }
        if (entity.getHoaDon() != null) {
            dto.setMaHoaDon(entity.getHoaDon().getMaHoaDon());
        }
        if (entity.getSanPham() != null) {
            dto.setMaSanPham(entity.getSanPham().getMaSanPham());
            dto.setTenSanPham(entity.getSanPham().getTenSanPham());
        }
        dto.setThanhTien(entity.getThanhTien());
        return dto;
    }

    public static ChiTietPhieuNhapDto map(ChiTietPhieuNhap entity) {
        if (entity == null) {
            return null;
        }
        ChiTietPhieuNhapDto dto = new ChiTietPhieuNhapDto();
        if (entity.getId() != null) {
            dto.setMaPhieuNhap(entity.getId().getMaPhieuNhap());
            dto.setMaSanPham(entity.getId().getMaSanPham());
        }
        if (entity.getPhieuNhap() != null) {
            dto.setMaPhieuNhap(entity.getPhieuNhap().getMaPhieuNhap());
        }
        if (entity.getSanPham() != null) {
            dto.setMaSanPham(entity.getSanPham().getMaSanPham());
            dto.setTenSanPham(entity.getSanPham().getTenSanPham());
        }
        dto.setSoLuong(entity.getSoLuong());
        dto.setGiaNhap(entity.getGiaNhap());
        dto.setThanhTien(entity.getThanhTien());
        return dto;
    }
}
