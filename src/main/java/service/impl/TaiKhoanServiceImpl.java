package service.impl;

import dao.NhanVienDAO;
import dao.TaiKhoanDao;
import dao.impl.NhanVienDaoImpl;
import dao.impl.TaiKhoanDaoImpl;
import dto.TaiKhoanDto;
import entity.NhanVien;
import entity.TaiKhoan;
import mapper.Mapper;
import service.TaiKhoanService;

import java.util.List;
import java.util.Locale;

public class TaiKhoanServiceImpl implements TaiKhoanService {

    private final TaiKhoanDao taiKhoanDao = new TaiKhoanDaoImpl();
    private final NhanVienDAO nhanVienDao = new NhanVienDaoImpl();

    @Override
    public TaiKhoanDto findById(String tenDangNhap) {

        return Mapper.map(taiKhoanDao.findById(tenDangNhap));
    }

    @Override
    public List<TaiKhoanDto> loadAll() {
        return taiKhoanDao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<TaiKhoanDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
            .filter(item -> text.isEmpty()
                || contains(item.getTenDangNhap(), text)
                || contains(item.getEmail(), text)
                || contains(item.getMaChucVu(), text)
                || contains(item.getTenChucVu(), text)
                || contains(item.getTenNhanVien(), text))
            .toList();
    }

    @Override
    public TaiKhoanDto update(TaiKhoanDto dto) {
        TaiKhoan entity = taiKhoanDao.findById(dto.getTenDangNhap());
        if (entity == null) {
            return null;
        }
        entity.setMatKhau(dto.getMatKhau());
        // role is determined by the associated NhanVien.chucVu; no role field on TaiKhoan
        if (dto.getMaNhanVien() != null) {
            entity.setNhanVien(nhanVienDao.findById(dto.getMaNhanVien()));
        }
        taiKhoanDao.update(entity);
        return findById(dto.getTenDangNhap());
    }

    @Override
    public boolean delete(String tenDangNhap) {
        return taiKhoanDao.delete(tenDangNhap);
    }

    @Override
    public TaiKhoanDto createDefaultAccount(String tenDangNhap, String matKhau, String maChucVu, String maNhanVien) {
        TaiKhoan entity = new TaiKhoan();
        entity.setTenDangNhap(tenDangNhap);
        entity.setMatKhau(matKhau);
        // role omitted: permissions come from employee's ChucVu
        entity.setNhanVien(nhanVienDao.findById(maNhanVien));
        taiKhoanDao.create(entity);
        return findById(tenDangNhap);
    }

    @Override
    public boolean resetPassword(String tenDangNhap, String passwordMoi) {
        TaiKhoan entity = taiKhoanDao.findById(tenDangNhap);
        if (entity == null) {
            return false;
        }
        entity.setMatKhau(passwordMoi);
        taiKhoanDao.update(entity);
        return true;
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}