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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"nhaCungCap", "nhanVien", "danhSachChiTiet"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "PhieuNhap")
public class PhieuNhap {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "maPhieuNhap")
    private String maPhieuNhap;

    @Column(nullable = false)
    private LocalDateTime ngayNhap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maNhaCungCap", nullable = false)
    @JsonIgnore
    private NhaCungCap nhaCungCap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maNhanVien")
    @JsonIgnore
    private NhanVien nhanVien;

    @Column(length = 500)
    private String ghiChu;

    @Column(nullable = false)
    private double tongTien;

    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ChiTietPhieuNhap> danhSachChiTiet = new ArrayList<>();

    public void themChiTiet(ChiTietPhieuNhap chiTiet) {
        if (chiTiet == null) {
            return;
        }
        if (danhSachChiTiet == null) {
            danhSachChiTiet = new ArrayList<>();
        }
        danhSachChiTiet.add(chiTiet);
    }

    public void capNhatTongTien() {
        double tong = 0D;
        if (danhSachChiTiet != null) {
            for (ChiTietPhieuNhap chiTiet : danhSachChiTiet) {
                tong += chiTiet.getThanhTien();
            }
        }
        tongTien = tong;
    }
}