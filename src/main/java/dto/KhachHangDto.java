package dto;

import lombok.*;

import java.io.Serializable;

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