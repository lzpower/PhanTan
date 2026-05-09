package gui;

import dto.ChiTietHoaDonDto;
import dto.HoaDonDto;
import dto.KhachHangDto;
import dto.KhuyenMaiDto;
import dto.NhanVienDto;
import dto.SanPhamDto;
import dto.TaiKhoanDto;
import entity.PaymentMethod;
import service.ChiTietHoaDonService;
import service.HoaDonService;
import service.KhachHangService;
import service.KhuyenMaiService;
import service.NhanVienService;
import service.SanPhamService;
import service.TaiKhoanService;
import service.ServiceFactory;
import service.impl.ChiTietHoaDonServiceImpl;
import service.impl.HoaDonServiceImpl;
import service.impl.KhachHangServiceImpl;
import service.impl.KhuyenMaiServiceImpl;
import service.impl.NhanVienServiceImpl;
import service.impl.SanPhamServiceImpl;
import service.impl.TaiKhoanServiceImpl;
import util.TableUtility;
import util.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gui_BanHang extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        VND_FORMAT = new DecimalFormat("#,##0", symbols);
        VND_FORMAT.setMaximumFractionDigits(0);
    }

    // ── SePay / VietQR ──────────────────────────────────────────────────────────
    private static final String SEPAY_WEBHOOK_URL  = "https://sepay-receiver.thinh-tools.workers.dev";
//    private static final String VIETQR_BANK_CODE   = "970418";          // BIDV BIN
    private static final String BANK_ACCOUNT       = "96247NHT3075";
    private static final String ACCOUNT_NAME       = "NHT3075";
    private static final HttpClient HTTP_CLIENT    = HttpClient.newHttpClient();

    private final HoaDonService hoaDonService = ServiceFactory.get(HoaDonService.class, HoaDonServiceImpl::new);
    private final ChiTietHoaDonService chiTietHoaDonService = ServiceFactory.get(ChiTietHoaDonService.class, ChiTietHoaDonServiceImpl::new);
    private final SanPhamService sanPhamService = ServiceFactory.get(SanPhamService.class, SanPhamServiceImpl::new);
    private final KhuyenMaiService khuyenMaiService = ServiceFactory.get(KhuyenMaiService.class, KhuyenMaiServiceImpl::new);
    private final KhachHangService khachHangService = ServiceFactory.get(KhachHangService.class, KhachHangServiceImpl::new);
    private final TaiKhoanService taiKhoanService = ServiceFactory.get(TaiKhoanService.class, TaiKhoanServiceImpl::new);
    private final NhanVienService nhanVienService = ServiceFactory.get(NhanVienService.class, NhanVienServiceImpl::new);

    private final Map<String, CartItem> cart = new LinkedHashMap<>();

    private String maNhanVienHienTai = "";
    private String tenNhanVienHienTai = "";
    private String maHoaDonHienTai = "";
    private boolean daCoHoaDon = false;
    private KhachHangDto khachHangHienTai;
    private boolean suDungDiem = false;
    private int diemDaSuDung = 0;
    private PaymentMethod paymentMode = PaymentMethod.TIENMAT;

    private boolean loadingProducts = false;
    private boolean syncingSelection = false;
    private boolean refreshingTable = false;
    private boolean updatingCashChange = false;

    private JComboBox<ProductOption> cboSanPham;
    private JTextField txtSoLuong;
    private JTable tblChiTiet;
    private DefaultTableModel chiTietModel;

    private JTextField txtMaHoaDon;
    private JTextField txtNhanVien;
    private JTextField txtNgayTao;

    private JTextField txtTimKhachHang;
    private JComboBox<PromotionOption> cboKhuyenMai;

    private JLabel lblTenKhachHang;
    private JLabel lblDiemKhachHang;
    private JLabel lblTongCong;
    private JLabel lblGiamGia;
    private JPanel suDungDiemRow;
    private JLabel lblSuDungDiem;
    private JLabel lblTongTien;
    private JTextField txtTienKhachDua;
    private JTextField txtTienThoi;
    private JPanel cashInputRow;
    private JPanel changeRow;

    private JButton btnTaoHoaDon;
    private JButton btnXoaSanPham;
    private JButton btnLamRong;
    private JButton btnTimKhachHang;
    private JButton btnDungDiem;
    private JButton btnTienMat;
    private JButton btnChuyenKhoan;
    private JButton btnThanhToan;

    public Gui_BanHang(String tenDangNhap) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        resolveEmployee(tenDangNhap);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadProducts();
        loadPromotions();
        resetHoaDon();

        btnTaoHoaDon.addActionListener(e -> taoHoaDonMoi());
        btnXoaSanPham.addActionListener(e -> xoaSanPhamKhoiHoaDon());
        btnLamRong.addActionListener(e -> lamRongHoaDon());
        btnTimKhachHang.addActionListener(e -> timKhachHang());
        btnDungDiem.addActionListener(e -> toggleDungDiem());
        btnTienMat.addActionListener(e -> setPaymentMode(PaymentMethod.TIENMAT));
        btnChuyenKhoan.addActionListener(e -> setPaymentMode(PaymentMethod.CHUYENKHOAN));
        btnThanhToan.addActionListener(e -> thanhToan());

        cboKhuyenMai.addActionListener(e -> updateTotals());
        cboSanPham.addActionListener(e -> {
            if (!loadingProducts && !syncingSelection) {
                addOrUpdateFromForm();
            }
        });
        txtSoLuong.addActionListener(e -> addOrUpdateFromForm());
        txtTienKhachDua.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                // Khi click vào: xóa định dạng, chỉ giữ số thuần để gõ tiếp
                updatingCashChange = true;
                try {
                    String raw = txtTienKhachDua.getText()
                            .replace(".", "").replace(",", "").replace("đ", "").replaceAll("\\s+", "").trim();
                    txtTienKhachDua.setText(raw.isEmpty() || raw.equals("0") ? "" : raw);
                } finally {
                    updatingCashChange = false;
                }
                txtTienKhachDua.selectAll();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Khi rời ô: định dạng lại có dấu chấm
                formatTienKhachDua();
            }
        });

        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!updatingCashChange) updateCashChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!updatingCashChange) updateCashChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!updatingCashChange) updateCashChange();
            }
        });

        chiTietModel.addTableModelListener(e -> {
            if (refreshingTable || e.getType() != TableModelEvent.UPDATE || e.getColumn() != 4) {
                return;
            }
            updateQuantityFromTable(e.getFirstRow());
        });

        tblChiTiet.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncFormFromSelectedRow();
                updateButtonState();
            }
        });

        updateButtonState();
        updatePaymentModeUi();
    }

    private void resolveEmployee(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            tenNhanVienHienTai = "Nhân viên";
            return;
        }

        TaiKhoanDto taiKhoan = taiKhoanService.findById(tenDangNhap);
        if (taiKhoan == null) {
            tenNhanVienHienTai = tenDangNhap;
            return;
        }

        maNhanVienHienTai = taiKhoan.getMaNhanVien() == null ? "" : taiKhoan.getMaNhanVien();
        if (taiKhoan.getTenNhanVien() != null && !taiKhoan.getTenNhanVien().isBlank()) {
            tenNhanVienHienTai = taiKhoan.getTenNhanVien();
            return;
        }

        if (!maNhanVienHienTai.isBlank()) {
            NhanVienDto nv = nhanVienService.findById(maNhanVienHienTai);
            if (nv != null && nv.getTenNhanVien() != null && !nv.getTenNhanVien().isBlank()) {
                tenNhanVienHienTai = nv.getTenNhanVien();
                return;
            }
        }

        tenNhanVienHienTai = tenDangNhap;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        btnTaoHoaDon = new JButton("TẠO HÓA ĐƠN");
        UiStyle.styleButton(btnTaoHoaDon, new Color(0, 153, 204));
        btnTaoHoaDon.setPreferredSize(new Dimension(240, 52));
        header.add(btnTaoHoaDon, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        txtMaHoaDon = createTopReadonly();
        txtNhanVien = createTopReadonly();
        txtNgayTao = createTopReadonly();

        addHeaderField(infoPanel, 0, "Mã hóa đơn", txtMaHoaDon);
        addHeaderField(infoPanel, 1, "Nhân viên", txtNhanVien);
        addHeaderField(infoPanel, 2, "Ngày tạo", txtNgayTao);

        header.add(infoPanel, BorderLayout.CENTER);
        return header;
    }

    private JTextField createTopReadonly() {
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setFont(new Font("Segoe UI", Font.BOLD, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        field.setBackground(UiStyle.PRIMARY);
        field.setForeground(Color.WHITE);
        return field;
    }

    private void addHeaderField(JPanel panel, int col, String labelText, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = col * 2;
        gbc.insets = new Insets(0, 0, 0, 8);

        JLabel label = new JLabel(labelText + ":");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 16);
        panel.add(field, gbc);
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout(14, 0));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        main.add(createLeftPanel(), BorderLayout.CENTER);
        main.add(createRightPanel(), BorderLayout.EAST);
        return main;
    }

    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setBackground(Color.WHITE);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setBackground(Color.WHITE);

        btnXoaSanPham = new JButton("Xóa sản phẩm");
        UiStyle.styleButton(btnXoaSanPham, new Color(234, 88, 12));
        btnXoaSanPham.setPreferredSize(new Dimension(160, 42));

        btnLamRong = new JButton("Làm rỗng");
        UiStyle.styleButton(btnLamRong, new Color(26, 107, 127));
        btnLamRong.setPreferredSize(new Dimension(150, 42));

        topActions.add(btnXoaSanPham);
        topActions.add(btnLamRong);
        left.add(topActions, BorderLayout.NORTH);

        JPanel addProductPanel = new JPanel(new GridBagLayout());
        addProductPanel.setBackground(UiStyle.LIGHT_BG);
        addProductPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)),
                "Thêm sản phẩm", 0, 0, new Font("Segoe UI", Font.BOLD, 14), UiStyle.PRIMARY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        addProductPanel.add(new JLabel("Sản phẩm:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cboSanPham = new JComboBox<>();
        cboSanPham.setEditable(true);
        cboSanPham.setPreferredSize(new Dimension(0, 40));
        addProductPanel.add(cboSanPham, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        addProductPanel.add(new JLabel("Số lượng:"), gbc);

        gbc.gridx = 3;
        txtSoLuong = new JTextField("1");
        txtSoLuong.setHorizontalAlignment(SwingConstants.CENTER);
        txtSoLuong.setPreferredSize(new Dimension(80, 40));
        addProductPanel.add(txtSoLuong, gbc);

        left.add(addProductPanel, BorderLayout.NORTH);

        chiTietModel = new DefaultTableModel(new Object[]{"STT", "Hình ảnh", "Mã vạch", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền", "Thao tác"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(chiTietModel);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblChiTiet.setRowHeight(56);
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(chiTietModel);
        tblChiTiet.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(tblChiTiet);
        UiStyle.styleTable(tblChiTiet, scrollPane);
        tblChiTiet.getColumnModel().getColumn(1).setCellRenderer(new ImageRenderer());
        tblChiTiet.getColumnModel().getColumn(4).setCellRenderer(centerRenderer());
        tblChiTiet.getColumnModel().getColumn(5).setCellRenderer(centerRenderer());
        tblChiTiet.getColumnModel().getColumn(6).setCellRenderer(centerRenderer());

        TableUtility.addActionColumn(tblChiTiet, 7, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                selectRowOnForm(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                removeRow(modelRow);
            }
        });

        left.add(scrollPane, BorderLayout.CENTER);
        return left;
    }

    private JPanel createRightPanel() {
        // Wrapper dùng BorderLayout: content ở CENTER, nút Thanh toán ở SOUTH
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(UiStyle.LIGHT_BG);
        wrapper.setPreferredSize(new Dimension(430, 0));

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UiStyle.LIGHT_BG);
        right.setBorder(new EmptyBorder(14, 14, 8, 14));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(0, 0, 6, 0);
        int row = 0;

        gbc.gridy = row++;
        right.add(sectionTitle("Thông tin khách hàng"), gbc);
        gbc.gridy = row++;
        right.add(spacer(8), gbc);

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(UiStyle.LIGHT_BG);
        txtTimKhachHang = new JTextField();
        txtTimKhachHang.putClientProperty("JTextField.placeholderText", "Nhập mã khách hàng hoặc số điện thoại");
        btnTimKhachHang = new JButton(TableUtility.loadIcon("/icon/tim.png",30,30));
        UiStyle.styleButton(btnTimKhachHang, new Color(52, 152, 219));
        btnTimKhachHang.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnTimKhachHang.setPreferredSize(new Dimension(40, 30));
        btnTimKhachHang.setMaximumSize(btnTimKhachHang.getPreferredSize());
        searchRow.add(txtTimKhachHang, BorderLayout.CENTER);
        searchRow.add(btnTimKhachHang, BorderLayout.EAST);
        gbc.gridy = row++;
        right.add(searchRow, gbc);

        gbc.gridy = row++;
        right.add(spacer(10), gbc);
        lblTenKhachHang = createValueLabel();
        row = addInfoRow(right, gbc, row, "Tên khách hàng", lblTenKhachHang);

        JPanel pointsRow = new JPanel(new BorderLayout(8, 0));
        pointsRow.setOpaque(false);
        lblDiemKhachHang = createValueLabel();
        pointsRow.add(createInfoRow("Điểm hiện có", lblDiemKhachHang), BorderLayout.CENTER);
        btnDungDiem = new JButton("Dùng điểm");
        UiStyle.styleButton(btnDungDiem, new Color(59, 130, 246));
        btnDungDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDungDiem.setPreferredSize(new Dimension(90, 40));
        btnDungDiem.setMaximumSize(new Dimension(90, 40));
        btnDungDiem.setMinimumSize(new Dimension(90, 40));
        btnDungDiem.setVisible(false);
        pointsRow.add(btnDungDiem, BorderLayout.EAST);
        gbc.gridy = row++;
        right.add(pointsRow, gbc);

        gbc.gridy = row++;
        right.add(spacer(12), gbc);
        gbc.gridy = row++;
        right.add(sectionTitle("Ưu đãi"), gbc);
        gbc.gridy = row++;
        right.add(spacer(6), gbc);

        gbc.gridy = row++;
        right.add(new JLabel("Khuyến mãi:"), gbc);
        cboKhuyenMai = new JComboBox<>();
        cboKhuyenMai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        gbc.gridy = row++;
        right.add(cboKhuyenMai, gbc);

        gbc.gridy = row++;
        right.add(spacer(12), gbc);
        gbc.gridy = row++;
        right.add(sectionTitle("Thanh toán"), gbc);
        gbc.gridy = row++;
        right.add(spacer(8), gbc);

        lblTongCong = createValueLabel();
        row = addInfoRow(right, gbc, row, "Tạm tính", lblTongCong);
        lblGiamGia = createValueLabel();
        row = addInfoRow(right, gbc, row, "Giảm giá", lblGiamGia);
        lblSuDungDiem = createValueLabel();
        suDungDiemRow = createInfoRow("Sử dụng điểm", lblSuDungDiem);
        suDungDiemRow.setVisible(true);
        gbc.gridy = row++;
        right.add(suDungDiemRow, gbc);
        lblTongTien = createValueLabel();
        row = addInfoRow(right, gbc, row, "Tổng tiền", lblTongTien);

        JPanel modeRow = new JPanel(new GridLayout(1, 2, 10, 0));
        modeRow.setOpaque(false);
        modeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnTienMat = createPaymentModeButton("Tiền mặt");
        btnChuyenKhoan = createPaymentModeButton("Chuyển khoản");
        modeRow.add(btnTienMat);
        modeRow.add(btnChuyenKhoan);
        gbc.gridy = row++;
        right.add(modeRow, gbc);

        cashInputRow = new JPanel(new BorderLayout(0, 4));
        cashInputRow.setOpaque(false);
        cashInputRow.add(new JLabel("Tiền khách đưa"), BorderLayout.NORTH);
        txtTienKhachDua = new JTextField("0");
        cashInputRow.add(txtTienKhachDua, BorderLayout.CENTER);
        cashInputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        gbc.gridy = row++;
        right.add(cashInputRow, gbc);

        changeRow = new JPanel(new BorderLayout(0, 4));
        changeRow.setOpaque(false);
        changeRow.add(new JLabel("Tiền thối lại"), BorderLayout.NORTH);
        txtTienThoi = new JTextField("0");
        txtTienThoi.setEditable(false);
        txtTienThoi.setEnabled(false);
        changeRow.add(txtTienThoi, BorderLayout.CENTER);
        changeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        gbc.gridy = row++;
        right.add(changeRow, gbc);

        // Filler đẩy content lên trên
        JPanel fillerPanel = new JPanel();
        fillerPanel.setOpaque(false);
        gbc.gridy = row;
        gbc.weighty = 1.0;
        right.add(fillerPanel, gbc);

        // Nút Thanh toán cố định ở SOUTH
        btnThanhToan = new JButton("Thanh toán");
        UiStyle.styleButton(btnThanhToan, new Color(46, 125, 50));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnThanhToan.setPreferredSize(new Dimension(0, 46));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(UiStyle.LIGHT_BG);
        southPanel.setBorder(new EmptyBorder(8, 14, 14, 14));
        southPanel.add(btnThanhToan, BorderLayout.CENTER);

        wrapper.add(right, BorderLayout.CENTER);
        wrapper.add(southPanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JLabel sectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(UiStyle.PRIMARY);
        return label;
    }

    private int addInfoRow(JPanel parent, GridBagConstraints base, int row, String labelText, JComponent valueComponent) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridy = row;
        parent.add(createInfoRow(labelText, valueComponent), gbc);
        return row + 1;
    }

    private JPanel createInfoRow(String labelText, JComponent valueComponent) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(45, 55, 72));
        row.add(label, BorderLayout.WEST);

        if (valueComponent != null) {
            row.add(valueComponent, BorderLayout.CENTER);
        }
        return row;
    }

    private JLabel createValueLabel() {
        JLabel field = new JLabel(" ");

        field.setFont(new Font("Segoe UI", Font.BOLD, 16));

        field.setPreferredSize(new Dimension(240, 32));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return field;
    }

    private JButton createPaymentModeButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(150, 42));
        button.setMaximumSize(button.getPreferredSize());
        return button;
    }

    private JComponent spacer(int height) {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, height));
        return spacer;
    }

    public void loadProducts() {
        loadingProducts = true;
        cboSanPham.removeAllItems();
        List<SanPhamDto> products = sanPhamService.loadAll();
        for (SanPhamDto product : products) {
            cboSanPham.addItem(new ProductOption(product));
        }
        cboSanPham.setSelectedIndex(-1);
        if (cboSanPham.getEditor() != null) {
            cboSanPham.getEditor().setItem("");
        }
        loadingProducts = false;
    }

    private void loadPromotions() {
        cboKhuyenMai.removeAllItems();
        cboKhuyenMai.addItem(PromotionOption.none());

        LocalDate today = LocalDate.now();
        for (KhuyenMaiDto km : khuyenMaiService.loadAll()) {
            boolean activeFrom = km.getNgayBatDau() == null || !today.isBefore(km.getNgayBatDau());
            boolean activeTo = km.getNgayKetThuc() == null || !today.isAfter(km.getNgayKetThuc());
            if (activeFrom && activeTo) {
                cboKhuyenMai.addItem(new PromotionOption(km));
            }
        }
        cboKhuyenMai.setSelectedIndex(0);
    }

    private void taoHoaDonMoi() {
        if (daCoHoaDon) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Đang có hóa đơn chưa thanh toán. Bạn muốn tạo mới và hủy hóa đơn hiện tại?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        maHoaDonHienTai = hoaDonService.nextId();
        daCoHoaDon = true;
        cart.clear();
        khachHangHienTai = null;
        suDungDiem = false;
        diemDaSuDung = 0;
        paymentMode = PaymentMethod.TIENMAT;

        txtMaHoaDon.setText(maHoaDonHienTai);
        txtNhanVien.setText(tenNhanVienHienTai);
        txtNgayTao.setText(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        txtSoLuong.setText("1");
        txtTimKhachHang.setText("");
        lblTenKhachHang.setText("");
        lblDiemKhachHang.setText("");
        txtTienKhachDua.setText("0");
        txtTienThoi.setText(formatCurrency(0));

        cboKhuyenMai.setSelectedIndex(0);
        btnDungDiem.setText("Dùng điểm");
        if (suDungDiemRow != null) {
            suDungDiemRow.setVisible(true);
        }
        // enable UI controls now that an invoice exists
        setUiEnabled(true);

        refreshCartTable();
        updateTotals();
        updatePaymentModeUi();
        updateButtonState();
    }

    private ProductOption selectedProduct() {
        Object selected = cboSanPham.getSelectedItem();
        if (selected instanceof ProductOption option) {
            return option;
        }
        String typed = cboSanPham.getEditor().getItem() == null ? "" : cboSanPham.getEditor().getItem().toString().trim().toLowerCase(Locale.ROOT);
        if (typed.isEmpty()) {
            return null;
        }
        for (int i = 0; i < cboSanPham.getItemCount(); i++) {
            ProductOption option = cboSanPham.getItemAt(i);
            String keyword = (option.maSanPham + " " + option.tenSanPham).toLowerCase(Locale.ROOT);
            if (keyword.contains(typed)) {
                return option;
            }
        }
        return null;
    }

    private void addOrUpdateFromForm() {
        if (!daCoHoaDon) {
            return;
        }

        ProductOption option = selectedProduct();
        if (option == null) {
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (soLuong <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SanPhamDto latest = sanPhamService.findById(option.maSanPham);
        if (latest == null) {
            JOptionPane.showMessageDialog(this, "Sản phẩm không tồn tại.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedCode = getSelectedCartProductCode();
        CartItem item = cart.get(latest.getMaSanPham());
        int currentQty = item == null ? 0 : item.soLuong;

        int nextQty = selectedCode != null && selectedCode.equals(latest.getMaSanPham()) ? soLuong : currentQty + soLuong;
        if (nextQty > latest.getSoLuongHienCo()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng vượt quá tồn kho. Tồn hiện tại: " + latest.getSoLuongHienCo(),
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (item == null) {
            cart.put(latest.getMaSanPham(), new CartItem(latest, nextQty));
        } else {
            item.soLuong = nextQty;
            item.sanPham = latest;
        }

        txtSoLuong.setText("1");
        refreshCartTable();
        updateTotals();
        updateButtonState();
    }

    private void xoaSanPhamKhoiHoaDon() {
        int viewRow = tblChiTiet.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblChiTiet.convertRowIndexToModel(viewRow);
        removeRow(modelRow);
    }

    private void updateQuantityFromTable(int modelRow) {
        if (modelRow < 0 || modelRow >= chiTietModel.getRowCount()) {
            return;
        }
        String maSanPham = String.valueOf(chiTietModel.getValueAt(modelRow, 2));
        String value = String.valueOf(chiTietModel.getValueAt(modelRow, 4));
        int soLuong;
        try {
            soLuong = Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            refreshCartTable();
            return;
        }

        if (soLuong <= 0) {
            refreshCartTable();
            return;
        }

        CartItem item = cart.get(maSanPham);
        if (item == null) {
            refreshCartTable();
            return;
        }

        SanPhamDto latest = sanPhamService.findById(maSanPham);
        if (latest != null && soLuong > latest.getSoLuongHienCo()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng vượt quá tồn kho. Tồn hiện tại: " + latest.getSoLuongHienCo(),
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            refreshCartTable();
            return;
        }

        item.soLuong = soLuong;
        if (latest != null) {
            item.sanPham = latest;
        }
        updateTotals();
        updateButtonState();
    }

    private void selectRowOnForm(int modelRow) {
        if (modelRow < 0 || modelRow >= chiTietModel.getRowCount()) {
            return;
        }
        String maSanPham = String.valueOf(chiTietModel.getValueAt(modelRow, 2));
        String soLuong = String.valueOf(chiTietModel.getValueAt(modelRow, 4));
        syncingSelection = true;
        try {
            selectProduct(maSanPham);
            txtSoLuong.setText(soLuong);
        } finally {
            syncingSelection = false;
        }
    }

    private void selectProduct(String maSanPham) {
        for (int i = 0; i < cboSanPham.getItemCount(); i++) {
            ProductOption option = cboSanPham.getItemAt(i);
            if (option != null && Objects.equals(option.maSanPham, maSanPham)) {
                cboSanPham.setSelectedIndex(i);
                return;
            }
        }
    }

    private void removeRow(int modelRow) {
        if (modelRow < 0 || modelRow >= chiTietModel.getRowCount()) {
            return;
        }
        String maSanPham = String.valueOf(chiTietModel.getValueAt(modelRow, 2));
        cart.remove(maSanPham);
        refreshCartTable();
        updateTotals();
        updateButtonState();
    }

    private void lamRongHoaDon() {
        if (!daCoHoaDon || cart.isEmpty()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa toàn bộ sản phẩm khỏi hóa đơn hiện tại?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            cart.clear();
            refreshCartTable();
            updateTotals();
            updateButtonState();
        }
    }

    private void timKhachHang() {
        if (!daCoHoaDon) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String key = txtTimKhachHang.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã khách hàng hoặc số điện thoại để tìm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhachHangDto kh = khachHangService.findById(key);
        if (kh == null) {
            String normalized = key.replaceAll("\\s+", "");
            kh = khachHangService.loadAll().stream()
                    .filter(item -> Objects.equals(item.getSoDienThoai() == null ? "" : item.getSoDienThoai().replaceAll("\\s+", ""), normalized))
                    .findFirst()
                    .orElse(null);
        }

        if (kh == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            clearCustomer();
            btnDungDiem.setVisible(false);
            updateTotals();
            updateButtonState();
            return;
        }

        khachHangHienTai = kh;
        lblTenKhachHang.setText(kh.getTenKhachHang());
        lblDiemKhachHang.setText(String.valueOf(kh.getSoDiem()));
        btnDungDiem.setVisible(true);
        suDungDiem = false;
        diemDaSuDung = 0;
        btnDungDiem.setText("Dùng điểm");

        updateTotals();
        updateButtonState();
        revalidate();
        repaint();
    }

    private void clearCustomer() {
        khachHangHienTai = null;
        lblTenKhachHang.setText("");
        lblDiemKhachHang.setText("");
        btnDungDiem.setVisible(false);
        suDungDiem = false;
        diemDaSuDung = 0;
        btnDungDiem.setText("Dùng điểm");
        if (suDungDiemRow != null) {
            suDungDiemRow.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void toggleDungDiem() {
        if (!daCoHoaDon) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (khachHangHienTai == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trước khi dùng điểm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!suDungDiem && khachHangHienTai.getSoDiem() <= 0) {
            JOptionPane.showMessageDialog(this, "Khách hàng không có điểm để sử dụng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        suDungDiem = !suDungDiem;
        btnDungDiem.setText(suDungDiem ? "Bỏ điểm" : "Dùng điểm");
        if (suDungDiemRow != null) {
            suDungDiemRow.setVisible(true);
        }
        updateTotals();
        revalidate();
        repaint();
    }

    private void setPaymentMode(PaymentMethod mode) {
        paymentMode = mode;
        updatePaymentModeUi();
        updateTotals();
    }

    private void updatePaymentModeUi() {
        boolean cash = paymentMode == PaymentMethod.TIENMAT;
        cashInputRow.setVisible(cash);
        changeRow.setVisible(cash);
        stylePaymentModeButton(btnTienMat, cash);
        stylePaymentModeButton(btnChuyenKhoan, !cash);
        revalidate();
        repaint();
    }

    private void stylePaymentModeButton(JButton button, boolean active) {
        if (active) {
            button.setBackground(new Color(34, 197, 94));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(new Color(22, 163, 74), 1));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235), 1));
        }
    }

    private void updateTotals() {
        double tongCong = cart.values().stream()
                .mapToDouble(item -> item.soLuong * item.sanPham.getGiaBan())
                .sum();

        double giamGiaKhuyenMai = 0D;
        PromotionOption khuyenMai = (PromotionOption) cboKhuyenMai.getSelectedItem();
        if (khuyenMai != null && !khuyenMai.none) {
            giamGiaKhuyenMai = tongCong * (khuyenMai.giaTriPhanTram / 100.0);
        }

        double sauKhuyenMai = Math.max(0, tongCong - giamGiaKhuyenMai);

        double giamGiaDiem = 0D;
        diemDaSuDung = 0;
        if (suDungDiem && khachHangHienTai != null) {
            double maxGiamGiaDiem = khachHangHienTai.getSoDiem() * 100.0;
            giamGiaDiem = Math.min(sauKhuyenMai, maxGiamGiaDiem);
            diemDaSuDung = (int) Math.ceil(giamGiaDiem / 100.0);
            lblDiemKhachHang.setText(String.valueOf(Math.max(0, khachHangHienTai.getSoDiem() - diemDaSuDung)));
        } else if (khachHangHienTai != null) {
            lblDiemKhachHang.setText(String.valueOf(khachHangHienTai.getSoDiem()));
        }

        double tongTien = Math.max(0, sauKhuyenMai - giamGiaDiem);

        lblTongCong.setText(formatCurrency(tongCong));
        lblGiamGia.setText(giamGiaKhuyenMai > 0 ? "-" + formatCurrency(giamGiaKhuyenMai) : formatCurrency(0));
        lblSuDungDiem.setText(giamGiaDiem > 0 ? "-" + formatCurrency(giamGiaDiem) : formatCurrency(0));
        lblTongTien.setText(formatCurrency(tongTien));
        if (suDungDiemRow != null) {
            suDungDiemRow.setVisible(suDungDiem && khachHangHienTai != null);
        }

        updateCashChange();
    }

    private void updateCashChange() {
        updatingCashChange = true;
        try {
            if (paymentMode == PaymentMethod.CHUYENKHOAN) {
                txtTienKhachDua.setText("0");
                txtTienThoi.setText(formatCurrency(0));
                return;
            }

            double tongTien = parseCurrency(lblTongTien.getText());
            double tienKhachDua = parseCurrency(txtTienKhachDua.getText());
            double tienThoi = Math.max(0, tienKhachDua - tongTien);
            txtTienThoi.setText(formatCurrency(tienThoi));
        } finally {
            updatingCashChange = false;
        }
    }

    private void thanhToan() {
        if (!daCoHoaDon || maHoaDonHienTai.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước khi thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn chưa có sản phẩm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        updateTotals();

        double tongCong = parseCurrency(lblTongCong.getText());
        double tongTien = parseCurrency(lblTongTien.getText());
        double tienKhachDua = parseCurrency(txtTienKhachDua.getText());

        if (paymentMode == PaymentMethod.TIENMAT && tienKhachDua < tongTien) {
            JOptionPane.showMessageDialog(this, "Số tiền khách đưa không đủ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtTienKhachDua.requestFocus();
            return;
        }

        // ── Chuyển khoản: hiển thị QR SePay và đợi xác nhận ─────────────────
        if (paymentMode == PaymentMethod.CHUYENKHOAN) {
            boolean paid = showTransferPaymentDialog(tongTien, maHoaDonHienTai);
            if (!paid) {
                return;   // người dùng huỷ hoặc đóng cửa sổ
            }
        }

        for (CartItem item : cart.values()) {
            SanPhamDto latest = sanPhamService.findById(item.sanPham.getMaSanPham());
            if (latest == null || latest.getSoLuongHienCo() < item.soLuong) {
                JOptionPane.showMessageDialog(this,
                        "Sản phẩm " + item.sanPham.getTenSanPham() + " không đủ tồn kho để thanh toán.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                loadProducts();
                return;
            }
            item.sanPham = latest;
        }

        PromotionOption khuyenMai = (PromotionOption) cboKhuyenMai.getSelectedItem();
        double giamGiaKhuyenMai = 0D;
        if (khuyenMai != null && !khuyenMai.none) {
            giamGiaKhuyenMai = tongCong * (khuyenMai.giaTriPhanTram / 100.0);
        }
        double sauKhuyenMai = Math.max(0, tongCong - giamGiaKhuyenMai);
        double giamGiaDiem = suDungDiem && khachHangHienTai != null
                ? Math.min(sauKhuyenMai, khachHangHienTai.getSoDiem() * 100.0)
                : 0D;
        HoaDonDto hoaDon = HoaDonDto.builder()
                .maHoaDon(maHoaDonHienTai)
                .ngayLap(LocalDate.now())
                .maNhanVien(maNhanVienHienTai)
                .tenNhanVien(tenNhanVienHienTai)
                .maKhachHang(khachHangHienTai == null ? null : khachHangHienTai.getMaKhachHang())
                .tenKhachHang(khachHangHienTai == null ? null : khachHangHienTai.getTenKhachHang())
                .maKhuyenMai(khuyenMai == null || khuyenMai.none ? null : khuyenMai.maKhuyenMai)
                .tenKhuyenMai(khuyenMai == null || khuyenMai.none ? null : khuyenMai.tenKhuyenMai)
                .phuongThucThanhToan(paymentMode)
                .tongTien(tongTien)
                .build();

        HoaDonDto saved = hoaDonService.save(hoaDon);
        if (saved == null) {
            JOptionPane.showMessageDialog(this, "Không thể tạo hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            for (CartItem item : cart.values()) {
                chiTietHoaDonService.save(ChiTietHoaDonDto.builder()
                        .maHoaDon(maHoaDonHienTai)
                        .maSanPham(item.sanPham.getMaSanPham())
                        .soLuong(item.soLuong)
                        .donGia(item.sanPham.getGiaBan())
                        .build());

                item.sanPham.setSoLuongHienCo(item.sanPham.getSoLuongHienCo() - item.soLuong);
                sanPhamService.update(item.sanPham);
            }

            if (khachHangHienTai != null) {
                KhachHangDto latest = khachHangService.findById(khachHangHienTai.getMaKhachHang());
                if (latest != null) {
                    int diemDaCo = latest.getSoDiem();
                    int diemMoi = (int) Math.round(tongCong / 10000.0);
                    int tongDiem = Math.max(0, diemDaCo - (suDungDiem ? diemDaSuDung : 0)) + diemMoi;
                    // update points directly to avoid phone-unique validation during points-only update
                    khachHangService.updatePoints(latest.getMaKhachHang(), tongDiem);
                }
            }
        } catch (Exception ex) {
            hoaDonService.delete(maHoaDonHienTai);
            JOptionPane.showMessageDialog(this, "Thanh toán thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double tienThoi = paymentMode == PaymentMethod.TIENMAT ? Math.max(0, tienKhachDua - tongTien) : 0D;

        List<ChiTietHoaDonDto> previewDetails = new ArrayList<>();
        for (CartItem item : cart.values()) {
            previewDetails.add(ChiTietHoaDonDto.builder()
                    .maHoaDon(maHoaDonHienTai)
                    .maSanPham(item.sanPham.getMaSanPham())
                    .tenSanPham(item.sanPham.getTenSanPham())
                    .soLuong(item.soLuong)
                    .donGia(item.sanPham.getGiaBan())
                    .thanhTien(item.soLuong * item.sanPham.getGiaBan())
                    .build());
        }

        Gui_HoaDonPreview dialog = new Gui_HoaDonPreview(
                SwingUtilities.getWindowAncestor(this),
                hoaDon,
                previewDetails,
                Gui_HoaDonPreview.PaymentSummary.afterSale(
                        tongCong,
                        giamGiaKhuyenMai,
                        giamGiaDiem,
                        tongTien,
                        paymentMode == PaymentMethod.TIENMAT ? tienKhachDua : tongTien,
                        paymentMode == PaymentMethod.TIENMAT ? tienThoi : 0D
                )
        );
        dialog.setVisible(true);

        loadProducts();
        resetHoaDon();

        // try to refresh invoice list panel if present
        Window w = SwingUtilities.getWindowAncestor(this);
        Gui_HoaDon hoaDonPanel = findComponentRecursively(w, Gui_HoaDon.class);
        if (hoaDonPanel != null) {
            hoaDonPanel.refreshData();
        }
    }

    private <T> T findComponentRecursively(Component root, Class<T> cls) {
        if (root == null) return null;
        if (cls.isInstance(root)) return cls.cast(root);
        if (root instanceof Container) {
            for (Component c : ((Container) root).getComponents()) {
                T found = findComponentRecursively(c, cls);
                if (found != null) return found;
            }
        }
        return null;
    }

    // ── SePay QR payment dialog ──────────────────────────────────────────────────
    /**
     * Hiển thị dialog QR VietQR (BIDV) và poll SePay webhook mỗi 3 giây.
     *
     * @param tongTien   số tiền cần thanh toán (VNĐ)
     * @param maHoaDon   mã hóa đơn dùng làm nội dung chuyển khoản (ví dụ "HD001")
     * @return {@code true} nếu SePay báo PAID, {@code false} nếu hủy
     */
    private boolean showTransferPaymentDialog(double tongTien, String maHoaDon) {

        // Chuẩn hoá mã: chỉ giữ chữ + số, phải khớp regex [a-zA-Z]+\d+
        String payCode = maHoaDon.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (!payCode.matches("[A-Z]+\\d+")) {
            payCode = "HD" + System.currentTimeMillis() % 100000;
        }
        final String finalPayCode = payCode;

        long amountLong = Math.round(tongTien);
        String qrImageUrl = String.format(
                "https://qr.sepay.vn/img?bank=BIDV&acc=%s&template=compact&amount=%d&des=%s",
                BANK_ACCOUNT, amountLong, finalPayCode
        );
        // Thêm vào đầu method showTransferPaymentDialog(), sau khi có finalPayCode
        try {
            String registerBody = "{\"code\":\"" + finalPayCode + "\",\"expectedAmount\":" + amountLong + "}";
            HttpRequest registerReq = HttpRequest.newBuilder()
                    .uri(URI.create(SEPAY_WEBHOOK_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                    .build();
            HTTP_CLIENT.send(registerReq, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {}
        // ── Dialog ───────────────────────────────────────────────────────────
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Thanh toán chuyển khoản", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(420, 700);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout(0, 0));
        dialog.setResizable(true);

        // Header
        JPanel header = new JPanel();
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel headerLbl = new JLabel("Quét mã QR để thanh toán");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLbl.setForeground(Color.WHITE);
        header.add(headerLbl);
        dialog.add(header, BorderLayout.NORTH);

        // Center: QR + info
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(16, 24, 12, 24));

        // QR image
        JLabel qrLabel = new JLabel("Đang tải mã QR…", SwingConstants.CENTER);
        qrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrLabel.setPreferredSize(new Dimension(280, 280));
        qrLabel.setMaximumSize(new Dimension(280, 280));
        qrLabel.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)));
        center.add(qrLabel);
        center.add(Box.createVerticalStrut(14));

        // Bank info rows
        String[][] info = {
                {"Ngân hàng",         "BIDV"},
                {"Số tài khoản",      BANK_ACCOUNT},
                {"Chủ tài khoản",     ACCOUNT_NAME},
                {"Nội dung chuyển khoản", finalPayCode},
                {"Số tiền",           VND_FORMAT.format(amountLong) + " đ"}
        };
        for (String[] row : info) {
            JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
            rowPanel.setBackground(Color.WHITE);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel k = new JLabel(row[0] + ":");
            k.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            k.setForeground(new Color(80, 80, 80));
            k.setPreferredSize(new Dimension(180, 26));

            JLabel v = new JLabel(row[1]);
            v.setFont(new Font("Segoe UI", Font.BOLD, 13));
            v.setForeground(row[0].startsWith("Nội dung") ? new Color(59, 130, 246) : new Color(20, 20, 20));

            rowPanel.add(k, BorderLayout.WEST);
            rowPanel.add(v, BorderLayout.CENTER);
            center.add(rowPanel);
            center.add(Box.createVerticalStrut(4));
        }

        center.add(Box.createVerticalStrut(10));

        // Status label
        JLabel statusLbl = new JLabel("⏳  Đang chờ xác nhận thanh toán…", SwingConstants.CENTER);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLbl.setForeground(new Color(180, 120, 0));
        statusLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(statusLbl);

        dialog.add(center, BorderLayout.CENTER);

        // Footer: cancel button
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(0, 0, 10, 0));
        JButton cancelBtn = new JButton("Hủy thanh toán");
        UiStyle.styleButton(cancelBtn, new Color(180, 60, 60));
        cancelBtn.setPreferredSize(new Dimension(180, 40));
        footer.add(cancelBtn);
        dialog.add(footer, BorderLayout.SOUTH);

        // ── State ────────────────────────────────────────────────────────────
        AtomicBoolean paymentConfirmed = new AtomicBoolean(false);
        AtomicBoolean stopPolling      = new AtomicBoolean(false);

        // Cancel button / window close
        Runnable doCancel = () -> {
            stopPolling.set(true);
            dialog.dispose();
        };
        cancelBtn.addActionListener(e -> doCancel.run());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { doCancel.run(); }
        });

        // ── Load QR image in background ───────────────────────────────────────
        Thread qrLoader = new Thread(() -> {
            try {
                java.net.URL url = URI.create(qrImageUrl).toURL();
                Image img = javax.imageio.ImageIO.read(url);
                if (img != null) {
                    Image scaled = img.getScaledInstance(276, 276, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> {
                        qrLabel.setIcon(new ImageIcon(scaled));
                        qrLabel.setText("");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> qrLabel.setText("<html><center>Không tải được mã QR.<br>Vui lòng chuyển khoản thủ công.</center></html>"));
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> qrLabel.setText("<html><center>Lỗi tải QR.<br>Chuyển khoản thủ công rồi bấm xác nhận.</center></html>"));
            }
        }, "qr-loader");
        qrLoader.setDaemon(true);
        qrLoader.start();

        // ── Polling timer (every 3 s on EDT via Swing Timer) ─────────────────
        Timer[] timerRef = {null};
        timerRef[0] = new Timer(3000, e -> {
            if (stopPolling.get()) { timerRef[0].stop(); return; }
            // Run HTTP call off EDT
            Thread poller = new Thread(() -> {
                try {
                    String checkUrl = SEPAY_WEBHOOK_URL + "?code=" + finalPayCode;
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(checkUrl))
                            .GET()
                            .timeout(java.time.Duration.ofSeconds(8))
                            .build();
                    HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
                    String body = resp.body();
                    if (body != null && body.contains("\"status\":\"PAID\"")) {
                        stopPolling.set(true);
                        paymentConfirmed.set(true);
                        SwingUtilities.invokeLater(() -> {
                            timerRef[0].stop();
                            statusLbl.setText("✅  Thanh toán thành công!");
                            statusLbl.setForeground(new Color(46, 125, 50));
                            cancelBtn.setEnabled(false);
                            Timer closeTimer = new Timer(1200, ev -> dialog.dispose());
                            closeTimer.setRepeats(false);
                            closeTimer.start();
                        });
                    } else if (body != null && body.contains("\"status\":\"WRONG_AMOUNT\"")) {
                        stopPolling.set(true);
                        timerRef[0].stop();
                        // Parse số tiền thực tế từ JSON
                        long actualAmount = 0;
                        try {
                            int idx = body.indexOf("\"amount\":");
                            if (idx >= 0) {
                                String sub = body.substring(idx + 9).replaceAll("[^0-9].*", "");
                                actualAmount = Long.parseLong(sub);
                            }
                        } catch (Exception ignored) {}
                        final long finalActual = actualAmount;
                        SwingUtilities.invokeLater(() -> {
                            statusLbl.setText("⚠️  Số tiền không khớp!");
                            statusLbl.setForeground(new Color(180, 60, 0));
                            int choice = JOptionPane.showConfirmDialog(
                                    dialog,
                                    "Số tiền không khớp!\n" +
                                            "Đơn hàng:     " + VND_FORMAT.format(amountLong) + " đ\n" +
                                            "Khách chuyển: " + VND_FORMAT.format(finalActual) + " đ\n\n" +
                                            "Vẫn xác nhận thanh toán?",
                                    "Cảnh báo",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );
                            if (choice == JOptionPane.YES_OPTION) {
                                paymentConfirmed.set(true);
                            }
                            dialog.dispose();
                        });
                    }
                } catch (Exception ex) {
                    // Network hiccup — will retry next tick
                }
            }, "sepay-poller");
            poller.setDaemon(true);
            poller.start();
        });
        timerRef[0].setInitialDelay(2000);
        timerRef[0].start();

        // ── Show dialog (blocks until disposed) ──────────────────────────────
        dialog.setVisible(true);
        timerRef[0].stop();
        stopPolling.set(true);

        return paymentConfirmed.get();
    }

    private void refreshCartTable() {
        String selectedCode = getSelectedCartProductCode();
        refreshingTable = true;
        try {
            chiTietModel.setRowCount(0);
            int stt = 1;
            for (CartItem item : cart.values()) {
                double thanhTien = item.soLuong * item.sanPham.getGiaBan();
                chiTietModel.addRow(new Object[]{
                        stt++,
                        item.sanPham.getUrlHinhAnh(),
                        item.sanPham.getMaSanPham(),
                        item.sanPham.getTenSanPham(),
                        item.soLuong,
                        formatCurrency(item.sanPham.getGiaBan()),
                        formatCurrency(thanhTien),
                        ""
                });
            }
        } finally {
            refreshingTable = false;
        }

        if (selectedCode != null) {
            selectProductInTable(selectedCode);
        } else {
            tblChiTiet.clearSelection();
        }
    }

    private void resetHoaDon() {
        maHoaDonHienTai = "";
        daCoHoaDon = false;
        cart.clear();
        paymentMode = PaymentMethod.TIENMAT;
        clearCustomer();

        txtMaHoaDon.setText("");
        txtNhanVien.setText(tenNhanVienHienTai);
        txtNgayTao.setText("");
        txtSoLuong.setText("1");
        txtTimKhachHang.setText("");

        lblTongCong.setText(formatCurrency(0));
        lblGiamGia.setText(formatCurrency(0));
        lblSuDungDiem.setText(formatCurrency(0));
        lblTongTien.setText(formatCurrency(0));
        txtTienKhachDua.setText("0");
        txtTienThoi.setText(formatCurrency(0));
        if (suDungDiemRow != null) {
            suDungDiemRow.setVisible(true);
        }

        if (cboKhuyenMai.getItemCount() > 0) {
            cboKhuyenMai.setSelectedIndex(0);
        }

        refreshCartTable();
        updatePaymentModeUi();
        updateButtonState();
        // disable interactive controls until invoice is created
        setUiEnabled(false);
    }

    private void setUiEnabled(boolean enabled) {
        // btnTaoHoaDon should always stay enabled so user can create invoice
        boolean controls = enabled;
        cboSanPham.setEnabled(controls);
        txtSoLuong.setEnabled(controls);
        tblChiTiet.setEnabled(controls);
        btnXoaSanPham.setEnabled(controls && tblChiTiet.getSelectedRow() >= 0);
        btnLamRong.setEnabled(controls && !cart.isEmpty());
        btnTimKhachHang.setEnabled(controls);
        btnDungDiem.setEnabled(controls && khachHangHienTai != null && khachHangHienTai.getSoDiem() > 0);
        cboKhuyenMai.setEnabled(controls);
        btnTienMat.setEnabled(controls);
        btnChuyenKhoan.setEnabled(controls);
        txtTienKhachDua.setEnabled(controls && paymentMode == PaymentMethod.TIENMAT);
        btnThanhToan.setEnabled(controls && !cart.isEmpty());
        revalidate();
        repaint();
    }

    private void updateButtonState() {
        boolean canOperate = daCoHoaDon;
        btnXoaSanPham.setEnabled(canOperate && tblChiTiet.getSelectedRow() >= 0);
        btnLamRong.setEnabled(canOperate && !cart.isEmpty());
        btnTimKhachHang.setEnabled(canOperate);
        btnDungDiem.setEnabled(canOperate && khachHangHienTai != null && khachHangHienTai.getSoDiem() > 0);
        btnThanhToan.setEnabled(canOperate && !cart.isEmpty());
    }

    private void syncFormFromSelectedRow() {
        int viewRow = tblChiTiet.getSelectedRow();
        if (viewRow < 0) {
            return;
        }
        int modelRow = tblChiTiet.convertRowIndexToModel(viewRow);
        selectRowOnForm(modelRow);
    }

    private void selectProductInTable(String maSanPham) {
        for (int row = 0; row < chiTietModel.getRowCount(); row++) {
            if (Objects.equals(String.valueOf(chiTietModel.getValueAt(row, 2)), maSanPham)) {
                int viewRow = tblChiTiet.convertRowIndexToView(row);
                if (viewRow >= 0) {
                    tblChiTiet.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
    }

    private String getSelectedCartProductCode() {
        int viewRow = tblChiTiet.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblChiTiet.convertRowIndexToModel(viewRow);
        return String.valueOf(chiTietModel.getValueAt(modelRow, 2));
    }

    private String formatCurrency(double value) {
        return VND_FORMAT.format(Math.max(0, Math.round(value))) + " đ";
    }

    private double parseCurrency(String text) {
        if (text == null || text.isBlank()) {
            return 0D;
        }
        String numeric = text.replace("đ", "")
                .replace("Đ", "")
                .replace("-", "")
                .replace(".", "")
                .replace(",", "")
                .replaceAll("\\s+", "")
                .trim();
        if (numeric.isEmpty()) {
            return 0D;
        }
        try {
            return Double.parseDouble(numeric);
        } catch (NumberFormatException ex) {
            return 0D;
        }
    }
    private void formatTienKhachDua() {
        updatingCashChange = true;
        try {
            String raw = txtTienKhachDua.getText()
                    .replace(".", "").replace(",", "").replace("đ", "").replaceAll("\\s+", "").trim();
            if (raw.isEmpty()) {
                txtTienKhachDua.setText("0");
            } else {
                try {
                    long value = Long.parseLong(raw);
                    txtTienKhachDua.setText(VND_FORMAT.format(value));
                } catch (NumberFormatException ex) {
                    txtTienKhachDua.setText("0");
                }
            }
        } finally {
            updatingCashChange = false;
        }
        updateCashChange();
    }

    private static DefaultTableCellRenderer centerRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return component;
            }
        };
    }

    private static final class ImageRenderer extends DefaultTableCellRenderer {
        private static final java.util.concurrent.ConcurrentHashMap<String, ImageIcon> IMAGE_CACHE
                = new java.util.concurrent.ConcurrentHashMap<>();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : UiStyle.ROW_ODD));

            String path = value == null ? "" : String.valueOf(value);
            if (path.isBlank()) return label;

            if (path.startsWith("http://") || path.startsWith("https://")) {
                ImageIcon cached = IMAGE_CACHE.get(path);
                if (cached != null) {
                    // Đã có trong cache — hiện luôn
                    label.setIcon(cached);
                } else {
                    // Chưa có — hiện "..." và tải về
                    label.setText("...");
                    new Thread(() -> {
                        try {
                            Image downloaded = javax.imageio.ImageIO.read(URI.create(path).toURL());
                            if (downloaded != null) {
                                ImageIcon icon = new ImageIcon(downloaded.getScaledInstance(64, 48, Image.SCALE_SMOOTH));
                                IMAGE_CACHE.put(path, icon);
                                SwingUtilities.invokeLater(table::repaint);
                            }
                        } catch (Exception ignored) {}
                    }).start();
                }
            } else {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        Image img = javax.imageio.ImageIO.read(file);
                        if (img != null) {
                            label.setIcon(new ImageIcon(img.getScaledInstance(64, 48, Image.SCALE_SMOOTH)));
                        } else {
                            label.setText("Lỗi ảnh");
                        }
                    } else {
                        ImageIcon icon = TableUtility.loadIcon(path, 64, 48);
                        if (icon != null) {
                            label.setIcon(icon);
                        } else {
                            label.setText("Lỗi ảnh");
                        }
                    }
                } catch (Exception e) {
                    label.setText("Lỗi ảnh");
                }
            }

            return label;
        }
    }

    private static final class ProductOption {
        private final String maSanPham;
        private final String tenSanPham;
        private final SanPhamDto dto;

        private ProductOption(SanPhamDto dto) {
            this.dto = dto;
            this.maSanPham = dto.getMaSanPham();
            this.tenSanPham = dto.getTenSanPham();
        }

        @Override
        public String toString() {
            return maSanPham + " - " + tenSanPham + " (Tồn: " + dto.getSoLuongHienCo() + ")";
        }
    }

    private static final class PromotionOption {
        private final String maKhuyenMai;
        private final String tenKhuyenMai;
        private final double giaTriPhanTram;
        private final boolean none;

        private PromotionOption(KhuyenMaiDto dto) {
            this.maKhuyenMai = dto.getMaKhuyenMai();
            this.tenKhuyenMai = dto.getTenKhuyenMai();
            this.giaTriPhanTram = dto.getGiaTriKhuyenMai();
            this.none = false;
        }

        private PromotionOption(String tenKhuyenMai) {
            this.maKhuyenMai = null;
            this.tenKhuyenMai = tenKhuyenMai;
            this.giaTriPhanTram = 0D;
            this.none = true;
        }

        static PromotionOption none() {
            return new PromotionOption("Không áp dụng");
        }

        @Override
        public String toString() {
            if (none) {
                return tenKhuyenMai;
            }
            return maKhuyenMai + " - " + tenKhuyenMai + " (" + VND_FORMAT.format(giaTriPhanTram) + "%)";
        }
    }

    private static final class CartItem {
        private SanPhamDto sanPham;
        private int soLuong;

        private CartItem(SanPhamDto sanPham, int soLuong) {
            this.sanPham = sanPham;
            this.soLuong = soLuong;
        }
    }
}
