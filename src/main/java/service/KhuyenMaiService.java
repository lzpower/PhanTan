package service;

import dto.KhuyenMaiDto;

import java.util.List;

public interface KhuyenMaiService {
    KhuyenMaiDto findById(String maKhuyenMai);
    List<KhuyenMaiDto> loadAll();
    List<KhuyenMaiDto> search(String keyword);
    KhuyenMaiDto save(KhuyenMaiDto dto);
    KhuyenMaiDto update(KhuyenMaiDto dto);
    boolean delete(String maKhuyenMai);
    String nextId();
}