package service.impl;

import dao.NhaCungCapDao;
import dao.NhanVienDAO;
import dao.PhieuNhapDao;
import dao.SanPhamDao;
import dao.impl.NhaCungCapDaoImpl;
import dao.impl.NhanVienDaoImpl;
import dao.impl.PhieuNhapDaoImpl;
import dao.impl.SanPhamDaoImpl;
import dto.ChiTietPhieuNhapDto;
import dto.PhieuNhapDto;
import entity.ChiTietPhieuNhap;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.PhieuNhap;
import entity.SanPham;
import mapper.Mapper;
import service.ChiTietPhieuNhapService;
import service.PhieuNhapService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhieuNhapServiceImpl implements PhieuNhapService {

    private final PhieuNhapDao dao = new PhieuNhapDaoImpl();
    private final NhaCungCapDao nhaCungCapDao = new NhaCungCapDaoImpl();
    private final NhanVienDAO nhanVienDao = new NhanVienDaoImpl();
    private final SanPhamDao sanPhamDao = new SanPhamDaoImpl();
    private final ChiTietPhieuNhapService chiTietService = new ChiTietPhieuNhapServiceImpl();

    @Override
    public PhieuNhapDto findById(String maPhieuNhap) {
        return Mapper.map(dao.findById(maPhieuNhap));
    }

    @Override
    public List<PhieuNhapDto> loadAll() {
        return dao.loadAll().stream().map(Mapper::map).toList();
    }

    @Override
    public List<PhieuNhapDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaPhieuNhap(), text)
                        || contains(item.getTenNhaCungCap(), text)
                        || contains(item.getTenNhanVien(), text)
                        || contains(item.getGhiChu(), text))
                .toList();
    }

    @Override
    public PhieuNhapDto save(PhieuNhapDto dto, List<ChiTietPhieuNhapDto> chiTietList) {
        if (chiTietList == null || chiTietList.isEmpty()) {
            throw new IllegalArgumentException("Phải có ít nhất một mặt hàng.");
        }

        NhaCungCap nhaCungCap = nhaCungCapDao.findById(dto.getMaNhaCungCap());
        if (nhaCungCap == null) {
            throw new IllegalArgumentException("Nhà cung cấp không tồn tại.");
        }

        NhanVien nhanVien = dto.getMaNhanVien() == null || dto.getMaNhanVien().isBlank() ? null : nhanVienDao.findById(dto.getMaNhanVien());
        PhieuNhap entity = PhieuNhap.builder()
                .maPhieuNhap(dto.getMaPhieuNhap())
                .ngayNhap(dto.getNgayNhap() == null ? LocalDateTime.now() : dto.getNgayNhap())
                .nhaCungCap(nhaCungCap)
                .nhanVien(nhanVien)
                .ghiChu(dto.getGhiChu())
                .tongTien(0D)
                .build();

        List<ChiTietPhieuNhap> chiTietEntities = new ArrayList<>();
        for (ChiTietPhieuNhapDto chiTietDto : chiTietList) {
            SanPham sanPham = sanPhamDao.findById(chiTietDto.getMaSanPham());
            if (sanPham == null) {
                throw new IllegalArgumentException("Sản phẩm không tồn tại: " + chiTietDto.getMaSanPham());
            }
            if (chiTietDto.getSoLuong() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
            }
            double giaNhap = chiTietDto.getGiaNhap();
            if (giaNhap <= 0) {
                throw new IllegalArgumentException("Giá nhập phải lớn hơn 0.");
            }
            ChiTietPhieuNhap chiTiet = ChiTietPhieuNhap.builder()
                    .id(new ChiTietPhieuNhap.ChiTietPhieuNhapId(dto.getMaPhieuNhap(), chiTietDto.getMaSanPham()))
                    .phieuNhap(entity)
                    .sanPham(sanPham)
                    .soLuong(chiTietDto.getSoLuong())
                    .giaNhap(giaNhap)
                    .build();
            chiTietEntities.add(chiTiet);
            entity.themChiTiet(chiTiet);
        }
        entity.capNhatTongTien();

        PhieuNhap saved = dao.create(entity);
        try {
            for (ChiTietPhieuNhap chiTiet : chiTietEntities) {
                SanPham sanPham = chiTiet.getSanPham();
                sanPham.setSoLuongHienCo(sanPham.getSoLuongHienCo() + chiTiet.getSoLuong());
                sanPhamDao.update(sanPham);
            }
        } catch (Exception ex) {
            dao.delete(saved.getMaPhieuNhap());
            throw ex;
        }
        return findById(saved.getMaPhieuNhap());
    }

    @Override
    public boolean delete(String maPhieuNhap) {
        return dao.delete(maPhieuNhap);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(PhieuNhapDto::getMaPhieuNhap)
                .filter(id -> id != null && id.startsWith("PN"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("PN%03d", number + 1))
                .findFirst()
                .orElse("PN001");
    }

    @Override
    public List<ChiTietPhieuNhapDto> loadDetails(String maPhieuNhap) {
        return chiTietService.loadByPhieuNhap(maPhieuNhap);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}