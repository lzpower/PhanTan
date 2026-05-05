package dao.impl;

import dao.NhanVienDAO;
import entity.NhanVien;

import java.util.List;

public class NhanVienDaoImpl extends AbstractGenericDaoImpl<NhanVien, String> implements NhanVienDAO {

    public NhanVienDaoImpl() {
        super(NhanVien.class);
    }

    @Override
    public NhanVien findById(String id) {
        return doInTransaction(em -> em.createQuery(
                        "SELECT nv FROM NhanVien nv LEFT JOIN FETCH nv.chucVu LEFT JOIN FETCH nv.taiKhoan WHERE nv.maNhanVien = :id",
                        NhanVien.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public List<NhanVien> loadAll() {
        return doInTransaction(em -> em.createQuery(
                        "SELECT DISTINCT nv FROM NhanVien nv LEFT JOIN FETCH nv.chucVu LEFT JOIN FETCH nv.taiKhoan",
                        NhanVien.class)
                .getResultList());
    }
}
