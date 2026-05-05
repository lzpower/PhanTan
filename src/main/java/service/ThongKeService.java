package service;

import dto.DashboardStatsDto;

import java.util.Map;

public interface ThongKeService {
    DashboardStatsDto thongKeDoanhThu(int year, Integer compareYear);
    Map<String, Double> topKhachHang(int year, int limit);
    Map<String, Double> topSanPham(int year, int limit);
}