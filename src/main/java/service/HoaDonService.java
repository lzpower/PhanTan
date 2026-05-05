package service;

import dto.HoaDonDto;

import java.util.List;

public interface HoaDonService {
    HoaDonDto findById(String maHoaDon);
    List<HoaDonDto> loadAll();
    List<HoaDonDto> search(String keyword);
    HoaDonDto save(HoaDonDto dto);
    HoaDonDto update(HoaDonDto dto);
    boolean delete(String maHoaDon);
    String nextId();
}