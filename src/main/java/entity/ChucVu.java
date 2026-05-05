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
@Table(name = "ChucVu")
public class ChucVu {
	@Id
	@EqualsAndHashCode.Include
	private String maChucVu;
	
	@Column(nullable = false)
	private String tenChucVu;
}
