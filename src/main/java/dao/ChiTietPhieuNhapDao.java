package dao;

import entity.ChiTietPhieuNhap;

import java.util.List;

public interface ChiTietPhieuNhapDao extends GenericDao<ChiTietPhieuNhap, ChiTietPhieuNhap.ChiTietPhieuNhapId> {
    List<ChiTietPhieuNhap> findByPhieuNhap(String maPhieuNhap);
}