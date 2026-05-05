package dao.impl;

import dao.HoaDonDao;
import entity.HoaDon;

import java.util.List;

public class HoaDonDaoImpl extends AbstractGenericDaoImpl<HoaDon, String> implements HoaDonDao {

    public HoaDonDaoImpl() {
        super(HoaDon.class);
    }

    @Override
    public HoaDon findById(String id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT hd FROM HoaDon hd LEFT JOIN FETCH hd.nhanVien LEFT JOIN FETCH hd.khachHang LEFT JOIN FETCH hd.khuyenMai WHERE hd.maHoaDon = :id",
                        HoaDon.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<HoaDon> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT hd FROM HoaDon hd LEFT JOIN FETCH hd.nhanVien LEFT JOIN FETCH hd.khachHang LEFT JOIN FETCH hd.khuyenMai",
                        HoaDon.class)
                .getResultList());
    }
}