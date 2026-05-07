package service.impl;

import dao.ChiTietPhieuNhapDao;
import dao.impl.ChiTietPhieuNhapDaoImpl;
import dto.ChiTietPhieuNhapDto;
import mapper.Mapper;
import service.ChiTietPhieuNhapService;

import java.util.List;

public class ChiTietPhieuNhapServiceImpl implements ChiTietPhieuNhapService {

    private final ChiTietPhieuNhapDao dao = new ChiTietPhieuNhapDaoImpl();

    @Override
    public List<ChiTietPhieuNhapDto> loadByPhieuNhap(String maPhieuNhap) {
        return dao.findByPhieuNhap(maPhieuNhap).stream().map(Mapper::map).toList();
    }
}