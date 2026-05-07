package service;

import dto.ChiTietPhieuNhapDto;
import dto.PhieuNhapDto;

import java.util.List;

public interface PhieuNhapService {
    PhieuNhapDto findById(String maPhieuNhap);
    List<PhieuNhapDto> loadAll();
    List<PhieuNhapDto> search(String keyword);
    PhieuNhapDto save(PhieuNhapDto dto, List<ChiTietPhieuNhapDto> chiTietList);
    boolean delete(String maPhieuNhap);
    String nextId();
    List<ChiTietPhieuNhapDto> loadDetails(String maPhieuNhap);
}