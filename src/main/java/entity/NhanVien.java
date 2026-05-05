package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chucVu", "taiKhoan"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "NhanVien")
public class NhanVien {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "maNhanVien")
    private String maNhanVien;
    
    @Column(nullable = false)
    private String tenNhanVien;
    
    private LocalDate ngaySinh;
    
    @Column(nullable = false, length = 10)
    private String gioiTinh;
    
    @Column(nullable = false, length = 150)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maChucVu", nullable = false)
    @JsonIgnore
    private ChucVu chucVu;
    
    @Column(nullable = false, length = 10)
    private String soDienThoai;

    @OneToOne(mappedBy = "nhanVien", fetch = FetchType.LAZY)
    @JsonIgnore
    private TaiKhoan taiKhoan;

}
