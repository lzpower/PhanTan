package service.impl;

import dao.KhuyenMaiDao;
import dao.impl.KhuyenMaiDaoImpl;
import dto.KhuyenMaiDto;
import entity.KhuyenMai;
import mapper.Mapper;
import service.KhuyenMaiService;

import java.util.List;
import java.util.Locale;

public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    private final KhuyenMaiDao dao = new KhuyenMaiDaoImpl();

    @Override
    public KhuyenMaiDto findById(String maKhuyenMai) {
        return Mapper.map(dao.findById(maKhuyenMai));
    }

    @Override
    public List<KhuyenMaiDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<KhuyenMaiDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaKhuyenMai(), text)
                        || contains(item.getTenKhuyenMai(), text))
                .toList();
    }

    @Override
    public KhuyenMaiDto save(KhuyenMaiDto dto) {
        KhuyenMai entity = KhuyenMai.builder()
                .maKhuyenMai(dto.getMaKhuyenMai())
                .tenKhuyenMai(dto.getTenKhuyenMai())
                .giaTriKhuyenMai(dto.getGiaTriKhuyenMai())
                .build();
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());
        return Mapper.map(dao.create(entity));
    }

    @Override
    public KhuyenMaiDto update(KhuyenMaiDto dto) {
        KhuyenMai entity = KhuyenMai.builder()
                .maKhuyenMai(dto.getMaKhuyenMai())
                .tenKhuyenMai(dto.getTenKhuyenMai())
                .giaTriKhuyenMai(dto.getGiaTriKhuyenMai())
                .build();
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());
        return Mapper.map(dao.update(entity));
    }

    @Override
    public boolean delete(String maKhuyenMai) {
        return dao.delete(maKhuyenMai);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(KhuyenMaiDto::getMaKhuyenMai)
                .filter(id -> id != null && id.startsWith("KM"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("KM%03d", number + 1))
                .findFirst()
                .orElse("KM001");
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}