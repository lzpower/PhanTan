package service;

import dto.ChiTietPhieuNhapDto;

import java.util.List;

public interface ChiTietPhieuNhapService {
    List<ChiTietPhieuNhapDto> loadByPhieuNhap(String maPhieuNhap);
}