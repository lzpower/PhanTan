package dao;

import entity.ChiTietHoaDon;

import java.util.List;

public interface ChiTietHoaDonDao extends GenericDao<ChiTietHoaDon, ChiTietHoaDon.ChiTietHoaDonId> {
    List<ChiTietHoaDon> findByHoaDon(String maHoaDon);
}