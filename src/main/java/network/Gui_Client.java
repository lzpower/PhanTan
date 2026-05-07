package network;

import dto.ChiTietHoaDonDto;
import dto.ChiTietPhieuNhapDto;
import dto.ChucVuDto;
import dto.HoaDonDto;
import dto.KhachHangDto;
import dto.KhuyenMaiDto;
import dto.LoaiSanPhamDto;
import dto.NhaCungCapDto;
import dto.NhanVienDto;
import dto.PhieuNhapDto;
import dto.SanPhamDto;
import dto.TaiKhoanDto;
import entity.ChiTietHoaDon;
import entity.PaymentMethod;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Gui_Client extends JFrame {

    private static final String DEFAULT_HOST = "Dell-6N85";
    private static final int DEFAULT_PORT = 9090;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JTextField txtHost = new JTextField(DEFAULT_HOST, 18);
    private final JTextField txtPort = new JTextField(String.valueOf(DEFAULT_PORT), 6);
    private final JTextField txtClientName = new JTextField(defaultClientName(), 24);
    private final JComboBox<CommandType> cboCommand = new JComboBox<>(CommandType.values());
    private final JTextArea txtPayload = new JTextArea(16, 54);
    private final JTextArea txtHelp = new JTextArea(8, 54);
    private final JTextArea txtResponse = new JTextArea(16, 54);
    private final JButton btnSend = new JButton("Gửi request");
    private final JButton btnTemplate = new JButton("Nạp mẫu");
    private final JButton btnClear = new JButton("Xóa");
    private final JLabel lblStatus = new JLabel("Sẵn sàng");

    public Gui_Client() {
        this(DEFAULT_HOST, DEFAULT_PORT, defaultClientName());
    }

    public Gui_Client(String host, int port, String clientName) {
        super("CuaHangTienLoi - Socket Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 820));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        txtHost.setText(host == null || host.isBlank() ? DEFAULT_HOST : host.trim());
        txtPort.setText(String.valueOf(port > 0 ? port : DEFAULT_PORT));
        txtClientName.setText(clientName == null || clientName.isBlank() ? defaultClientName() : clientName.trim());

        txtPayload.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtHelp.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        txtResponse.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtPayload.setLineWrap(true);
        txtPayload.setWrapStyleWord(true);
        txtHelp.setLineWrap(true);
        txtHelp.setWrapStyleWord(true);
        txtResponse.setEditable(false);
        txtHelp.setEditable(false);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        cboCommand.addActionListener(e -> loadTemplate());
        btnTemplate.addActionListener(e -> loadTemplate());
        btnClear.addActionListener(e -> {
            txtPayload.setText("");
            txtResponse.setText("");
            lblStatus.setText("Đã xóa nội dung");
        });
        btnSend.addActionListener(e -> sendRequestAsync());

        loadTemplate();
        pack();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(16, 16, 0, 16));

        JPanel connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Kết nối"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        connectionPanel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        connectionPanel.add(txtHost, gbc);
        gbc.gridx = 2;
        connectionPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 3;
        connectionPanel.add(txtPort, gbc);
        gbc.gridx = 4;
        connectionPanel.add(new JLabel("Client name:"), gbc);
        gbc.gridx = 5;
        connectionPanel.add(txtClientName, gbc);

        JPanel commandPanel = new JPanel(new GridBagLayout());
        commandPanel.setBorder(BorderFactory.createTitledBorder("Request"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(6, 8, 6, 8);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.gridx = 0; gbc2.gridy = 0;
        commandPanel.add(new JLabel("Command:"), gbc2);
        gbc2.gridx = 1;
        gbc2.weightx = 1.0;
        commandPanel.add(cboCommand, gbc2);
        gbc2.gridx = 2;
        gbc2.weightx = 0;
        commandPanel.add(btnTemplate, gbc2);
        gbc2.gridx = 3;
        commandPanel.add(btnClear, gbc2);
        gbc2.gridx = 4;
        commandPanel.add(btnSend, gbc2);

        panel.add(connectionPanel, BorderLayout.NORTH);
        panel.add(commandPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JSplitPane createCenterPanel() {
        JPanel payloadPanel = new JPanel(new BorderLayout(8, 8));
        payloadPanel.setBorder(new EmptyBorder(0, 16, 16, 8));
        payloadPanel.add(new JLabel("Payload"), BorderLayout.NORTH);
        payloadPanel.add(new JScrollPane(txtPayload), BorderLayout.CENTER);

        JPanel helpPanel = new JPanel(new BorderLayout(8, 8));
        helpPanel.setBorder(new EmptyBorder(0, 8, 16, 16));
        helpPanel.add(new JLabel("Mẫu / Hướng dẫn"), BorderLayout.NORTH);
        helpPanel.add(new JScrollPane(txtHelp), BorderLayout.CENTER);

        JSplitPane topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, payloadPanel, helpPanel);
        topSplit.setResizeWeight(0.62);
        topSplit.setDividerLocation(650);

        JPanel responsePanel = new JPanel(new BorderLayout(8, 8));
        responsePanel.setBorder(new EmptyBorder(0, 16, 16, 16));
        responsePanel.add(new JLabel("Response"), BorderLayout.NORTH);
        responsePanel.add(new JScrollPane(txtResponse), BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, responsePanel);
        verticalSplit.setResizeWeight(0.55);
        verticalSplit.setDividerLocation(420);
        return verticalSplit;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 16, 16, 16));
        lblStatus.setBorder(new EmptyBorder(8, 4, 8, 4));
        panel.add(lblStatus, BorderLayout.CENTER);
        return panel;
    }

    private void loadTemplate() {
        CommandType commandType = (CommandType) cboCommand.getSelectedItem();
        if (commandType == null) {
            return;
        }
        txtPayload.setText(sampleFor(commandType));
        txtHelp.setText(helpFor(commandType));
        txtPayload.setCaretPosition(0);
        txtHelp.setCaretPosition(0);
    }

    private void sendRequestAsync() {
        btnSend.setEnabled(false);
        lblStatus.setText("Đang gửi request...");
        SwingWorker<Response, Void> worker = new SwingWorker<>() {
            @Override
            protected Response doInBackground() throws Exception {
                return sendRequest();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    txtResponse.setText(response.toString());
                    lblStatus.setText("Đã nhận response: " + response.getMessage());
                } catch (Exception ex) {
                    txtResponse.setText(ex.toString());
                    lblStatus.setText("Lỗi khi gửi request");
                } finally {
                    btnSend.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private Response sendRequest() throws Exception {
        String host = txtHost.getText().trim();
        int port = Integer.parseInt(txtPort.getText().trim());
        String clientName = txtClientName.getText().trim();
        CommandType commandType = (CommandType) cboCommand.getSelectedItem();
        if (commandType == null) {
            throw new IllegalStateException("Chưa chọn command.");
        }

        Object data = buildPayload(commandType, txtPayload.getText());
        Request request = new Request(clientName, commandType, data);

        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        }
    }

    private Object buildPayload(CommandType commandType, String rawText) {
        Map<String, String> values = parseKeyValueLines(rawText);

        return switch (commandType) {
            case PING,
                 CHUCVU_LOAD_ALL,
                 CHUCVU_NEXT_ID,
                 LOAISANPHAM_LOAD_ALL,
                 LOAISANPHAM_NEXT_ID,
                 NHACUNGCAP_LOAD_ALL,
                 NHACUNGCAP_NEXT_ID,
                 SANPHAM_LOAD_ALL,
                 SANPHAM_NEXT_ID,
                 HOADON_LOAD_ALL,
                 HOADON_NEXT_ID,
                 PHIEUNHAP_LOAD_ALL,
                 PHIEUNHAP_NEXT_ID,
                 PHIEUNHAP_LOAD_DETAILS,
                 CHITIETHOADON_LOAD_ALL,
                 CHITIETPHIEUNHAP_LOAD_BY_PHIEU_NHAP,
                 KHACHHANG_LOAD_ALL,
                 KHACHHANG_NEXT_ID,
                 NHANVIEN_LOAD_ALL,
                 NHANVIEN_NEXT_ID,
                 TAIKHOAN_LOAD_ALL,
                 KHUYENMAI_LOAD_ALL,
                 KHUYENMAI_NEXT_ID -> null;

            case CHUCVU_FIND_BY_ID,
                 CHUCVU_SEARCH,
                 CHUCVU_DELETE -> readDirectValue(values, rawText, "maChucVu", "query", "value");
            case LOAISANPHAM_FIND_BY_ID,
                 LOAISANPHAM_SEARCH,
                 LOAISANPHAM_DELETE -> readDirectValue(values, rawText, "maLoaiSanPham", "query", "value");
            case NHACUNGCAP_FIND_BY_ID,
                 NHACUNGCAP_SEARCH,
                 NHACUNGCAP_DELETE -> readDirectValue(values, rawText, "maNhaCungCap", "query", "value");
            case SANPHAM_FIND_BY_ID,
                 SANPHAM_SEARCH,
                 SANPHAM_DELETE -> readDirectValue(values, rawText, "maSanPham", "query", "value");
            case HOADON_FIND_BY_ID,
                 HOADON_SEARCH,
                 HOADON_DELETE -> readDirectValue(values, rawText, "maHoaDon", "query", "value");
            case PHIEUNHAP_FIND_BY_ID,
                 PHIEUNHAP_SEARCH,
                 PHIEUNHAP_DELETE -> readDirectValue(values, rawText, "maPhieuNhap", "query", "value");
            case CHITIETHOADON_FIND_BY_ID,
                 CHITIETHOADON_DELETE -> parseChiTietHoaDonId(rawText, values);
            case CHITIETHOADON_LOAD_BY_HOA_DON -> readDirectValue(values, rawText, "maHoaDon", "query", "value");
            case KHACHHANG_FIND_BY_ID,
                 KHACHHANG_SEARCH,
                 KHACHHANG_DELETE -> readDirectValue(values, rawText, "maKhachHang", "query", "value");
            case NHANVIEN_FIND_BY_ID,
                 NHANVIEN_SEARCH,
                 NHANVIEN_DELETE -> readDirectValue(values, rawText, "maNhanVien", "query", "value");
            case TAIKHOAN_FIND_BY_ID,
                 TAIKHOAN_SEARCH,
                 TAIKHOAN_DELETE -> readDirectValue(values, rawText, "tenDangNhap", "query", "value");
            case KHUYENMAI_FIND_BY_ID,
                 KHUYENMAI_SEARCH,
                 KHUYENMAI_DELETE -> readDirectValue(values, rawText, "maKhuyenMai", "query", "value");

            case CHUCVU_SAVE, CHUCVU_UPDATE -> parseChucVu(values);
            case LOAISANPHAM_SAVE, LOAISANPHAM_UPDATE -> parseLoaiSanPham(values);
            case NHACUNGCAP_SAVE, NHACUNGCAP_UPDATE -> parseNhaCungCap(values);
            case SANPHAM_SAVE, SANPHAM_UPDATE -> parseSanPham(values);
            case HOADON_SAVE, HOADON_UPDATE -> parseHoaDon(values);
            case PHIEUNHAP_SAVE -> parsePhieuNhapPayload(values);
            case KHUYENMAI_SAVE, KHUYENMAI_UPDATE -> parseKhuyenMai(values);
            case KHACHHANG_SAVE, KHACHHANG_UPDATE -> parseKhachHang(values);
            case NHANVIEN_SAVE, NHANVIEN_UPDATE -> parseNhanVien(values);
            case TAIKHOAN_UPDATE -> parseTaiKhoan(values);
            case TAIKHOAN_CREATE_DEFAULT -> parseCreateDefaultAccount(values);
            case TAIKHOAN_RESET_PASSWORD -> parseResetPassword(values);
            case NHANVIEN_BUILD_USERNAME -> parseBuildUsername(values);
            case THONGKE_DOANHTHU,
                 THONGKE_TOP_KHACH_HANG,
                 THONGKE_TOP_SAN_PHAM -> parseThongKe(values);
            case CHITIETHOADON_SAVE, CHITIETHOADON_UPDATE -> parseChiTietHoaDon(values);
        };
    }

    private Object parseChucVu(Map<String, String> values) {
        return ChucVuDto.builder()
                .maChucVu(value(values, "maChucVu", "CV001"))
                .tenChucVu(value(values, "tenChucVu", "Quản lý"))
                .build();
    }

    private Object parseLoaiSanPham(Map<String, String> values) {
        return LoaiSanPhamDto.builder()
                .maLoaiSanPham(value(values, "maLoaiSanPham", "LSP001"))
                .tenLoaiSanPham(value(values, "tenLoaiSanPham", "Danh mục mẫu"))
                .build();
    }

    private Object parseNhaCungCap(Map<String, String> values) {
        return NhaCungCapDto.builder()
                .maNhaCungCap(value(values, "maNhaCungCap", "NCC001"))
                .tenNhaCungCap(value(values, "tenNhaCungCap", "Nhà cung cấp mẫu"))
                .diaChi(value(values, "diaChi", "Địa chỉ mẫu"))
                .soDienThoai(value(values, "soDienThoai", "0987654321"))
                .email(value(values, "email", "demo@example.com"))
                .build();
    }

    private Object parseSanPham(Map<String, String> values) {
        return SanPhamDto.builder()
                .maSanPham(value(values, "maSanPham", "SP001"))
                .tenSanPham(value(values, "tenSanPham", "Sản phẩm mẫu"))
                .maLoaiSanPham(value(values, "maLoaiSanPham", "LSP001"))
                .tenLoaiSanPham(values.get("tenLoaiSanPham"))
                .soLuongHienCo(parseInt(values.get("soLuongHienCo"), 10))
                .giaBan(parseDouble(values.get("giaBan"), 10000D))
                .urlHinhAnh(values.getOrDefault("urlHinhAnh", ""))
                .build();
    }

    private Object parseHoaDon(Map<String, String> values) {
        return HoaDonDto.builder()
                .maHoaDon(value(values, "maHoaDon", "HD001"))
                .ngayLap(parseLocalDate(values.get("ngayLap"), LocalDate.now()))
                .maNhanVien(values.get("maNhanVien"))
                .tenNhanVien(values.get("tenNhanVien"))
                .maKhachHang(values.get("maKhachHang"))
                .tenKhachHang(values.get("tenKhachHang"))
                .maKhuyenMai(values.get("maKhuyenMai"))
                .tenKhuyenMai(values.get("tenKhuyenMai"))
                .phuongThucThanhToan(parsePaymentMethod(values.get("phuongThucThanhToan")))
                .tongTien(parseDouble(values.get("tongTien"), 0D))
                .build();
    }

    private Object parsePhieuNhapPayload(Map<String, String> values) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("dto", PhieuNhapDto.builder()
                .maPhieuNhap(value(values, "maPhieuNhap", "PN001"))
                .ngayNhap(parseLocalDateTime(values.get("ngayNhap"), LocalDateTime.now()))
                .maNhaCungCap(value(values, "maNhaCungCap", "NCC001"))
                .tenNhaCungCap(values.get("tenNhaCungCap"))
                .maNhanVien(values.get("maNhanVien"))
                .tenNhanVien(values.get("tenNhanVien"))
                .ghiChu(values.get("ghiChu"))
                .soMatHang(parseInt(values.get("soMatHang"), 0))
                .tongTien(parseDouble(values.get("tongTien"), 0D))
                .build());
        payload.put("chiTietList", parseChiTietPhieuNhapList(values.get("details")));
        return payload;
    }

    private Object parseKhuyenMai(Map<String, String> values) {
        return KhuyenMaiDto.builder()
                .maKhuyenMai(value(values, "maKhuyenMai", "KM001"))
                .tenKhuyenMai(value(values, "tenKhuyenMai", "Khuyến mãi mẫu"))
                .giaTriKhuyenMai(parseDouble(values.get("giaTriKhuyenMai"), 10D))
                .ngayBatDau(parseLocalDate(values.get("ngayBatDau"), LocalDate.now()))
                .ngayKetThuc(parseLocalDate(values.get("ngayKetThuc"), LocalDate.now().plusDays(7)))
                .build();
    }

    private Object parseKhachHang(Map<String, String> values) {
        return KhachHangDto.builder()
                .maKhachHang(value(values, "maKhachHang", "KH001"))
                .tenKhachHang(value(values, "tenKhachHang", "Khách hàng mẫu"))
                .soDienThoai(value(values, "soDienThoai", "0988888888"))
                .soDiem(parseInt(values.get("soDiem"), 0))
                .build();
    }

    private Object parseNhanVien(Map<String, String> values) {
        return NhanVienDto.builder()
                .maNhanVien(value(values, "maNhanVien", "NV001"))
                .tenNhanVien(value(values, "tenNhanVien", "Nhân viên mẫu"))
                .ngaySinh(parseLocalDate(values.get("ngaySinh"), LocalDate.of(2000, 1, 1)))
                .gioiTinh(value(values, "gioiTinh", "Nam"))
                .email(value(values, "email", "demo@example.com"))
                .maChucVu(value(values, "maChucVu", "CV001"))
                .tenChucVu(values.get("tenChucVu"))
                .soDienThoai(value(values, "soDienThoai", "0987654321"))
                .tenTaiKhoan(values.get("tenTaiKhoan"))
                .build();
    }

    private Object parseTaiKhoan(Map<String, String> values) {
        TaiKhoanDto dto = new TaiKhoanDto();
        dto.setTenDangNhap(value(values, "tenDangNhap", "user001"));
        dto.setMatKhau(value(values, "matKhau", "123456"));
        dto.setMaChucVu(values.get("maChucVu"));
        dto.setTenChucVu(values.get("tenChucVu"));
        dto.setMaNhanVien(values.get("maNhanVien"));
        dto.setTenNhanVien(values.get("tenNhanVien"));
        dto.setEmail(values.get("email"));
        return dto;
    }

    private Object parseCreateDefaultAccount(Map<String, String> values) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("tenDangNhap", value(values, "tenDangNhap", "user001"));
        payload.put("matKhau", value(values, "matKhau", "123456"));
        payload.put("maChucVu", values.get("maChucVu"));
        payload.put("maNhanVien", values.get("maNhanVien"));
        return payload;
    }

    private Object parseResetPassword(Map<String, String> values) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("tenDangNhap", value(values, "tenDangNhap", "user001"));
        payload.put("matKhau", value(values, "matKhau", "123456"));
        return payload;
    }

    private Object parseBuildUsername(Map<String, String> values) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("maNhanVien", value(values, "maNhanVien", "NV001"));
        payload.put("tenNhanVien", value(values, "tenNhanVien", "Nhân viên mẫu"));
        return payload;
    }

    private Object parseThongKe(Map<String, String> values) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("year", parseInt(values.get("year"), LocalDate.now().getYear()));
        if (values.containsKey("compareYear") && !values.get("compareYear").isBlank()) {
            payload.put("compareYear", parseInt(values.get("compareYear"), LocalDate.now().getYear() - 1));
        }
        if (values.containsKey("limit") && !values.get("limit").isBlank()) {
            payload.put("limit", parseInt(values.get("limit"), 5));
        }
        return payload;
    }

    private Object parseChiTietHoaDon(Map<String, String> values) {
        return ChiTietHoaDonDto.builder()
                .maHoaDon(value(values, "maHoaDon", "HD001"))
                .maSanPham(value(values, "maSanPham", "SP001"))
                .tenSanPham(values.get("tenSanPham"))
                .soLuong(parseInt(values.get("soLuong"), 1))
                .donGia(parseDouble(values.get("donGia"), 10000D))
                .build();
    }

    private Object parseChiTietHoaDonId(String rawText, Map<String, String> values) {
        String maHoaDon = firstNonBlank(values, "maHoaDon", "hoaDon");
        String maSanPham = firstNonBlank(values, "maSanPham", "sanPham");
        if ((maHoaDon == null || maSanPham == null) && rawText != null && rawText.contains("|")) {
            String[] parts = rawText.split("\\|", 2);
            if (parts.length == 2) {
                if (maHoaDon == null) {
                    maHoaDon = parts[0].trim();
                }
                if (maSanPham == null) {
                    maSanPham = parts[1].trim();
                }
            }
        }
        if (maHoaDon == null || maSanPham == null) {
            throw new IllegalArgumentException("Cần nhập maHoaDon và maSanPham cho chi tiết hóa đơn.");
        }
        return new ChiTietHoaDon.ChiTietHoaDonId(maHoaDon, maSanPham);
    }

    private List<ChiTietPhieuNhapDto> parseChiTietPhieuNhapList(String rawDetails) {
        if (rawDetails == null || rawDetails.isBlank()) {
            return List.of();
        }
        List<ChiTietPhieuNhapDto> result = new ArrayList<>();
        for (String item : rawDetails.split(";")) {
            String text = item.trim();
            if (text.isEmpty()) {
                continue;
            }
            String[] parts = text.split("\\|");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Mỗi chi tiết phiếu nhập phải có dạng maSanPham|soLuong|giaNhap.");
            }
            result.add(ChiTietPhieuNhapDto.builder()
                    .maSanPham(parts[0].trim())
                    .soLuong(parseInt(parts[1].trim(), 1))
                    .giaNhap(parseDouble(parts[2].trim(), 0D))
                    .build());
        }
        return result;
    }

    private Map<String, String> parseKeyValueLines(String rawText) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawText == null) {
            return values;
        }
        for (String line : rawText.split("\\R")) {
            String text = line.trim();
            if (text.isEmpty() || text.startsWith("#")) {
                continue;
            }
            int index = text.indexOf('=');
            if (index <= 0) {
                continue;
            }
            String key = text.substring(0, index).trim();
            String value = text.substring(index + 1).trim();
            values.put(key, value);
        }
        return values;
    }

    private String readDirectValue(Map<String, String> values, String rawText, String... keys) {
        String existing = firstNonBlank(values, keys);
        if (existing != null) {
            return existing;
        }
        if (rawText == null) {
            return null;
        }
        String trimmed = rawText.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(Map<String, String> values, String... keys) {
        for (String key : keys) {
            String value = values.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private String value(Map<String, String> values, String key, String defaultValue) {
        String value = values.get(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private int parseInt(String text, int defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(text.trim());
    }

    private double parseDouble(String text, double defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return Double.parseDouble(text.trim());
    }

    private LocalDate parseLocalDate(String text, LocalDate defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return LocalDate.parse(text.trim(), DATE_FORMATTER);
    }

    private LocalDateTime parseLocalDateTime(String text, LocalDateTime defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return LocalDateTime.parse(text.trim(), DATE_TIME_FORMATTER);
    }

    private PaymentMethod parsePaymentMethod(String text) {
        if (text == null || text.isBlank()) {
            return PaymentMethod.TIENMAT;
        }
        String normalized = text.trim();
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(normalized) || method.getDisplayName().equalsIgnoreCase(normalized)) {
                return method;
            }
        }
        return PaymentMethod.fromDisplayName(normalized);
    }

    private String sampleFor(CommandType commandType) {
        return switch (commandType) {
            case PING -> "";

            case CHUCVU_FIND_BY_ID, CHUCVU_SEARCH, CHUCVU_DELETE -> "maChucVu=CV001";
            case CHUCVU_SAVE, CHUCVU_UPDATE -> "maChucVu=CV001\ntenChucVu=Quản lý";
            case CHUCVU_LOAD_ALL, CHUCVU_NEXT_ID -> "";

            case LOAISANPHAM_FIND_BY_ID, LOAISANPHAM_SEARCH, LOAISANPHAM_DELETE -> "maLoaiSanPham=LSP001";
            case LOAISANPHAM_SAVE, LOAISANPHAM_UPDATE -> "maLoaiSanPham=LSP001\ntenLoaiSanPham=Đồ uống";
            case LOAISANPHAM_LOAD_ALL, LOAISANPHAM_NEXT_ID -> "";

            case NHACUNGCAP_FIND_BY_ID, NHACUNGCAP_SEARCH, NHACUNGCAP_DELETE -> "maNhaCungCap=NCC001";
            case NHACUNGCAP_SAVE, NHACUNGCAP_UPDATE -> "maNhaCungCap=NCC001\ntenNhaCungCap=Công ty A\ndiaChi=Địa chỉ A\nsoDienThoai=0912345678\nemail=demo@example.com";
            case NHACUNGCAP_LOAD_ALL, NHACUNGCAP_NEXT_ID -> "";

            case SANPHAM_FIND_BY_ID, SANPHAM_SEARCH, SANPHAM_DELETE -> "maSanPham=SP001";
            case SANPHAM_SAVE, SANPHAM_UPDATE -> "maSanPham=SP001\ntenSanPham=Nước ngọt\nmaLoaiSanPham=LSP001\ntenLoaiSanPham=Đồ uống\nsoLuongHienCo=50\ngiaBan=12000\nurlHinhAnh=/img/sp1.png";
            case SANPHAM_LOAD_ALL, SANPHAM_NEXT_ID -> "";

            case HOADON_FIND_BY_ID, HOADON_SEARCH, HOADON_DELETE -> "maHoaDon=HD001";
            case HOADON_SAVE, HOADON_UPDATE -> "maHoaDon=HD001\nngayLap=2026-05-07\nmaNhanVien=NV001\nmaKhachHang=KH001\nmaKhuyenMai=KM001\nphuongThucThanhToan=Tiền mặt\ntongTien=150000";
            case HOADON_LOAD_ALL, HOADON_NEXT_ID -> "";

            case PHIEUNHAP_FIND_BY_ID, PHIEUNHAP_SEARCH, PHIEUNHAP_DELETE, PHIEUNHAP_LOAD_DETAILS -> "maPhieuNhap=PN001";
            case PHIEUNHAP_SAVE -> "maPhieuNhap=PN001\nngayNhap=2026-05-07 10:30\nmaNhaCungCap=NCC001\nmaNhanVien=NV001\nghiChu=Nhập hàng demo\ndetails=SP001|10|12000;SP002|5|15000";
            case PHIEUNHAP_LOAD_ALL, PHIEUNHAP_NEXT_ID -> "";

            case CHITIETHOADON_FIND_BY_ID, CHITIETHOADON_DELETE -> "maHoaDon=HD001\nmaSanPham=SP001";
            case CHITIETHOADON_LOAD_BY_HOA_DON -> "maHoaDon=HD001";
            case CHITIETHOADON_SAVE, CHITIETHOADON_UPDATE -> "maHoaDon=HD001\nmaSanPham=SP001\nsoLuong=2\ndonGia=12000";
            case CHITIETHOADON_LOAD_ALL -> "";

            case CHITIETPHIEUNHAP_LOAD_BY_PHIEU_NHAP -> "maPhieuNhap=PN001";

            case KHACHHANG_FIND_BY_ID, KHACHHANG_SEARCH, KHACHHANG_DELETE -> "maKhachHang=KH001";
            case KHACHHANG_SAVE, KHACHHANG_UPDATE -> "maKhachHang=KH001\ntenKhachHang=Khách hàng mẫu\nsoDienThoai=0988888888\nsoDiem=10";
            case KHACHHANG_LOAD_ALL, KHACHHANG_NEXT_ID -> "";

            case NHANVIEN_FIND_BY_ID, NHANVIEN_SEARCH, NHANVIEN_DELETE -> "maNhanVien=NV001";
            case NHANVIEN_SAVE, NHANVIEN_UPDATE -> "maNhanVien=NV001\ntenNhanVien=Nhân viên mẫu\nngaySinh=2000-01-01\ngioiTinh=Nam\nemail=demo@example.com\nmaChucVu=CV001\nsoDienThoai=0987654321\ntenTaiKhoan=nv001";
            case NHANVIEN_LOAD_ALL, NHANVIEN_NEXT_ID -> "";
            case NHANVIEN_BUILD_USERNAME -> "maNhanVien=NV001\ntenNhanVien=Nhân viên mẫu";

            case TAIKHOAN_FIND_BY_ID, TAIKHOAN_SEARCH, TAIKHOAN_DELETE -> "tenDangNhap=nv001";
            case TAIKHOAN_UPDATE -> "tenDangNhap=nv001\nmatKhau=123456\nmaNhanVien=NV001";
            case TAIKHOAN_CREATE_DEFAULT -> "tenDangNhap=nv001\nmatKhau=123456\nmaChucVu=CV001\nmaNhanVien=NV001";
            case TAIKHOAN_RESET_PASSWORD -> "tenDangNhap=nv001\nmatKhau=123456";
            case TAIKHOAN_LOAD_ALL -> "";

            case KHUYENMAI_FIND_BY_ID, KHUYENMAI_SEARCH, KHUYENMAI_DELETE -> "maKhuyenMai=KM001";
            case KHUYENMAI_SAVE, KHUYENMAI_UPDATE -> "maKhuyenMai=KM001\ntenKhuyenMai=Giảm giá tháng 5\ngiaTriKhuyenMai=10\nngayBatDau=2026-05-01\nngayKetThuc=2026-05-31";
            case KHUYENMAI_LOAD_ALL, KHUYENMAI_NEXT_ID -> "";

            case THONGKE_DOANHTHU -> "year=2026\ncompareYear=2025";
            case THONGKE_TOP_KHACH_HANG, THONGKE_TOP_SAN_PHAM -> "year=2026\nlimit=5";
        };
    }

    private String helpFor(CommandType commandType) {
        return switch (commandType) {
            case PING -> "Gửi request kiểm tra kết nối.";

            case CHUCVU_FIND_BY_ID, CHUCVU_SEARCH, CHUCVU_DELETE -> "Nhập maChucVu.";
            case CHUCVU_SAVE, CHUCVU_UPDATE -> "maChucVu, tenChucVu.";
            case CHUCVU_LOAD_ALL, CHUCVU_NEXT_ID -> "Không cần payload.";

            case LOAISANPHAM_FIND_BY_ID, LOAISANPHAM_SEARCH, LOAISANPHAM_DELETE -> "Nhập maLoaiSanPham.";
            case LOAISANPHAM_SAVE, LOAISANPHAM_UPDATE -> "maLoaiSanPham, tenLoaiSanPham.";
            case LOAISANPHAM_LOAD_ALL, LOAISANPHAM_NEXT_ID -> "Không cần payload.";

            case NHACUNGCAP_FIND_BY_ID, NHACUNGCAP_SEARCH, NHACUNGCAP_DELETE -> "Nhập maNhaCungCap.";
            case NHACUNGCAP_SAVE, NHACUNGCAP_UPDATE -> "maNhaCungCap, tenNhaCungCap, diaChi, soDienThoai, email.";
            case NHACUNGCAP_LOAD_ALL, NHACUNGCAP_NEXT_ID -> "Không cần payload.";

            case SANPHAM_FIND_BY_ID, SANPHAM_SEARCH, SANPHAM_DELETE -> "Nhập maSanPham.";
            case SANPHAM_SAVE, SANPHAM_UPDATE -> "maSanPham, tenSanPham, maLoaiSanPham, soLuongHienCo, giaBan, urlHinhAnh.";
            case SANPHAM_LOAD_ALL, SANPHAM_NEXT_ID -> "Không cần payload.";

            case HOADON_FIND_BY_ID, HOADON_SEARCH, HOADON_DELETE -> "Nhập maHoaDon.";
            case HOADON_SAVE, HOADON_UPDATE -> "maHoaDon, ngayLap(yyyy-MM-dd), maNhanVien, maKhachHang, maKhuyenMai, phuongThucThanhToan, tongTien.";
            case HOADON_LOAD_ALL, HOADON_NEXT_ID -> "Không cần payload.";

            case PHIEUNHAP_FIND_BY_ID, PHIEUNHAP_SEARCH, PHIEUNHAP_DELETE -> "Nhập maPhieuNhap.";
            case PHIEUNHAP_SAVE -> "dto: maPhieuNhap, ngayNhap(yyyy-MM-dd HH:mm), maNhaCungCap, maNhanVien, ghiChu; details=maSP|soLuong|giaNhap;...";
            case PHIEUNHAP_LOAD_ALL, PHIEUNHAP_NEXT_ID, PHIEUNHAP_LOAD_DETAILS -> "Nhập maPhieuNhap.";

            case CHITIETHOADON_FIND_BY_ID, CHITIETHOADON_DELETE -> "Nhập maHoaDon và maSanPham, cách nhau bằng | hoặc key=value.";
            case CHITIETHOADON_LOAD_BY_HOA_DON -> "Nhập maHoaDon.";
            case CHITIETHOADON_SAVE, CHITIETHOADON_UPDATE -> "maHoaDon, maSanPham, soLuong, donGia.";
            case CHITIETHOADON_LOAD_ALL -> "Không cần payload.";

            case CHITIETPHIEUNHAP_LOAD_BY_PHIEU_NHAP -> "Nhập maPhieuNhap.";

            case KHACHHANG_FIND_BY_ID, KHACHHANG_SEARCH, KHACHHANG_DELETE -> "Nhập maKhachHang.";
            case KHACHHANG_SAVE, KHACHHANG_UPDATE -> "maKhachHang, tenKhachHang, soDienThoai, soDiem.";
            case KHACHHANG_LOAD_ALL, KHACHHANG_NEXT_ID -> "Không cần payload.";

            case NHANVIEN_FIND_BY_ID, NHANVIEN_SEARCH, NHANVIEN_DELETE -> "Nhập maNhanVien.";
            case NHANVIEN_SAVE, NHANVIEN_UPDATE -> "maNhanVien, tenNhanVien, ngaySinh(yyyy-MM-dd), gioiTinh, email, maChucVu, soDienThoai.";
            case NHANVIEN_LOAD_ALL, NHANVIEN_NEXT_ID -> "Không cần payload.";
            case NHANVIEN_BUILD_USERNAME -> "maNhanVien, tenNhanVien.";

            case TAIKHOAN_FIND_BY_ID, TAIKHOAN_SEARCH, TAIKHOAN_DELETE -> "Nhập tenDangNhap.";
            case TAIKHOAN_UPDATE -> "tenDangNhap, matKhau, maNhanVien.";
            case TAIKHOAN_CREATE_DEFAULT -> "tenDangNhap, matKhau, maChucVu, maNhanVien.";
            case TAIKHOAN_RESET_PASSWORD -> "tenDangNhap, matKhau.";
            case TAIKHOAN_LOAD_ALL -> "Không cần payload.";

            case KHUYENMAI_FIND_BY_ID, KHUYENMAI_SEARCH, KHUYENMAI_DELETE -> "Nhập maKhuyenMai.";
            case KHUYENMAI_SAVE, KHUYENMAI_UPDATE -> "maKhuyenMai, tenKhuyenMai, giaTriKhuyenMai, ngayBatDau, ngayKetThuc.";
            case KHUYENMAI_LOAD_ALL, KHUYENMAI_NEXT_ID -> "Không cần payload.";

            case THONGKE_DOANHTHU -> "year, compareYear.";
            case THONGKE_TOP_KHACH_HANG, THONGKE_TOP_SAN_PHAM -> "year, limit.";
        };
    }

    public static String defaultClientName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "client-" + System.getProperty("user.name", "unknown");
        }
    }

    public static void main(String[] args) {
        String host = args.length > 0 && args[0] != null && !args[0].isBlank() ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        String clientName = args.length > 2 && args[2] != null && !args[2].isBlank() ? args[2] : defaultClientName();
        SwingUtilities.invokeLater(() -> new Gui_Client(host, port, clientName).setVisible(true));
    }
}