package service;

import dto.DashboardStatsDto;
import dto.ProductSalesDto;
import dto.SanPhamDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ThongKeService {
    DashboardStatsDto getStats(LocalDate start, LocalDate end, String employeeId);
    Map<LocalDate, Double> getRevenueByDateRange(LocalDate start, LocalDate end);
    List<ProductSalesDto> getTopProducts(LocalDate start, LocalDate end, int limit);
    List<SanPhamDto> getLowStockProducts(int threshold);
}
