package service;

import dto.SanPhamDto;

import java.util.List;

public interface SanPhamService {
    SanPhamDto findById(String maSanPham);
    List<SanPhamDto> loadAll();
    List<SanPhamDto> search(String keyword);
    SanPhamDto save(SanPhamDto dto);
    SanPhamDto update(SanPhamDto dto);
    boolean delete(String maSanPham);
    String nextId();
}