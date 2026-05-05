package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DashboardStatsDto implements Serializable {

    private int year;
    private Map<Integer, Double> monthlyRevenue = new LinkedHashMap<>();
    private Map<Integer, Double> compareMonthlyRevenue = new LinkedHashMap<>();
}