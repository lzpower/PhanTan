package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder

@Entity
@Table(name = "NhaCungCap")
public class NhaCungCap {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "maNhaCungCap")
    private String maNhaCungCap;

    @Column(nullable = false)
    private String tenNhaCungCap;

    @Column(nullable = false)
    private String diaChi;

    @Column(nullable = false, length = 20)
    private String soDienThoai;

    @Column(nullable = false)
    private String email;
}