package service.impl;

import dao.LoaiSanPhamDao;
import dao.SanPhamDao;
import dao.impl.LoaiSanPhamDaoImpl;
import dao.impl.SanPhamDaoImpl;
import dto.SanPhamDto;
import entity.LoaiSanPham;
import entity.SanPham;
import mapper.Mapper;
import service.SanPhamService;

import java.util.List;
import java.util.Locale;

public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamDao dao = new SanPhamDaoImpl();
    private final LoaiSanPhamDao loaiSanPhamDao = new LoaiSanPhamDaoImpl();

    @Override
    public SanPhamDto findById(String maSanPham) {
        return Mapper.map(dao.findById(maSanPham));
    }

    @Override
    public List<SanPhamDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<SanPhamDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaSanPham(), text)
                        || contains(item.getTenSanPham(), text)
                        || contains(item.getMaLoaiSanPham(), text)
                        || contains(item.getTenLoaiSanPham(), text))
                .toList();
    }

    @Override
    public SanPhamDto save(SanPhamDto dto) {
        SanPham entity = dao.create(buildEntity(dto));
        return findById(entity.getMaSanPham());
    }

    @Override
    public SanPhamDto update(SanPhamDto dto) {
        SanPham entity = dao.update(buildEntity(dto));
        return entity != null ? findById(entity.getMaSanPham()) : null;
    }

    @Override
    public boolean delete(String maSanPham) {
        return dao.delete(maSanPham);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(SanPhamDto::getMaSanPham)
                .filter(id -> id != null && id.startsWith("SP"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("SP%03d", number + 1))
                .findFirst()
                .orElse("SP001");
    }

    private SanPham buildEntity(SanPhamDto dto) {
        LoaiSanPham loaiSanPham = loaiSanPhamDao.findById(dto.getMaLoaiSanPham());
        double giaBan = dto.getGiaBan() > 0 ? dto.getGiaBan() : dto.getGiaNhap() * 1.5;
        return SanPham.builder()
                .maSanPham(dto.getMaSanPham())
                .tenSanPham(dto.getTenSanPham())
                .loaiSanPham(loaiSanPham)
                .soLuongHienCo(dto.getSoLuongHienCo())
                .giaNhap(dto.getGiaNhap())
                .giaBan(giaBan)
                .urlHinhAnh(dto.getUrlHinhAnh())
                .build();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}