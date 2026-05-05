package service.impl;

import dao.KhachHangDao;
import dao.impl.KhachHangDaoImpl;
import dto.KhachHangDto;
import entity.KhachHang;
import mapper.Mapper;
import service.KhachHangService;

import java.util.List;

public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangDao dao = new KhachHangDaoImpl();

    @Override
    public KhachHangDto findById(String maKhachHang) {
        return Mapper.map(dao.findById(maKhachHang));
    }

    @Override
    public List<KhachHangDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<KhachHangDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase();
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaKhachHang(), text)
                        || contains(item.getTenKhachHang(), text)
                        || contains(item.getSoDienThoai(), text))
                .toList();
    }

    @Override
    public KhachHangDto save(KhachHangDto dto) {
        validatePhone(dto.getSoDienThoai(), null);
        KhachHang entity = new KhachHang(dto.getMaKhachHang(), dto.getTenKhachHang(), dto.getSoDienThoai(), dto.getSoDiem());
        return Mapper.map(dao.create(entity));
    }

    @Override
    public KhachHangDto update(KhachHangDto dto) {
        validatePhone(dto.getSoDienThoai(), dto.getMaKhachHang());
        KhachHang entity = new KhachHang(dto.getMaKhachHang(), dto.getTenKhachHang(), dto.getSoDienThoai(), dto.getSoDiem());
        return Mapper.map(dao.update(entity));
    }

    @Override
    public boolean delete(String maKhachHang) {
        return dao.delete(maKhachHang);
    }

    @Override
    public String nextId() {
        return loadAll().stream()
                .map(KhachHangDto::getMaKhachHang)
                .filter(id -> id != null && id.startsWith("KH"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("KH%03d", number + 1))
                .findFirst()
                .orElse("KH001");
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private void validatePhone(String soDienThoai, String maKhachHang) {
        String phone = soDienThoai == null ? "" : soDienThoai.trim();
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        boolean duplicate = dao.loadAll().stream()
                .anyMatch(item -> item.getSoDienThoai() != null
                        && item.getSoDienThoai().trim().equalsIgnoreCase(phone)
                        && (maKhachHang == null || !maKhachHang.equals(item.getMaKhachHang())));
        if (duplicate) {
            throw new IllegalArgumentException("Số điện thoại khách hàng đã tồn tại.");
        }
    }
}