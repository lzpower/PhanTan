package dao.impl;

import dao.KhachHangDao;
import entity.KhachHang;

public class KhachHangDaoImpl extends AbstractGenericDaoImpl<KhachHang, String> implements KhachHangDao {

    public KhachHangDaoImpl() {
        super(KhachHang.class);
    }
}
