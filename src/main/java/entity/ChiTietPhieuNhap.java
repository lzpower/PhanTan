package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"phieuNhap", "sanPham"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "ChiTietPhieuNhap")
public class ChiTietPhieuNhap {

    @EmbeddedId
    private ChiTietPhieuNhapId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maPhieuNhap")
    @JoinColumn(name = "maPhieuNhap")
    @JsonIgnore
    private PhieuNhap phieuNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maSanPham")
    @JoinColumn(name = "maSanPham")
    @JsonIgnore
    private SanPham sanPham;

    @Column(nullable = false)
    private int soLuong;

    @Column(nullable = false)
    private double giaNhap;

    public double getThanhTien() {
        return soLuong * giaNhap;
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class ChiTietPhieuNhapId implements Serializable {
        @Column(name = "maPhieuNhap")
        private String maPhieuNhap;

        @Column(name = "maSanPham")
        private String maSanPham;
    }
}