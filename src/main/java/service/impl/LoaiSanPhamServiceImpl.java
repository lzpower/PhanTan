package service.impl;

import dao.LoaiSanPhamDao;
import dao.impl.LoaiSanPhamDaoImpl;
import dto.LoaiSanPhamDto;
import entity.LoaiSanPham;
import mapper.Mapper;
import service.LoaiSanPhamService;

import java.util.List;
import java.util.Locale;

public class LoaiSanPhamServiceImpl implements LoaiSanPhamService {

    private final LoaiSanPhamDao dao = new LoaiSanPhamDaoImpl();

    @Override
    public LoaiSanPhamDto findById(String maLoaiSanPham) {
        return Mapper.map(dao.findById(maLoaiSanPham));
    }

    @Override
    public List<LoaiSanPhamDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<LoaiSanPhamDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaLoaiSanPham(), text)
                        || contains(item.getTenLoaiSanPham(), text))
                .toList();
    }

    @Override
    public LoaiSanPhamDto save(LoaiSanPhamDto dto) {
        LoaiSanPham entity = new LoaiSanPham(dto.getMaLoaiSanPham(), dto.getTenLoaiSanPham());
        return Mapper.map(dao.create(entity));
    }

    @Override
    public LoaiSanPhamDto update(LoaiSanPhamDto dto) {
        LoaiSanPham entity = new LoaiSanPham(dto.getMaLoaiSanPham(), dto.getTenLoaiSanPham());
        return Mapper.map(dao.update(entity));
    }

    @Override
    public boolean delete(String maLoaiSanPham) {
        return dao.delete(maLoaiSanPham);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(LoaiSanPhamDto::getMaLoaiSanPham)
                .filter(id -> id != null && id.startsWith("LSP"))
                .map(id -> id.substring(3))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("LSP%03d", number + 1))
                .findFirst()
                .orElse("LSP001");
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}