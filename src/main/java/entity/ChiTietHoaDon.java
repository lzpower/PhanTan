package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"hoaDon", "sanPham"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "ChiTietHoaDon")
public class ChiTietHoaDon {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private ChiTietHoaDonId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maHoaDon")
    @JoinColumn(name = "maHoaDon")
    @JsonIgnore
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSanPham")
    @JoinColumn(name = "maSanPham")
    @JsonIgnore
    private SanPham sanPham;

    private int soLuong;
    private double donGia;

    public double getThanhTien() {
        return soLuong * donGia;
    }


    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Builder

    @Embeddable
    public static class ChiTietHoaDonId implements Serializable {
        private String maHoaDon;
        private String maSanPham;
    }
}