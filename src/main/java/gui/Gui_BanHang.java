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
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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

public class Gui_BanHang extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        VND_FORMAT = new DecimalFormat("#,##0", symbols);
        VND_FORMAT.setMaximumFractionDigits(0);
    }

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
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateCashChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateCashChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateCashChange();
            }
        });

        chiTietModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (refreshingTable || e.getType() != TableModelEvent.UPDATE || e.getColumn() != 4) {
                    return;
                }
                updateQuantityFromTable(e.getFirstRow());
            }
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
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UiStyle.LIGHT_BG);
        right.setBorder(new EmptyBorder(14, 14, 14, 14));
        right.setPreferredSize(new Dimension(430, 0));

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
        btnDungDiem.setPreferredSize(new Dimension(70, 40));
        btnDungDiem.setMaximumSize(btnDungDiem.getPreferredSize());
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
        suDungDiemRow.setVisible(false);
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

        gbc.gridy = row++;
        right.add(spacer(14), gbc);

        JPanel paymentButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        paymentButtons.setOpaque(false);
        btnThanhToan = new JButton("Thanh toán");
        UiStyle.styleButton(btnThanhToan, new Color(46, 125, 50));
        btnThanhToan.setForeground(Color.WHITE);
        paymentButtons.add(btnThanhToan);
        gbc.gridy = row++;
        right.add(paymentButtons, gbc);

        JPanel fillerPanel = new JPanel();
        fillerPanel.setOpaque(false);
        gbc.gridy = row;
        gbc.weighty = 1.0;
        right.add(fillerPanel, gbc);

        return right;
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

    private void loadProducts() {
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
            suDungDiemRow.setVisible(false);
        }

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
            suDungDiemRow.setVisible(false);
        }
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
            suDungDiemRow.setVisible(suDungDiem && khachHangHienTai != null);
        }
        updateTotals();
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
        if (paymentMode == PaymentMethod.CHUYENKHOAN) {
            txtTienKhachDua.setText("0");
            txtTienThoi.setText(formatCurrency(0));
            return;
        }

        double tongTien = parseCurrency(lblTongTien.getText());
        double tienKhachDua = parseCurrency(txtTienKhachDua.getText());
        double tienThoi = Math.max(0, tienKhachDua - tongTien);
        txtTienThoi.setText(formatCurrency(tienThoi));
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
                    latest.setSoDiem(tongDiem);
                    khachHangService.update(latest);
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
                        paymentMode == PaymentMethod.TIENMAT ? tienKhachDua : null,
                        paymentMode == PaymentMethod.TIENMAT ? tienThoi : null
                )
        );
        dialog.setVisible(true);

        loadProducts();
        resetHoaDon();
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
            suDungDiemRow.setVisible(false);
        }

        if (cboKhuyenMai.getItemCount() > 0) {
            cboKhuyenMai.setSelectedIndex(0);
        }

        refreshCartTable();
        updatePaymentModeUi();
        updateButtonState();
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
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : UiStyle.ROW_ODD));
            String path = value == null ? "" : String.valueOf(value);
            if (!path.isBlank()) {
                ImageIcon icon = TableUtility.loadIcon(path, 64, 48);
                if (icon != null) {
                    label.setIcon(icon);
                } else {
                    label.setText(path);
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
