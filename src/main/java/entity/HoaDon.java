package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"nhanVien", "khachHang", "khuyenMai", "danhSachChiTiet"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "HoaDon")
public class HoaDon {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "maHoaDon")
    private String maHoaDon;
    
    @Column(nullable = false)
    private LocalDate ngayLap;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maNhanVien", nullable = false)
    @JsonIgnore
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKhachHang")
    @JsonIgnore
    private KhachHang khachHang;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maKhuyenMai")
    @JsonIgnore
    private KhuyenMai khuyenMai;
    
    @Column(nullable = false)
    private double tongTien;
    
    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ChiTietHoaDon> danhSachChiTiet;
    
    public void themChiTiet(ChiTietHoaDon chiTiet) {
        if (this.danhSachChiTiet == null) {
            this.danhSachChiTiet = new ArrayList<>();
        }
        this.danhSachChiTiet.add(chiTiet);
    }

    public double getTongTien() {
        if (danhSachChiTiet != null && !danhSachChiTiet.isEmpty()) {
            double tong = 0;
            for (ChiTietHoaDon ct : danhSachChiTiet) {
                tong += ct.getThanhTien();
            }
            return tong;
        }
        return tongTien;
    }

    public void capNhatTongTien() {
        double tong = 0;
        if (danhSachChiTiet != null) {
            for (ChiTietHoaDon ct : danhSachChiTiet) {
                tong += ct.getThanhTien();
            }
        }
        this.tongTien = tong;
    }
    
    public double getTongTienSauKhuyenMai() {
        double tongTien = getTongTien();
        if (khuyenMai != null) {
            return tongTien * (1 - khuyenMai.getGiaTriKhuyenMai() / 100);
        }
        return tongTien;
    }
}