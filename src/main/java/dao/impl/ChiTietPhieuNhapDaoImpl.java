package dao.impl;

import dao.ChiTietPhieuNhapDao;
import entity.ChiTietPhieuNhap;

import java.util.List;

public class ChiTietPhieuNhapDaoImpl extends AbstractGenericDaoImpl<ChiTietPhieuNhap, ChiTietPhieuNhap.ChiTietPhieuNhapId> implements ChiTietPhieuNhapDao {

    public ChiTietPhieuNhapDaoImpl() {
        super(ChiTietPhieuNhap.class);
    }

    @Override
    public ChiTietPhieuNhap findById(ChiTietPhieuNhap.ChiTietPhieuNhapId id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT ct FROM ChiTietPhieuNhap ct LEFT JOIN FETCH ct.phieuNhap LEFT JOIN FETCH ct.sanPham WHERE ct.id = :id",
                        ChiTietPhieuNhap.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<ChiTietPhieuNhap> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT ct FROM ChiTietPhieuNhap ct LEFT JOIN FETCH ct.phieuNhap LEFT JOIN FETCH ct.sanPham",
                        ChiTietPhieuNhap.class)
                .getResultList());
    }

    @Override
    public List<ChiTietPhieuNhap> findByPhieuNhap(String maPhieuNhap) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT ct FROM ChiTietPhieuNhap ct LEFT JOIN FETCH ct.phieuNhap LEFT JOIN FETCH ct.sanPham WHERE ct.phieuNhap.maPhieuNhap = :maPhieuNhap",
                        ChiTietPhieuNhap.class)
                .setParameter("maPhieuNhap", maPhieuNhap)
                .getResultList());
    }
}