package service.impl;

import db.JPAUtil;
import dto.DashboardStatsDto;
import dto.ProductSalesDto;
import dto.SanPhamDto;
import jakarta.persistence.EntityManager;
import service.ThongKeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThongKeServiceImpl implements ThongKeService {

    @Override
    public DashboardStatsDto getStats(LocalDate start, LocalDate end, String employeeId) {
        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(tongTien), 0) FROM HoaDon WHERE ngayLap BETWEEN :start AND :end) as revenue, " +
                "(SELECT COUNT(*) FROM HoaDon WHERE ngayLap BETWEEN :start AND :end) as invoices, " +
                "(SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon WHERE hd.ngayLap BETWEEN :start AND :end) as products";
        
        String employeeRevSql = "SELECT COALESCE(SUM(tongTien), 0) FROM HoaDon WHERE (ngayLap BETWEEN :start AND :end)";
        if (employeeId != null && !employeeId.isBlank()) {
            employeeRevSql += " AND maNhanVien = :employeeId";
        } else {
            // If no employeeId provided, just set to 0 or same as total revenue
            employeeRevSql = "SELECT 0";
        }

        try (EntityManager em = JPAUtil.getEntityManager()) {
            Object[] result = (Object[]) em.createNativeQuery(sql)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            
            var query = em.createNativeQuery(employeeRevSql)
                    .setParameter("start", start)
                    .setParameter("end", end);
            if (employeeId != null && !employeeId.isBlank()) {
                query.setParameter("employeeId", employeeId);
            }
            Object empResult = query.getSingleResult();
            
            return DashboardStatsDto.builder()
                    .totalRevenueToday(toDouble(result[0]))
                    .totalInvoicesToday(((Number) result[1]).longValue())
                    .totalProductsSoldToday(((Number) result[2]).longValue())
                    .employeeRevenueToday(toDouble(empResult))
                    .build();
        }
    }

    @Override
    public Map<LocalDate, Double> getRevenueByDateRange(LocalDate start, LocalDate end) {
        String sql = "SELECT ngayLap, SUM(tongTien) FROM HoaDon " +
                "WHERE ngayLap BETWEEN :start AND :end " +
                "GROUP BY ngayLap ORDER BY ngayLap";
        
        Map<LocalDate, Double> result = new LinkedHashMap<>();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(sql)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
            for (Object[] row : rows) {
                LocalDate date = null;
                if (row[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) row[0]).toLocalDate();
                } else if (row[0] instanceof LocalDate) {
                    date = (LocalDate) row[0];
                } else if (row[0] != null) {
                    date = LocalDate.parse(row[0].toString());
                }
                if (date != null) {
                    result.put(date, toDouble(row[1]));
                }
            }
        }
        return result;
    }

    @Override
    public List<ProductSalesDto> getTopProducts(LocalDate start, LocalDate end, int limit) {
        String sql = "SELECT sp.maSanPham, sp.tenSanPham, SUM(ct.soLuong) as soLuong, SUM(ct.soLuong * ct.donGia) as doanhThu " +
                "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon " +
                "JOIN SanPham sp ON ct.maSanPham = sp.maSanPham " +
                "WHERE hd.ngayLap BETWEEN :start AND :end " +
                "GROUP BY sp.maSanPham, sp.tenSanPham " +
                "ORDER BY soLuong DESC LIMIT :limit";
        
        List<ProductSalesDto> result = new ArrayList<>();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(sql)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .setParameter("limit", limit)
                    .getResultList();
            for (Object[] row : rows) {
                result.add(ProductSalesDto.builder()
                        .maSanPham((String) row[0])
                        .tenSanPham((String) row[1])
                        .soLuongBan(((Number) row[2]).longValue())
                        .doanhThu(toDouble(row[3]))
                        .build());
            }
        }
        return result;
    }

    @Override
    public List<SanPhamDto> getLowStockProducts(int threshold) {
        String sql = "SELECT sp.maSanPham, sp.tenSanPham, sp.soLuongHienCo, sp.giaBan, lsp.tenLoaiSanPham " +
                "FROM SanPham sp JOIN LoaiSanPham lsp ON sp.maLoaiSanPham = lsp.maLoaiSanPham " +
                "WHERE sp.soLuongHienCo < :threshold " +
                "ORDER BY sp.soLuongHienCo ASC";
        
        List<SanPhamDto> result = new ArrayList<>();
        try (EntityManager em = JPAUtil.getEntityManager()) {
            List<Object[]> rows = em.createNativeQuery(sql)
                    .setParameter("threshold", threshold)
                    .getResultList();
            for (Object[] row : rows) {
                result.add(SanPhamDto.builder()
                        .maSanPham((String) row[0])
                        .tenSanPham((String) row[1])
                        .soLuongHienCo(((Number) row[2]).intValue())
                        .giaBan(toDouble(row[3]))
                        .tenLoaiSanPham((String) row[4])
                        .build());
            }
        }
        return result;
    }

    private double toDouble(Object value) {
        if (value instanceof BigDecimal bd) return bd.doubleValue();
        if (value instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}
