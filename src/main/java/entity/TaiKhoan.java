package entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "nhanVien")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "TaiKhoan")
public class TaiKhoan {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "tenDangNhap")
    private String tenDangNhap;
    
    @Column(nullable = false)
    private String matKhau;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maNhanVien", unique = true)
    @JsonIgnore
    private NhanVien nhanVien;
}