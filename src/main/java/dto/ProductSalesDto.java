package dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSalesDto implements Serializable {
    private String maSanPham;
    private String tenSanPham;
    private long soLuongBan;
    private double doanhThu;
}
