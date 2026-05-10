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
        List<String> existingIds = loadAll().stream()
                .map(SanPhamDto::getMaSanPham)
                .collect(java.util.stream.Collectors.toList());

        String newId;
        java.util.Random random = new java.util.Random();
        do {
            // Random 10 số còn lại (sau "89")
            long suffix = (long) (random.nextDouble() * 9_999_999_999L) + 1;
            newId = String.format("89%010d", suffix);
        } while (existingIds.contains(newId)); // Đảm bảo không trùng

        return newId;
    }

    private SanPham buildEntity(SanPhamDto dto) {
        LoaiSanPham loaiSanPham = loaiSanPhamDao.findById(dto.getMaLoaiSanPham());
        return SanPham.builder()
                .maSanPham(dto.getMaSanPham())
                .tenSanPham(dto.getTenSanPham())
                .loaiSanPham(loaiSanPham)
                .soLuongHienCo(dto.getSoLuongHienCo())
                .giaBan(dto.getGiaBan())
                .urlHinhAnh(dto.getUrlHinhAnh())
                .build();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}