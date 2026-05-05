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
@Table(name = "LoaiSanPham")
public class LoaiSanPham {
	@Id
	@EqualsAndHashCode.Include
	@Column(name = "maLoaiSanPham")
    private String maLoaiSanPham;
    
	@Column(nullable = false)
    private String tenLoaiSanPham;
}
