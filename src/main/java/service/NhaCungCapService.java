package service;

import dto.NhaCungCapDto;

import java.util.List;

public interface NhaCungCapService {
    NhaCungCapDto findById(String maNhaCungCap);
    List<NhaCungCapDto> loadAll();
    List<NhaCungCapDto> search(String keyword);
    NhaCungCapDto save(NhaCungCapDto dto);
    NhaCungCapDto update(NhaCungCapDto dto);
    boolean delete(String maNhaCungCap);
    String nextId();
}