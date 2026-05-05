package dao.impl;

import dao.LoaiSanPhamDao;
import entity.LoaiSanPham;

public class LoaiSanPhamDaoImpl extends AbstractGenericDaoImpl<LoaiSanPham, String> implements LoaiSanPhamDao {

    public LoaiSanPhamDaoImpl() {
        super(LoaiSanPham.class);
    }
}