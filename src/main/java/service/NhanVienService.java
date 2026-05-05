package service;

import dto.NhanVienDto;

import java.util.List;

public interface NhanVienService {
    NhanVienDto findById(String maNhanVien);
    List<NhanVienDto> loadAll();
    List<NhanVienDto> search(String keyword);
    NhanVienDto save(NhanVienDto dto);
    NhanVienDto update(NhanVienDto dto);
    boolean delete(String maNhanVien);
    String nextId();
    String buildUsername(String maNhanVien, String tenNhanVien);
}