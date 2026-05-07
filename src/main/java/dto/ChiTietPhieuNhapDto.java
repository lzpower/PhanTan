package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ChiTietPhieuNhapDto implements Serializable {

    private String maPhieuNhap;
    private String maSanPham;
    private String tenSanPham;
    private int soLuong;
    private double giaNhap;
    private double thanhTien;
}