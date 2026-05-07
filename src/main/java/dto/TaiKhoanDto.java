package dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TaiKhoanDto implements Serializable {

    private String tenDangNhap;
    private String matKhau;
    private String maChucVu;
    private String tenChucVu;
    private String maNhanVien;
    private String tenNhanVien;
    private String email;
}