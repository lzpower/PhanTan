package dao.impl;

import dao.SanPhamDao;
import entity.SanPham;

import java.util.List;

public class SanPhamDaoImpl extends AbstractGenericDaoImpl<SanPham, String> implements SanPhamDao {

    public SanPhamDaoImpl() {
        super(SanPham.class);
    }

    @Override
    public SanPham findById(String id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.loaiSanPham WHERE sp.maSanPham = :id",
                        SanPham.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<SanPham> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT sp FROM SanPham sp LEFT JOIN FETCH sp.loaiSanPham",
                        SanPham.class)
                .getResultList());
    }

    @Override
    public List<SanPham> findByLoaiSanPham(String maLoaiSanPham) {
        String query = "SELECT sp FROM SanPham sp LEFT JOIN FETCH sp.loaiSanPham WHERE sp.loaiSanPham.maLoaiSanPham = :maLoaiSanPham";
        return doInTransaction(em -> em.createQuery(query, SanPham.class)
                .setParameter("maLoaiSanPham", maLoaiSanPham)
                .getResultList());
    }
}