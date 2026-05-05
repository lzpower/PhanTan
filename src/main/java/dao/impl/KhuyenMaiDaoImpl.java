package dao.impl;

import dao.KhuyenMaiDao;
import entity.KhuyenMai;

public class KhuyenMaiDaoImpl extends AbstractGenericDaoImpl<KhuyenMai, String> implements KhuyenMaiDao {

    public KhuyenMaiDaoImpl() {
        super(KhuyenMai.class);
    }
}
