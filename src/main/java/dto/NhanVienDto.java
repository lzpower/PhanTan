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
public class NhanVienDto implements Serializable {
    private String maNhanVien;
    private String tenNhanVien;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String email;
    private String maChucVu;
    private String tenChucVu;
    private String soDienThoai;
    private String tenTaiKhoan;
}