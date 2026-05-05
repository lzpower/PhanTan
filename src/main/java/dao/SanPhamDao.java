package dao;

import entity.SanPham;

import java.util.List;

public interface SanPhamDao extends GenericDao<SanPham, String> {
    List<SanPham> findByLoaiSanPham(String maLoaiSanPham);
}