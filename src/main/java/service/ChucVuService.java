package service;

import dto.ChucVuDto;

import java.util.List;

public interface ChucVuService {
    ChucVuDto findById(String maChucVu);
    List<ChucVuDto> loadAll();
    List<ChucVuDto> search(String keyword);
    ChucVuDto save(ChucVuDto dto);
    ChucVuDto update(ChucVuDto dto);
    boolean delete(String maChucVu);
    String nextId();
}