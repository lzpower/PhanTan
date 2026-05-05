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
public class KhuyenMaiDto implements Serializable {

    private String maKhuyenMai;
    private String tenKhuyenMai;
    private double giaTriKhuyenMai;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;


}