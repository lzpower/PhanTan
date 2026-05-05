package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "KhachHang")
public class KhachHang {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "maKhachHang")
    private String maKhachHang;
    
    @Column(nullable = false)
    private String tenKhachHang;
    
    @Column(nullable = false)
    private String soDienThoai;
    
    @Column(nullable = false)
    private int soDiem;
}