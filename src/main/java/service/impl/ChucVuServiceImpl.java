package service.impl;

import dao.ChucVuDao;
import dao.impl.ChucVuDaoImpl;
import dto.ChucVuDto;
import entity.ChucVu;
import mapper.Mapper;
import service.ChucVuService;

import java.util.List;
import java.util.Locale;

public class ChucVuServiceImpl implements ChucVuService {

    private final ChucVuDao dao = new ChucVuDaoImpl();

    @Override
    public ChucVuDto findById(String maChucVu) {
        return Mapper.map(dao.findById(maChucVu));
    }

    @Override
    public List<ChucVuDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<ChucVuDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaChucVu(), text)
                        || contains(item.getTenChucVu(), text))
                .toList();
    }

    @Override
    public ChucVuDto save(ChucVuDto dto) {
        ChucVu entity = new ChucVu(dto.getMaChucVu(), dto.getTenChucVu());
        return Mapper.map(dao.create(entity));
    }

    @Override
    public ChucVuDto update(ChucVuDto dto) {
        ChucVu entity = new ChucVu(dto.getMaChucVu(), dto.getTenChucVu());
        return Mapper.map(dao.update(entity));
    }

    @Override
    public boolean delete(String maChucVu) {
        return dao.delete(maChucVu);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(ChucVuDto::getMaChucVu)
                .filter(id -> id != null && id.startsWith("CV"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("CV%03d", number + 1))
                .findFirst()
                .orElse("CV001");
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}