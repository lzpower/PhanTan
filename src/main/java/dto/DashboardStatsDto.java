package dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto implements Serializable {
    private double totalRevenueToday;
    private long totalInvoicesToday;
    private long totalProductsSoldToday;
    private double employeeRevenueToday;
}
