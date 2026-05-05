package dao.impl;

import dao.TaiKhoanDao;
import entity.NhanVien;
import entity.TaiKhoan;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TaiKhoanDaoImpl extends AbstractGenericDaoImpl<TaiKhoan, String> implements TaiKhoanDao {

    public TaiKhoanDaoImpl() {
        super(TaiKhoan.class);
    }

    @Override
    public TaiKhoan findById(String id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT t FROM TaiKhoan t LEFT JOIN FETCH t.nhanVien WHERE t.tenDangNhap = :id",
                        TaiKhoan.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<TaiKhoan> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT t FROM TaiKhoan t LEFT JOIN FETCH t.nhanVien",
                        TaiKhoan.class)
                .getResultList());
    }

    @Override
    public TaiKhoan create(TaiKhoan taiKhoan) {
        return doInTransaction(em -> {
            if (taiKhoan.getNhanVien() != null && taiKhoan.getNhanVien().getMaNhanVien() != null) {
                NhanVien managedNhanVien = em.getReference(NhanVien.class, taiKhoan.getNhanVien().getMaNhanVien());
                taiKhoan.setNhanVien(managedNhanVien);
            }
            em.persist(taiKhoan);
            return taiKhoan;
        });
    }
}
