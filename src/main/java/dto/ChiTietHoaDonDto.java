package dto;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ChiTietHoaDonDto implements Serializable {

    private String maHoaDon;
    private String maSanPham;
    private String tenSanPham;
    private int soLuong;
    private double donGia;
    private double thanhTien;
}