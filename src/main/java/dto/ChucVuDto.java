package dto;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChucVuDto implements Serializable {

    private String maChucVu;
    private String tenChucVu;

    @Override
    public String toString() {
        return tenChucVu != null ? tenChucVu : maChucVu;
    }
}