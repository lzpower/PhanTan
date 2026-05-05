package service;

import dto.TaiKhoanDto;

import java.util.List;

public interface TaiKhoanService {
    TaiKhoanDto findById(String tenDangNhap);
    List<TaiKhoanDto> loadAll();
    List<TaiKhoanDto> search(String keyword);
    TaiKhoanDto update(TaiKhoanDto dto);
    boolean delete(String tenDangNhap);
    TaiKhoanDto createDefaultAccount(String tenDangNhap, String matKhau, String maChucVu, String maNhanVien);
    boolean resetPassword(String tenDangNhap, String passwordMoi);
}