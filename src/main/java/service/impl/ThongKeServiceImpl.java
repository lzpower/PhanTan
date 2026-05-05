package service.impl;

import db.JPAUtil;
import dto.DashboardStatsDto;
import jakarta.persistence.EntityManager;
import service.ThongKeService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThongKeServiceImpl implements ThongKeService {

    @Override
    public DashboardStatsDto thongKeDoanhThu(int year, Integer compareYear) {
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setYear(year);
        dto.setMonthlyRevenue(loadMonthlyRevenue(year));
        if (compareYear != null) {
            dto.setCompareMonthlyRevenue(loadMonthlyRevenue(compareYear));
        }
        return dto;
    }

    @Override
    public Map<String, Double> topKhachHang(int year, int limit) {
        String sql = "SELECT kh.tenKhachHang, COALESCE(SUM(hd.tongTien),0) AS tong " +
                "FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.maKhachHang = kh.maKhachHang " +
                "WHERE YEAR(hd.ngayLap) = ? GROUP BY kh.tenKhachHang ORDER BY tong DESC LIMIT ?";
        return loadRanking(sql, year, limit);
    }

    @Override
    public Map<String, Double> topSanPham(int year, int limit) {
        String sql = "SELECT sp.tenSanPham, COALESCE(SUM(ct.soLuong * ct.donGia),0) AS tong " +
                "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                "WHERE YEAR(hd.ngayLap) = ? GROUP BY sp.tenSanPham ORDER BY tong DESC LIMIT ?";
        return loadRanking(sql, year, limit);
    }

    private Map<Integer, Double> loadMonthlyRevenue(int year) {
        String sql = "SELECT MONTH(ngayLap) AS thang, COALESCE(SUM(tongTien),0) AS tong " +
                "FROM HoaDon WHERE YEAR(ngayLap) = ? GROUP BY MONTH(ngayLap) ORDER BY MONTH(ngayLap)";
        Map<Integer, Double> result = new LinkedHashMap<>();
        try (EntityManager entityManager = JPAUtil.getEntityManager()) {
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter(1, year)
                    .getResultList();
            for (Object[] row : rows) {
                int month = ((Number) row[0]).intValue();
                double total = toDouble(row[1]);
                result.put(month, total);
            }
        }
        return result;
    }

    private Map<String, Double> loadRanking(String sql, int year, int limit) {
        Map<String, Double> result = new LinkedHashMap<>();
        try (EntityManager entityManager = JPAUtil.getEntityManager()) {
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter(1, year)
                    .setParameter(2, limit)
                    .getResultList();
            for (Object[] row : rows) {
                String label = row[0] != null ? String.valueOf(row[0]) : "Khác";
                result.put(label, toDouble(row[1]));
            }
        }
        return result;
    }

    private double toDouble(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.doubleValue();
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0D;
    }
}