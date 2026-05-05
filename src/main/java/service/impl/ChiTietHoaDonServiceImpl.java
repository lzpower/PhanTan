package service.impl;

import dao.ChiTietHoaDonDao;
import dao.HoaDonDao;
import dao.SanPhamDao;
import dao.impl.ChiTietHoaDonDaoImpl;
import dao.impl.HoaDonDaoImpl;
import dao.impl.SanPhamDaoImpl;
import dto.ChiTietHoaDonDto;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.SanPham;
import mapper.Mapper;
import service.ChiTietHoaDonService;

import java.util.List;

public class ChiTietHoaDonServiceImpl implements ChiTietHoaDonService {

    private final ChiTietHoaDonDao dao = new ChiTietHoaDonDaoImpl();
    private final HoaDonDao hoaDonDao = new HoaDonDaoImpl();
    private final SanPhamDao sanPhamDao = new SanPhamDaoImpl();

    @Override
    public ChiTietHoaDonDto findById(ChiTietHoaDon.ChiTietHoaDonId id) {
        return Mapper.map(dao.findById(id));
    }

    @Override
    public List<ChiTietHoaDonDto> loadAll() {
        return dao.loadAll().stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public List<ChiTietHoaDonDto> loadByHoaDon(String maHoaDon) {
        return dao.findByHoaDon(maHoaDon).stream().map(item -> Mapper.map(item)).toList();
    }

    @Override
    public ChiTietHoaDonDto save(ChiTietHoaDonDto dto) {
        return Mapper.map(dao.create(buildEntity(dto)));
    }

    @Override
    public ChiTietHoaDonDto update(ChiTietHoaDonDto dto) {
        return Mapper.map(dao.update(buildEntity(dto)));
    }

    @Override
    public boolean delete(ChiTietHoaDon.ChiTietHoaDonId id) {
        return dao.delete(id);
    }

    private ChiTietHoaDon buildEntity(ChiTietHoaDonDto dto) {
        HoaDon hoaDon = hoaDonDao.findById(dto.getMaHoaDon());
        SanPham sanPham = sanPhamDao.findById(dto.getMaSanPham());
        double donGia = dto.getDonGia() > 0 ? dto.getDonGia() : (sanPham != null ? sanPham.getGiaBan() : 0D);
        return ChiTietHoaDon.builder()
                .id(new ChiTietHoaDon.ChiTietHoaDonId(dto.getMaHoaDon(), dto.getMaSanPham()))
                .hoaDon(hoaDon)
                .sanPham(sanPham)
                .soLuong(dto.getSoLuong())
                .donGia(donGia)
                .build();
    }
}