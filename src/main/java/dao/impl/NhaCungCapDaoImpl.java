package dao.impl;

import dao.NhaCungCapDao;
import entity.NhaCungCap;

public class NhaCungCapDaoImpl extends AbstractGenericDaoImpl<NhaCungCap, String> implements NhaCungCapDao {

    public NhaCungCapDaoImpl() {
        super(NhaCungCap.class);
    }
}