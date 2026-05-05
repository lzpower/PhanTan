package dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class HoaDonDto implements Serializable {

    private String maHoaDon;
    private LocalDate ngayLap;
    private String maNhanVien;
    private String tenNhanVien;
    private String maKhachHang;
    private String tenKhachHang;
    private String maKhuyenMai;
    private String tenKhuyenMai;
    private double tongTien;
}