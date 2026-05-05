package service;

import dto.LoaiSanPhamDto;

import java.util.List;

public interface LoaiSanPhamService {
    LoaiSanPhamDto findById(String maLoaiSanPham);
    List<LoaiSanPhamDto> loadAll();
    List<LoaiSanPhamDto> search(String keyword);
    LoaiSanPhamDto save(LoaiSanPhamDto dto);
    LoaiSanPhamDto update(LoaiSanPhamDto dto);
    boolean delete(String maLoaiSanPham);
    String nextId();
}