package dao.impl;

import dao.ChucVuDao;
import entity.ChucVu;

public class ChucVuDaoImpl extends AbstractGenericDaoImpl<ChucVu, String> implements ChucVuDao {

    public ChucVuDaoImpl() {
        super(ChucVu.class);
    }
}
