package service.impl;

import dao.ChucVuDao;
import dao.NhanVienDAO;
import dao.TaiKhoanDao;
import dao.impl.ChucVuDaoImpl;
import dao.impl.NhanVienDaoImpl;
import dao.impl.TaiKhoanDaoImpl;
import dto.NhanVienDto;
import entity.ChucVu;
import entity.NhanVien;
import entity.TaiKhoan;
import mapper.Mapper;
import service.NhanVienService;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienDAO nhanVienDao = new NhanVienDaoImpl();
    private final ChucVuDao chucVuDao = new ChucVuDaoImpl();
    private final TaiKhoanDao taiKhoanDao = new TaiKhoanDaoImpl();

    @Override
    public NhanVienDto findById(String maNhanVien) {
        return Mapper.map(nhanVienDao.findById(maNhanVien));
    }

    @Override
    public List<NhanVienDto> loadAll() {
        return nhanVienDao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<NhanVienDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaNhanVien(), text)
                        || contains(item.getTenNhanVien(), text)
                        || contains(item.getSoDienThoai(), text)
                        || contains(item.getEmail(), text)
                        || contains(item.getTenChucVu(), text)
                        || contains(item.getTenTaiKhoan(), text))
                .toList();
    }

    @Override
    public NhanVienDto save(NhanVienDto dto) {
        validatePhone(dto.getSoDienThoai(), null);
        ChucVu chucVu = chucVuDao.findById(dto.getMaChucVu());
        NhanVien entity = NhanVien.builder()
            .maNhanVien(dto.getMaNhanVien())
        .tenNhanVien(dto.getTenNhanVien())
            .ngaySinh(dto.getNgaySinh())
            .gioiTinh(dto.getGioiTinh())
            .email(dto.getEmail())
            .chucVu(chucVu)
            .soDienThoai(dto.getSoDienThoai())
            .build();
        entity = nhanVienDao.create(entity);

        String tenDangNhap = buildUsername(entity.getMaNhanVien(), entity.getTenNhanVien());
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(tenDangNhap);
        taiKhoan.setMatKhau("1");
        taiKhoan.setNhanVien(entity);
        try {
            taiKhoanDao.create(taiKhoan);
        } catch (Exception ex) {
            nhanVienDao.delete(entity.getMaNhanVien());
            throw ex;
        }

        entity.setTaiKhoan(taiKhoan);
        return Mapper.map(entity);
    }

    @Override
    public NhanVienDto update(NhanVienDto dto) {
        validatePhone(dto.getSoDienThoai(), dto.getMaNhanVien());
        NhanVien current = nhanVienDao.findById(dto.getMaNhanVien());
        if (current == null) {
            return null;
        }
        ChucVu chucVu = chucVuDao.findById(dto.getMaChucVu());
        current.setTenNhanVien(dto.getTenNhanVien());
        current.setNgaySinh(dto.getNgaySinh());
        current.setGioiTinh(dto.getGioiTinh());
        current.setEmail(dto.getEmail());
        current.setSoDienThoai(dto.getSoDienThoai());
        current.setChucVu(chucVu);
        return Mapper.map(nhanVienDao.update(current));
    }

    @Override
    public boolean delete(String maNhanVien) {
        TaiKhoan taiKhoan = taiKhoanDao.loadAll().stream()
                .filter(item -> item.getNhanVien() != null && Objects.equals(item.getNhanVien().getMaNhanVien(), maNhanVien))
                .findFirst()
                .orElse(null);
        if (taiKhoan != null) {
            taiKhoanDao.delete(taiKhoan.getTenDangNhap());
        }
        return nhanVienDao.delete(maNhanVien);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(NhanVienDto::getMaNhanVien)
                .filter(id -> id != null && id.startsWith("NV"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("NV%03d", number + 1))
                .findFirst()
                .orElse("NV001");
    }

    @Override
    public String buildUsername(String maNhanVien, String tenNhanVien) {
        String normalizedName = Normalizer.normalize(tenNhanVien == null ? "" : tenNhanVien, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "");
        return (maNhanVien == null ? "nv" : maNhanVien.toLowerCase(Locale.ROOT)) + "_" + normalizedName;
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void validatePhone(String soDienThoai, String maNhanVien) {
        String phone = soDienThoai == null ? "" : soDienThoai.trim();
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        boolean duplicate = loadAll().stream().anyMatch(item -> {
            String itemPhone = item.getSoDienThoai() == null ? "" : item.getSoDienThoai().trim();
            if (!itemPhone.equalsIgnoreCase(phone)) {
                return false;
            }
            // If creating (maNhanVien == null) and phone matches any existing -> duplicate
            if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
                return true;
            }
            // If updating, ignore the same employee (compare IDs trimmed, case-insensitive)
            String itemMa = item.getMaNhanVien() == null ? "" : item.getMaNhanVien().trim();
            return !maNhanVien.trim().equalsIgnoreCase(itemMa);
        });
        if (duplicate) {
            throw new IllegalArgumentException("Số điện thoại nhân viên đã tồn tại.");
        }
    }
}