package service.impl;

import dao.NhaCungCapDao;
import dao.impl.NhaCungCapDaoImpl;
import dto.NhaCungCapDto;
import entity.NhaCungCap;
import mapper.Mapper;
import service.NhaCungCapService;

import java.util.List;
import java.util.Locale;

public class NhaCungCapServiceImpl implements NhaCungCapService {

    private final NhaCungCapDao dao = new NhaCungCapDaoImpl();

    @Override
    public NhaCungCapDto findById(String maNhaCungCap) {
        return Mapper.map(dao.findById(maNhaCungCap));
    }

    @Override
    public List<NhaCungCapDto> loadAll() {
        return dao.loadAll().stream().map(Mapper::map).toList();
    }

    @Override
    public List<NhaCungCapDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaNhaCungCap(), text)
                        || contains(item.getTenNhaCungCap(), text)
                        || contains(item.getDiaChi(), text)
                        || contains(item.getSoDienThoai(), text)
                        || contains(item.getEmail(), text))
                .toList();
    }

    @Override
    public NhaCungCapDto save(NhaCungCapDto dto) {
        NhaCungCap entity = NhaCungCap.builder()
                .maNhaCungCap(dto.getMaNhaCungCap())
                .tenNhaCungCap(dto.getTenNhaCungCap())
                .diaChi(dto.getDiaChi())
                .soDienThoai(dto.getSoDienThoai())
                .email(dto.getEmail())
                .build();
        return Mapper.map(dao.create(entity));
    }

    @Override
    public NhaCungCapDto update(NhaCungCapDto dto) {
        NhaCungCap entity = NhaCungCap.builder()
                .maNhaCungCap(dto.getMaNhaCungCap())
                .tenNhaCungCap(dto.getTenNhaCungCap())
                .diaChi(dto.getDiaChi())
                .soDienThoai(dto.getSoDienThoai())
                .email(dto.getEmail())
                .build();
        return Mapper.map(dao.update(entity));
    }

    @Override
    public boolean delete(String maNhaCungCap) {
        return dao.delete(maNhaCungCap);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(NhaCungCapDto::getMaNhaCungCap)
                .filter(id -> id != null && id.startsWith("NCC"))
                .map(id -> id.substring(3))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("NCC%03d", number + 1))
                .findFirst()
                .orElse("NCC001");
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}