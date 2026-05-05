package service.impl;

import dao.HoaDonDao;
import dao.KhachHangDao;
import dao.KhuyenMaiDao;
import dao.NhanVienDAO;
import dao.impl.HoaDonDaoImpl;
import dao.impl.KhachHangDaoImpl;
import dao.impl.KhuyenMaiDaoImpl;
import dao.impl.NhanVienDaoImpl;
import dto.HoaDonDto;
import entity.HoaDon;
import mapper.Mapper;
import service.HoaDonService;

import java.util.List;
import java.util.Locale;

public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonDao dao = new HoaDonDaoImpl();
    private final NhanVienDAO nhanVienDao = new NhanVienDaoImpl();
    private final KhachHangDao khachHangDao = new KhachHangDaoImpl();
    private final KhuyenMaiDao khuyenMaiDao = new KhuyenMaiDaoImpl();

    @Override
    public HoaDonDto findById(String maHoaDon) {
        return Mapper.map(dao.findById(maHoaDon));
    }

    @Override
    public List<HoaDonDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<HoaDonDto> search(String keyword) {
        String text = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return loadAll().stream()
                .filter(item -> text.isEmpty()
                        || contains(item.getMaHoaDon(), text)
                        || contains(item.getMaNhanVien(), text)
                        || contains(item.getTenNhanVien(), text)
                        || contains(item.getMaKhachHang(), text)
                        || contains(item.getTenKhachHang(), text)
                        || contains(item.getMaKhuyenMai(), text)
                        || contains(item.getTenKhuyenMai(), text))
                .toList();
    }

    @Override
    public HoaDonDto save(HoaDonDto dto) {
        return Mapper.map(dao.create(buildEntity(dto)));
    }

    @Override
    public HoaDonDto update(HoaDonDto dto) {
        return Mapper.map(dao.update(buildEntity(dto)));
    }

    @Override
    public boolean delete(String maHoaDon) {
        return dao.delete(maHoaDon);
    }

    @Override
    public String nextId() {
        return dao.loadAll().stream()
            .map(HoaDon::getMaHoaDon)
                .filter(id -> id != null && id.startsWith("HD"))
                .map(id -> id.substring(2))
                .filter(number -> number.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .stream()
                .mapToObj(number -> String.format("HD%03d", number + 1))
                .findFirst()
                .orElse("HD001");
    }

    private HoaDon buildEntity(HoaDonDto dto) {
        return HoaDon.builder()
                .maHoaDon(dto.getMaHoaDon())
                .ngayLap(dto.getNgayLap())
                .nhanVien(dto.getMaNhanVien() != null ? nhanVienDao.findById(dto.getMaNhanVien()) : null)
                .khachHang(dto.getMaKhachHang() != null ? khachHangDao.findById(dto.getMaKhachHang()) : null)
                .khuyenMai(dto.getMaKhuyenMai() != null ? khuyenMaiDao.findById(dto.getMaKhuyenMai()) : null)
                .tongTien(dto.getTongTien())
                .build();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}