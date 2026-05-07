CREATE DATABASE IF NOT EXISTS CuaHangTienLoi
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE CuaHangTienLoi;

CREATE TABLE IF NOT EXISTS LoaiSanPham (
    maLoaiSanPham VARCHAR(20) PRIMARY KEY,
    tenLoaiSanPham VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS SanPham (
    maSanPham VARCHAR(20) PRIMARY KEY,
    tenSanPham VARCHAR(100) NOT NULL,
    maLoaiSanPham VARCHAR(20) NOT NULL,
    soLuongHienCo INT NOT NULL,
    giaNhap DECIMAL(18,2) NOT NULL,
    giaBan DECIMAL(18,2) NOT NULL,
    urlHinhAnh VARCHAR(255),
    FOREIGN KEY (maLoaiSanPham) REFERENCES LoaiSanPham(maLoaiSanPham)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ChucVu (
    maChucVu VARCHAR(20) PRIMARY KEY,
    tenChucVu VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS NhanVien (
    maNhanVien VARCHAR(20) PRIMARY KEY,
    tenNhanVien VARCHAR(100) NOT NULL,
    ngaySinh DATE NULL,
    gioiTinh VARCHAR(10) NOT NULL,
    email VARCHAR(150) NOT NULL,
    maChucVu VARCHAR(20) NOT NULL,
    soDienThoai VARCHAR(10) NOT NULL,
    FOREIGN KEY (maChucVu) REFERENCES ChucVu(maChucVu),
    CONSTRAINT chk_nhanvien_gioitinh CHECK (gioiTinh IN ('Nam', 'Nữ'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS TaiKhoan (
    tenDangNhap VARCHAR(50) PRIMARY KEY,
    matKhau VARCHAR(100) NOT NULL,
    vaiTro VARCHAR(50) NOT NULL,
    maNhanVien VARCHAR(20) UNIQUE,
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    tenKhachHang VARCHAR(100) NOT NULL,
    soDienThoai VARCHAR(10) NOT NULL,
    soDiem INT DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS KhuyenMai (
    maKhuyenMai VARCHAR(20) PRIMARY KEY,
    tenKhuyenMai VARCHAR(100) NOT NULL,
    giaTriKhuyenMai DECIMAL(5,2) NOT NULL CHECK (giaTriKhuyenMai > 0 AND giaTriKhuyenMai <= 100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    ngayLap DATE NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    maKhachHang VARCHAR(20),
    maKhuyenMai VARCHAR(20),
    phuongThucThanhToan ENUM('TIENMAT', 'CHUYENKHOAN') DEFAULT 'TIENMAT',
    tongTien DECIMAL(18,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    FOREIGN KEY (maKhuyenMai) REFERENCES KhuyenMai(maKhuyenMai)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ChiTietHoaDon (
    maHoaDon VARCHAR(20),
    maSanPham VARCHAR(20),
    soLuong INT NOT NULL,
    donGia DECIMAL(18,2) NOT NULL,
    PRIMARY KEY (maHoaDon, maSanPham),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS NhaCungCap (
    maNhaCungCap VARCHAR(20) PRIMARY KEY,
    tenNhaCungCap VARCHAR(150) NOT NULL,
    diaChi VARCHAR(255) NOT NULL,
    soDienThoai VARCHAR(20) NOT NULL,
    email VARCHAR(150) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS PhieuNhap (
    maPhieuNhap VARCHAR(20) PRIMARY KEY,
    ngayNhap DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NULL,
    ghiChu VARCHAR(500) NULL,
    tongTien DECIMAL(18,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ChiTietPhieuNhap (
    maPhieuNhap VARCHAR(20),
    maSanPham VARCHAR(20),
    soLuong INT NOT NULL,
    giaNhap DECIMAL(18,2) NOT NULL,
    PRIMARY KEY (maPhieuNhap, maSanPham),
    FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhap(maPhieuNhap),
    FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
) ENGINE=InnoDB;




-- Insert data for LoaiSanPham
INSERT INTO LoaiSanPham (maLoaiSanPham, tenLoaiSanPham) VALUES
('LSP001', N'Hàng tổng hợp'),
('LSP002', N'Hóa mỹ phẩm'),
('LSP003', N'Thực phẩm khô'),
('LSP004', N'Kem'),
('LSP005', N'Sữa & Sản phẩm từ sữa'),
('LSP006', N'Snack'),
('LSP007', N'Bánh kẹo'),
('LSP008', N'Nước giải khát lạnh'),
('LSP009', N'Bia - rượu');

-- Insert data for ChucVu
INSERT INTO ChucVu (maChucVu, tenChucVu) VALUES
('CV001', N'Quản lý'),
('CV002', N'Nhân viên bán hàng');

INSERT INTO NhanVien (maNhanVien, tenNhanVien, ngaySinh, gioiTinh, email, maChucVu, soDienThoai) VALUES
('NV001', 'Nguyễn Văn Tân', '1998-02-11', 'Nam', 'tan.nv001@example.com', 'CV001', '0901234567'),
('NV002', 'Trần Thanh Trường', '1999-05-18', 'Nam', 'truong.tt002@example.com', 'CV002', '0912345678'),
('NV003', 'Hồ Thị Kim Xuyến', '2000-09-27', 'Nữ', 'xuyen.ht003@example.com', 'CV002', '0923456789'),
('NV004', 'Huỳnh Anh Trọng', '1997-12-03', 'Nam', 'trong.ha004@example.com', 'CV002', '0934567890');

-- Insert data for TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, maNhanVien) VALUES
('admin', 'admin', 'NV001'),
('ttt', '123', 'NV002'),
('htkx', '123', 'NV003'),
('hat', '123', 'NV004');

-- Insert data for KhachHang
INSERT INTO KhachHang (maKhachHang, tenKhachHang, soDienThoai, soDiem) VALUES
('KH001', N'Nguyễn Thị X', '0901234567', 100),
('KH002', N'Trần Văn Y', '0912345678', 50),
('KH003', N'Lê Thị Z', '0923456789', 200),
('KH004', N'Phạm Văn T', '0934567890', 0),
('KH005', N'Hoàng Thị U', '0945678901', 150);

-- Insert data for KhuyenMai
INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giaTriKhuyenMai) VALUES
('KM001', N'Giảm giá mùa thi lại', 10),
('KM002', N'Khuyến mãi Tết cho sinh viên rớt môn', 20),
('KM003', N'Black Friday cho sinh viên FA', 15),
('KM004', N'FAN JACK', 25);

-- Insert data for SanPham
INSERT INTO SanPham (maSanPham, tenSanPham, maLoaiSanPham, soLuongHienCo, giaBan, urlHinhAnh) VALUES
('893024581736', N'Vỉ 4 viên pin AA Con Ó', 'LSP001', 100, 22800, 'HangTongHop1.jpg'),
('893147205981', N'Vỉ 2 viên pin tiểu AAA Panasonic', 'LSP001', 100, 16950, 'HangTongHop2.jpg'),
('893659328174', N'Khăn giấy ăn PREMIESR VinaTissue 1 lớp 100 tờ', 'LSP001', 100, 18000, 'HangTongHop3.jpg'),
('893710492385', N'Khăn ướt em bé Bobby không mùi gói 100 miếng', 'LSP001', 100, 63000, 'HangTongHop4.jpg'),
('893843217906', N'Bột giặt OMO Comfort tinh dầu thơm nồng nàn túi 350g', 'LSP001', 100, 33000, 'HangTongHop5.jpg'),
('893932485170', N'Bột giặt nhiệt Aba hương nước hoa túi 350g', 'LSP001', 100, 29250, 'HangTongHop6.jpg'),
('893518204637', N'Khẩu trang kháng khuẩn Famapro 4D 4 lớp gói 10 cái', 'LSP001', 100, 21900, 'HangTongHop7.jpg'),
('893608391524', N'Khẩu trang y tế cao cấp Famapro Extra 4 lớp hộp 50 cái', 'LSP001', 100, 51000, 'HangTongHop8.jpg'),
('893790421683', N'Bông tẩy trang Mihoo dạng tròn 150 miếng', 'LSP002', 100, 47000,  'HoaMyPham1.jpg'),
('893286319740', N'Nước tẩy trang Simple Micellar Water Gently Removes Make Up & Hydrates làm sạch lớp trang điểm 400ml', 'LSP002', 100,  238500, 'HoaMyPham2.jpg'),
('893104592837', N'Kem đánh răng P/S trắng răng than hoạt tính 230g', 'LSP002', 100, 52500, 'HoaMyPham3.jpg'),
('893375801294', N'Kem đánh răng Sensodyne trắng răng tự nhiên 100g', 'LSP002', 100, 111000, 'HoaMyPham4.jpg'),
('893421790865', N'Sữa chống nắng Sunplay Skin Aqua Clear White dưỡng da trắng mịn SPF 50+/PA++++ 25g', 'LSP002', 100,  198000, 'HoaMyPham5.jpg'),
('893916284530', N'Sáp vuốt tóc Romano Clay Wax giữ nếp lâu', 'LSP002', 100, 123000, 'HoaMyPham6.jpg'),
('893635728149', N'Ngũ cốc ăn sáng Nestlé Milo gói 70g', 'LSP003', 100, 46500, 'ThucPhamKho1.jpg'),
('893752490381', N'Gói nước cốt cà phê sữa NesCafé 75ml', 'LSP003', 100, 12000, 'ThucPhamKho2.jpg'),
('893382917045', N'Mì Hảo Hảo vị tôm chua cay gói 75g', 'LSP003', 100, 6600, 'ThucPhamKho3.jpg'),
('893509273186', N'Mì xào khô Indomie Mi Goreng vị đặc biệt gói 85g', 'LSP003', 100, 9000, 'ThucPhamKho4.jpg'),
('893678214953', N'Mì Modern lẩu Thái tôm ly 65g', 'LSP003', 100, 12300, 'ThucPhamKho5.jpg'),
('893801357692', N'Mì trộn Cung Đình Kool vị sườn nướng tô 99g', 'LSP003', 100, 22950, 'ThucPhamKho6.jpg'),
('893215074839', N'Kem que Topten Vanila Wall''s cây 60g', 'LSP004', 100, 15000, 'Kem1.jpg'),
('893947260318', N'Kem bánh mochi dâu Joyday gói 45ml', 'LSP004', 100, 9000, 'Kem2.jpg'),
('893062173489', N'Kem vani socola Merino Super Teen cây 60g', 'LSP004', 100, 13500, 'Kem3.jpg'),
('893439528176', N'Kem trứng muối dừa Hùng Linh Snow baby premium cây 60ml', 'LSP004', 100, 22500, 'Kem4.jpg'),
('893124708935', N'Sữa tươi tiệt trùng ít đường Vinamilk 100% Sữa tươi bịch 220ml', 'LSP005', 100, 12900, 'Sua1.jpg'),
('893673905812', N'Hộp sữa tươi tiệt trùng có đường TH true MILK 180ml', 'LSP005', 100, 13500, 'Sua2.jpg'),
('893298471650', N'Hộp sữa lúa mạch Milo 180ml', 'LSP005', 100, 12000, 'Sua3.jpg'),
('893540296781', N'Hộp sữa chua không đường Vinamilk 100g', 'LSP005', 100, 8700, 'Sua4.jpg'),
('893826107943', N'Chai sữa uống lên men Yakult 65ml', 'LSP005', 100, 7500, 'Sua5.jpg'),
('893015384672', N'Sữa uống lên men hương cam Betagen chai 700ml', 'LSP005', 100, 45000, 'Sua6.jpg'),
('893274509316', N'Snack tôm cay đặc biệt Oishi gói 32g', 'LSP006', 100, 6000, 'Snack1.jpg'),
('893364801295', N'Snack que nhân vị kem Tiramisu Oishi Akiko gói 140g', 'LSP006', 100, 30000, 'Snack2.jpg'),
('893703421586', N'Snack vị bò lúc lắc Poca gói 65g', 'LSP006', 100, 15000, 'Snack3.jpg'),
('893892640731', N'Snack vị kim chi Hàn Quốc O''Star gói 152g', 'LSP006', 100, 31500, 'Snack4.jpg'),
('893678209354', N'Snack khoai tây tôm hùm nướng ngũ vị Lay''s Stax lon 150g', 'LSP006', 100, 60000, 'Snack5.jpg'),
('893197630458', N'Bánh gạo nướng vị tảo biển Orion An gói 111.3g', 'LSP007', 100, 22500, 'BanhKeo1.jpg'),
('893903726184', N'Bánh chocopie Orion hộp 198g (6 cái)', 'LSP007', 100, 45000, 'BanhKeo2.jpg'),
('893086492317', N'Kẹo hương bạc hà nhân socola Dynamite Big Bang gói 120g', 'LSP007', 100, 18000, 'BanhKeo3.jpg'),
('893425087163', N'Kẹo dẻo lợi khuẩn vị trái cây Bibica Huro Probiotics gói 24g', 'LSP007', 100, 9000, 'BanhKeo4.jpg'),
('893713945280', N'Kẹo ngậm không đường Mentos Clean Breath hương đào bạc hà hộp 35g', 'LSP007', 100, 43200, 'BanhKeo5.jpg'),
('893592014738', N'Nước ngọt 7 Up vị chanh lon 320ml', 'LSP008', 100, 9300, 'Nuoc1.jpg'),
('893280143697', N'Nước ngọt Pepsi không calo chai 390ml', 'LSP008', 100, 10500, 'Nuoc2.jpg'),
('893491703652', N'Nước tăng lực Redbull Thái kẽm và vitamin 250ml', 'LSP008', 100, 13500, 'Nuoc3.jpg'),
('893869204517', N'Nước tăng lực Monster Energy 355ml', 'LSP008', 100, 30000, 'Nuoc4.jpg'),
('893351209478', N'Nước tinh khiết Aquafina 355ml', 'LSP008', 100, 7500, 'Nuoc5.jpg'),
('893173046892', N'Nước giải khát có ga Aquafina Soda 320ml', 'LSP008', 100, 12000, 'Nuoc6.jpg'),
('893692174058', N'Sữa trái cây Nutriboost hương bánh quy kem 297ml', 'LSP008', 100, 13500, 'Nuoc7.jpg'),
('893948731025', N'Bia Sài Gòn Lager 330ml', 'LSP009', 100, 15000, 'BiaRuou1.jpg'),
('893064792158', N'Bia Tiger lon 250ml', 'LSP009', 100, 15900, 'BiaRuou2.jpg'),
('893210385749', N'Rượu soju Korice hương việt quất 12% chai 360ml', 'LSP009', 100, 75000, 'BiaRuou3.jpg'),
('893758291360', N'Rượu vang đỏ Sài Gòn Classic 12.5% chai 750ml', 'LSP009', 100, 135000, 'BiaRuou4.jpg'),
('893147328059', N'Nước trái cây lên men Chill Blueberry Vodka vị việt quất lon 330ml', 'LSP009', 100, 27000, 'BiaRuou5.jpg');

-- Insert data for HoaDon
INSERT INTO HoaDon (maHoaDon, ngayLap, maNhanVien, maKhachHang, maKhuyenMai, phuongThucThanhToan, tongTien) VALUES
('HD001', '2023-04-22', 'NV002', 'KH001', NULL, 'TIENMAT', 284100),
('HD002', '2023-04-22', 'NV002', 'KH002', NULL, 'CHUYENKHOAN', 393000),
('HD003', '2023-04-21', 'NV004', 'KH003', NULL, 'TIENMAT', 291000),
('HD004', '2023-04-20', 'NV004', NULL, NULL, 'TIENMAT', 159000),
('HD005', '2023-04-19', 'NV002', 'KH005', NULL, 'CHUYENKHOAN', 63600);

-- Insert data for ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maHoaDon, maSanPham, soLuong, donGia) VALUES
('HD001', '893024581736', 2, 22800),
('HD001', '893286319740', 1, 238500),
('HD002', '893518204637', 5, 21900),
('HD002', '893382917045', 10, 6600),
('HD003', '893421790865', 1, 198000),
('HD003', '893635728149', 2, 46500),
('HD004', '893215074839', 3, 15000),
('HD004', '893124708935', 5, 12900),
('HD005', '893903726184', 1, 45000),
('HD005', '893592014738', 2, 9300);

-- Insert data for NhaCungCap (NEW)
INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai, email) VALUES
('NCC001', N'Công ty TNHH Thực phẩm ABC', N'123 Nguyễn Văn Linh, Quận 7, TP.HCM', '0901234567', 'abc@gmail.com'),
('NCC002', N'Công ty CP Đồ uống XYZ', N'456 Lê Lợi, Quận 1, TP.HCM', '0912345678', 'xyz@gmail.com'),
('NCC003', N'Công ty TNHH Hóa mỹ phẩm Beauty', N'789 Cách Mạng Tháng 8, Quận 3, TP.HCM', '0923456789', 'beauty@gmail.com'),
('NCC004', N'Công ty CP Thực phẩm đông lạnh Frosty', N'101 Nguyễn Trãi, Quận 5, TP.HCM', '0934567890', 'frosty@gmail.com'),
('NCC005', N'Công ty TNHH Đồ gia dụng HomeStuff', N'202 Võ Văn Tần, Quận 3, TP.HCM', '0945678901', 'homestuff@gmail.com');

-- Insert data for PhieuNhap (NEW)
INSERT INTO PhieuNhap (maPhieuNhap, ngayNhap, maNhanVien, maNhaCungCap, ghiChu, tongTien) VALUES
('PN001', '2023-04-10 08:00:00', 'NV003', 'NCC001', NULL, 2600000),
('PN002', '2023-04-12 09:30:00', 'NV003', 'NCC002', NULL, 5410000),
('PN003', '2023-04-15 10:15:00', 'NV003', 'NCC003', NULL, 16020000),
('PN004', '2023-04-18 13:45:00', 'NV003', 'NCC004', NULL, 3120000),
('PN005', '2023-04-20 15:00:00', 'NV003', 'NCC005', NULL, 4450000);

-- Insert data for ChiTietPhieuNhap (NEW)
INSERT INTO ChiTietPhieuNhap (maPhieuNhap, maSanPham, soLuong, giaNhap) VALUES
('PN001', '893382917045', 200, 4400),
('PN001', '893509273186', 150, 6000),
('PN001', '893678214953', 100, 8200),
('PN002', '893592014738', 300, 6200),
('PN002', '893280143697', 250, 7000),
('PN002', '893491703652', 200, 9000),
('PN003', '893104592837', 100, 35000),
('PN003', '893375801294', 80, 74000),
('PN003', '893421790865', 50, 132000),
('PN004', '893215074839', 150, 10000),
('PN004', '893947260318', 120, 6000),
('PN004', '893062173489', 100, 9000),
('PN005', '893024581736', 100, 15200),
('PN005', '893147205981', 100, 11300),
('PN005', '893659328174', 150, 12000)