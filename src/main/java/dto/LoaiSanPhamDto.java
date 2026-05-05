package dto;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiSanPhamDto implements Serializable {

    private String maLoaiSanPham;
    private String tenLoaiSanPham;

    @Override
    public String toString() {
        return tenLoaiSanPham != null ? tenLoaiSanPham : maLoaiSanPham;
    }
}