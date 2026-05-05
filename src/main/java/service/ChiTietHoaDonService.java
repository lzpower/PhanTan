package service;

import dto.ChiTietHoaDonDto;
import entity.ChiTietHoaDon;

import java.util.List;

public interface ChiTietHoaDonService {
    ChiTietHoaDonDto findById(ChiTietHoaDon.ChiTietHoaDonId id);
    List<ChiTietHoaDonDto> loadAll();
    List<ChiTietHoaDonDto> loadByHoaDon(String maHoaDon);
    ChiTietHoaDonDto save(ChiTietHoaDonDto dto);
    ChiTietHoaDonDto update(ChiTietHoaDonDto dto);
    boolean delete(ChiTietHoaDon.ChiTietHoaDonId id);
}