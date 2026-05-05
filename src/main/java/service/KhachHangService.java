package service;

import dto.KhachHangDto;

import java.util.List;

public interface KhachHangService {
    KhachHangDto findById(String maKhachHang);
    List<KhachHangDto> loadAll();
    List<KhachHangDto> search(String keyword);
    KhachHangDto save(KhachHangDto dto);
    KhachHangDto update(KhachHangDto dto);
    boolean delete(String maKhachHang);
    String nextId();
}