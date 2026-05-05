package entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString()
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "KhuyenMai")
public class KhuyenMai {

    @Id
    @EqualsAndHashCode.Include
    private String maKhuyenMai;

    @Column(nullable = false)
    private String tenKhuyenMai;

    @Column(nullable = false)
    private double giaTriKhuyenMai;

    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;

}
