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
public class KhachHangDto implements Serializable {

    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private int soDiem;

}