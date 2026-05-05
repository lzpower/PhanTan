package dao.impl;

import dao.ChiTietHoaDonDao;
import entity.ChiTietHoaDon;

import java.util.List;

public class ChiTietHoaDonDaoImpl extends AbstractGenericDaoImpl<ChiTietHoaDon, ChiTietHoaDon.ChiTietHoaDonId> implements ChiTietHoaDonDao {

    public ChiTietHoaDonDaoImpl() {
        super(ChiTietHoaDon.class);
    }

    @Override
    public ChiTietHoaDon findById(ChiTietHoaDon.ChiTietHoaDonId id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT ct FROM ChiTietHoaDon ct LEFT JOIN FETCH ct.hoaDon LEFT JOIN FETCH ct.sanPham LEFT JOIN FETCH ct.sanPham.loaiSanPham WHERE ct.id = :id",
                        ChiTietHoaDon.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<ChiTietHoaDon> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT ct FROM ChiTietHoaDon ct LEFT JOIN FETCH ct.hoaDon LEFT JOIN FETCH ct.sanPham LEFT JOIN FETCH ct.sanPham.loaiSanPham",
                        ChiTietHoaDon.class)
                .getResultList());
    }

    @Override
    public List<ChiTietHoaDon> findByHoaDon(String maHoaDon) {
        String query = "SELECT ct FROM ChiTietHoaDon ct LEFT JOIN FETCH ct.hoaDon LEFT JOIN FETCH ct.sanPham LEFT JOIN FETCH ct.sanPham.loaiSanPham WHERE ct.hoaDon.maHoaDon = :maHoaDon";
        return doInTransaction(em -> em.createQuery(query, ChiTietHoaDon.class)
                .setParameter("maHoaDon", maHoaDon)
                .getResultList());
    }
}