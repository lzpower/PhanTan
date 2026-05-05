package dto;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SanPhamDto implements Serializable {

    private String maSanPham;
    private String tenSanPham;
    private String maLoaiSanPham;
    private String tenLoaiSanPham;
    private int soLuongHienCo;
    private double giaNhap;
    private double giaBan;
    private String urlHinhAnh;
}