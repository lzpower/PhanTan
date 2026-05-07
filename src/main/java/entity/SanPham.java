package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "loaiSanPham")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "SanPham")
public class SanPham {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "maSanPham")
    private String maSanPham;

    @Column(nullable = false)
    private String tenSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maLoaiSanPham", nullable = false)
    @JsonIgnore
    private LoaiSanPham loaiSanPham;

    @Column(nullable = false)
    private int soLuongHienCo;

    @Column(nullable = false)
    private double giaBan;

    @Column(name = "urlHinhAnh")
    private String urlHinhAnh;
}
