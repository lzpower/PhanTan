package dao.impl;

import dao.PhieuNhapDao;
import entity.PhieuNhap;

import java.util.List;

public class PhieuNhapDaoImpl extends AbstractGenericDaoImpl<PhieuNhap, String> implements PhieuNhapDao {

    public PhieuNhapDaoImpl() {
        super(PhieuNhap.class);
    }

    @Override
    public PhieuNhap findById(String id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT pn FROM PhieuNhap pn LEFT JOIN FETCH pn.nhaCungCap LEFT JOIN FETCH pn.nhanVien LEFT JOIN FETCH pn.danhSachChiTiet ct LEFT JOIN FETCH ct.sanPham WHERE pn.maPhieuNhap = :id",
                        PhieuNhap.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<PhieuNhap> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT pn FROM PhieuNhap pn LEFT JOIN FETCH pn.nhaCungCap LEFT JOIN FETCH pn.nhanVien LEFT JOIN FETCH pn.danhSachChiTiet ct LEFT JOIN FETCH ct.sanPham ORDER BY pn.ngayNhap DESC",
                        PhieuNhap.class)
                .getResultList());
    }
}