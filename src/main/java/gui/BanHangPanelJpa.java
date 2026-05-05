package gui;

import dto.ChiTietHoaDonDto;
import dto.HoaDonDto;
import dto.KhachHangDto;
import dto.KhuyenMaiDto;
import dto.NhanVienDto;
import dto.SanPhamDto;
import dto.TaiKhoanDto;
import service.ChiTietHoaDonService;
import service.HoaDonService;
import service.KhachHangService;
import service.KhuyenMaiService;
import service.NhanVienService;
import service.SanPhamService;
import service.TaiKhoanService;
import service.impl.ChiTietHoaDonServiceImpl;
import service.impl.HoaDonServiceImpl;
import service.impl.KhachHangServiceImpl;
import service.impl.KhuyenMaiServiceImpl;
import service.impl.NhanVienServiceImpl;
import service.impl.SanPhamServiceImpl;
import service.impl.TaiKhoanServiceImpl;
import util.UiStyle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class BanHangPanelJpa extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        VND_FORMAT = new DecimalFormat("#,##0", symbols);
        VND_FORMAT.setMaximumFractionDigits(0);
    }

    private final HoaDonService hoaDonService = new HoaDonServiceImpl();
    private final ChiTietHoaDonService chiTietHoaDonService = new ChiTietHoaDonServiceImpl();
    private final SanPhamService sanPhamService = new SanPhamServiceImpl();
    private final KhuyenMaiService khuyenMaiService = new KhuyenMaiServiceImpl();
    private final KhachHangService khachHangService = new KhachHangServiceImpl();
    private final TaiKhoanService taiKhoanService = new TaiKhoanServiceImpl();
    private final NhanVienService nhanVienService = new NhanVienServiceImpl();

    private final Map<String, CartItem> cart = new LinkedHashMap<>();

    private String maNhanVienHienTai = "";
    private String tenNhanVienHienTai = "";
    private String maHoaDonHienTai = "";
    private boolean daCoHoaDon = false;

    private KhachHangDto khachHangHienTai;
    private boolean suDungDiem = false;
    private int diemDaSuDung = 0;

    private JComboBox<ProductOption> cboSanPham;
    private JTextField txtSoLuong;
    private JTable tblChiTiet;
    private DefaultTableModel chiTietModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtMaHoaDon;
    private JTextField txtNhanVien;
    private JTextField txtNgayTao;

    private JTextField txtMaKhachHang;
    private JTextField txtTenKhachHang;
    private JTextField txtSoDienThoai;
    private JTextField txtDiem;
    private JComboBox<PromotionOption> cboKhuyenMai;

    private JTextField txtTongCong;
    private JTextField txtGiamGia;
    private JTextField txtSuDungDiem;
    private JTextField txtTongTien;
    private JTextField txtTienKhachDua;
    private JTextField txtTienThoi;

    private JButton btnTaoHoaDon;
    private JButton btnThemSanPham;
    private JButton btnXoaSanPham;
    private JButton btnLamRong;
    private JButton btnTimKhachHang;
    private JButton btnDungDiem;
    private JButton btnDung;
    private JButton btnThanhToan;

    public BanHangPanelJpa(String tenDangNhap) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        resolveEmployee(tenDangNhap);

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadProducts();
        loadPromotions();
        resetHoaDon();

        btnTaoHoaDon.addActionListener(e -> taoHoaDonMoi());
        btnThemSanPham.addActionListener(e -> themSanPhamVaoHoaDon());
        btnXoaSanPham.addActionListener(e -> xoaSanPhamKhoiHoaDon());
        btnLamRong.addActionListener(e -> lamRongHoaDon());
        btnTimKhachHang.addActionListener(e -> timKhachHang());
        btnDungDiem.addActionListener(e -> toggleDungDiem());
        btnThanhToan.addActionListener(e -> thanhToan());
        btnDung.addActionListener(e -> dungHoaDon());

        cboKhuyenMai.addActionListener(e -> updateTongTien());

        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                capNhatTienThoi();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                capNhatTienThoi();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                capNhatTienThoi();
            }
        });

        tblChiTiet.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnXoaSanPham.setEnabled(tblChiTiet.getSelectedRow() >= 0 && daCoHoaDon);
            }
        });
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

        JPanel addProductPanel = new JPanel(new GridBagLayout());
        addProductPanel.setBackground(UiStyle.LIGHT_BG);
        addProductPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(210, 220, 235)),
                "Thêm sản phẩm", 0, 0, new Font("Segoe UI", Font.BOLD, 14), UiStyle.PRIMARY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        addProductPanel.add(new JLabel("Sản phẩm:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        cboSanPham = new JComboBox<>();
        cboSanPham.setEditable(true);
        addProductPanel.add(cboSanPham, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        addProductPanel.add(new JLabel("Số lượng:"), gbc);

        gbc.gridx = 3;
        txtSoLuong = new JTextField("1");
        txtSoLuong.setHorizontalAlignment(SwingConstants.CENTER);
        txtSoLuong.setPreferredSize(new Dimension(80, 36));
        addProductPanel.add(txtSoLuong, gbc);

        gbc.gridx = 4;
        btnThemSanPham = new JButton("Thêm");
        UiStyle.styleButton(btnThemSanPham, new Color(16, 185, 129));
        btnThemSanPham.setPreferredSize(new Dimension(130, 42));
        addProductPanel.add(btnThemSanPham, gbc);

        left.add(addProductPanel, BorderLayout.NORTH);

        chiTietModel = new DefaultTableModel(new Object[]{"STT", "Mã SP", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblChiTiet = new JTable(chiTietModel);
        tblChiTiet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(chiTietModel);
        sorter.setRowFilter(RowFilter.regexFilter(".*"));
        tblChiTiet.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tblChiTiet);
        UiStyle.styleTable(tblChiTiet, scrollPane);
        left.add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setBackground(Color.WHITE);

        btnXoaSanPham = new JButton("Xóa SP");
        UiStyle.styleButton(btnXoaSanPham, new Color(234, 88, 12));
        btnXoaSanPham.setPreferredSize(new Dimension(140, 44));

        btnLamRong = new JButton("Làm rỗng");
        UiStyle.styleButton(btnLamRong, new Color(26, 107, 127));
        btnLamRong.setPreferredSize(new Dimension(150, 44));

        actions.add(btnXoaSanPham);
        actions.add(btnLamRong);

        left.add(actions, BorderLayout.SOUTH);
        return left;
    }

    private JPanel createRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(UiStyle.LIGHT_BG);
        right.setBorder(new EmptyBorder(14, 14, 14, 14));
        right.setPreferredSize(new Dimension(430, 0));

        right.add(sectionTitle("Thông tin khách hàng"));
        right.add(Box.createVerticalStrut(8));

        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setBackground(UiStyle.LIGHT_BG);
        txtMaKhachHang = new JTextField();
        txtMaKhachHang.putClientProperty("JTextField.placeholderText", "Nhập mã KH hoặc số điện thoại");
        btnTimKhachHang = new JButton("Tìm");
        UiStyle.styleButton(btnTimKhachHang, new Color(52, 152, 219));
        btnTimKhachHang.setPreferredSize(new Dimension(90, 42));
        searchRow.add(txtMaKhachHang, BorderLayout.CENTER);
        searchRow.add(btnTimKhachHang, BorderLayout.EAST);
        right.add(searchRow);

        right.add(Box.createVerticalStrut(8));
        txtTenKhachHang = addReadonlyField(right, "Tên khách hàng");
        txtSoDienThoai = addReadonlyField(right, "Số điện thoại");
        txtDiem = addReadonlyField(right, "Điểm hiện có");

        right.add(Box.createVerticalStrut(8));
        right.add(sectionTitle("Ưu đãi"));
        right.add(Box.createVerticalStrut(6));

        right.add(new JLabel("Khuyến mãi:"));
        cboKhuyenMai = new JComboBox<>();
        right.add(cboKhuyenMai);

        right.add(Box.createVerticalStrut(8));
        btnDungDiem = new JButton("Dùng điểm");
        UiStyle.styleButton(btnDungDiem, new Color(59, 130, 246));
        btnDungDiem.setPreferredSize(new Dimension(170, 42));
        right.add(btnDungDiem);

        right.add(Box.createVerticalStrut(12));
        right.add(sectionTitle("Thanh toán"));
        right.add(Box.createVerticalStrut(8));

        txtTongCong = addReadonlyField(right, "Tổng cộng");
        txtGiamGia = addReadonlyField(right, "Giảm giá KM");
        txtSuDungDiem = addReadonlyField(right, "Giảm từ điểm");
        txtTongTien = addReadonlyField(right, "Tổng tiền");

        right.add(new JLabel("Tiền khách đưa"));
        txtTienKhachDua = new JTextField("0");
        txtTienKhachDua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        right.add(txtTienKhachDua);

        txtTienThoi = addReadonlyField(right, "Tiền thối");

        right.add(Box.createVerticalStrut(14));

        JPanel paymentButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        paymentButtons.setOpaque(false);

        btnDung = new JButton("Dừng");
        UiStyle.styleButton(btnDung, new Color(148, 163, 184));
        btnDung.setPreferredSize(new Dimension(120, 44));

        btnThanhToan = new JButton("Thanh toán");
        UiStyle.styleButton(btnThanhToan, new Color(46, 125, 50));
        btnThanhToan.setPreferredSize(new Dimension(160, 44));

        paymentButtons.add(btnDung);
        paymentButtons.add(btnThanhToan);

        right.add(paymentButtons);
        right.add(Box.createVerticalGlue());
        return right;
    }

    private JLabel sectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(UiStyle.PRIMARY);
        return label;
    }

    private JTextField addReadonlyField(JPanel parent, String label) {
        parent.add(new JLabel(label));
        JTextField field = new JTextField();
        field.setEditable(false);
        field.setEnabled(false);
        field.setDisabledTextColor(UIManager.getColor("TextField.foreground"));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        parent.add(field);
        parent.add(Box.createVerticalStrut(6));
        return field;
    }

    private void loadProducts() {
        List<SanPhamDto> products = sanPhamService.loadAll();
        cboSanPham.removeAllItems();
        for (SanPhamDto product : products) {
            cboSanPham.addItem(new ProductOption(product));
        }
        if (cboSanPham.getItemCount() > 0) {
            cboSanPham.setSelectedIndex(0);
        }
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

        txtMaHoaDon.setText(maHoaDonHienTai);
        txtNhanVien.setText(tenNhanVienHienTai);
        txtNgayTao.setText(LocalDate.now().format(DATE_FORMATTER));

        txtSoLuong.setText("1");
        txtMaKhachHang.setText("");
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtDiem.setText("");
        txtTienKhachDua.setText("0");

        cboKhuyenMai.setSelectedIndex(0);
        btnDungDiem.setText("Dùng điểm");

        refreshCartTable();
        updateTongTien();
        updateButtonState();
    }

    private ProductOption selectedProduct() {
        Object selected = cboSanPham.getSelectedItem();
        if (selected instanceof ProductOption option) {
            return option;
        }
        String typed = selected == null ? "" : selected.toString().trim().toLowerCase(Locale.ROOT);
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

    private void themSanPhamVaoHoaDon() {
        if (!daCoHoaDon) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước khi thêm sản phẩm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProductOption option = selectedProduct();
        if (option == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm phù hợp.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtSoLuong.requestFocus();
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

        CartItem item = cart.get(latest.getMaSanPham());
        int soLuongHienTai = item == null ? 0 : item.soLuong;
        if (soLuongHienTai + soLuong > latest.getSoLuongHienCo()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng vượt quá tồn kho. Tồn hiện tại: " + latest.getSoLuongHienCo(),
                    "Thông báo",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (item == null) {
            cart.put(latest.getMaSanPham(), new CartItem(latest, soLuong));
        } else {
            item.soLuong += soLuong;
            item.sanPham = latest;
        }

        txtSoLuong.setText("1");
        refreshCartTable();
        updateTongTien();
        updateButtonState();
    }

    private void xoaSanPhamKhoiHoaDon() {
        int selectedRow = tblChiTiet.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tblChiTiet.convertRowIndexToModel(selectedRow);
        String maSanPham = String.valueOf(chiTietModel.getValueAt(modelRow, 1));
        cart.remove(maSanPham);
        refreshCartTable();
        updateTongTien();
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
            updateTongTien();
            updateButtonState();
        }
    }

    private void timKhachHang() {
        if (!daCoHoaDon) {
            JOptionPane.showMessageDialog(this, "Vui lòng tạo hóa đơn trước.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String key = txtMaKhachHang.getText().trim();
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
            khachHangHienTai = null;
            txtTenKhachHang.setText("");
            txtSoDienThoai.setText("");
            txtDiem.setText("");
            suDungDiem = false;
            diemDaSuDung = 0;
            btnDungDiem.setText("Dùng điểm");
            updateTongTien();
            return;
        }

        khachHangHienTai = kh;
        txtMaKhachHang.setText(kh.getMaKhachHang());
        txtTenKhachHang.setText(kh.getTenKhachHang());
        txtSoDienThoai.setText(kh.getSoDienThoai());
        txtDiem.setText(String.valueOf(kh.getSoDiem()));
        suDungDiem = false;
        diemDaSuDung = 0;
        btnDungDiem.setText("Dùng điểm");

        updateTongTien();
        updateButtonState();
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
        btnDungDiem.setText(suDungDiem ? "Bỏ dùng điểm" : "Dùng điểm");
        updateTongTien();
    }

    private void updateTongTien() {
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
            txtDiem.setText(String.valueOf(Math.max(0, khachHangHienTai.getSoDiem() - diemDaSuDung)));
        } else if (khachHangHienTai != null) {
            txtDiem.setText(String.valueOf(khachHangHienTai.getSoDiem()));
        }

        double tongTien = Math.max(0, sauKhuyenMai - giamGiaDiem);

        txtTongCong.setText(formatCurrency(tongCong));
        txtGiamGia.setText(giamGiaKhuyenMai > 0 ? "-" + formatCurrency(giamGiaKhuyenMai) : formatCurrency(0));
        txtSuDungDiem.setText(giamGiaDiem > 0 ? "-" + formatCurrency(giamGiaDiem) : formatCurrency(0));
        txtTongTien.setText(formatCurrency(tongTien));

        capNhatTienThoi();
    }

    private void capNhatTienThoi() {
        double tongTien = parseCurrency(txtTongTien.getText());
        double tienKhachDua = parseCurrency(txtTienKhachDua.getText());
        double tienThoi = tienKhachDua - tongTien;
        if (tienThoi < 0) {
            txtTienThoi.setText(formatCurrency(0));
            return;
        }
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

        updateTongTien();

        double tongCong = parseCurrency(txtTongCong.getText());
        double tongTien = parseCurrency(txtTongTien.getText());
        double tienKhachDua = parseCurrency(txtTienKhachDua.getText());

        if (tienKhachDua < tongTien) {
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

        HoaDonDto hoaDon = HoaDonDto.builder()
                .maHoaDon(maHoaDonHienTai)
                .ngayLap(LocalDate.now())
                .maNhanVien(maNhanVienHienTai)
                .maKhachHang(khachHangHienTai == null ? null : khachHangHienTai.getMaKhachHang())
                .maKhuyenMai(khuyenMai == null || khuyenMai.none ? null : khuyenMai.maKhuyenMai)
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

        double tienThoi = tienKhachDua - tongTien;
        JOptionPane.showMessageDialog(this,
                "Thanh toán thành công!\nMã hóa đơn: " + maHoaDonHienTai
                        + "\nTổng tiền: " + formatCurrency(tongTien)
                        + "\nTiền thối: " + formatCurrency(Math.max(0, tienThoi)),
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);

        loadProducts();
        resetHoaDon();
    }

    private void dungHoaDon() {
        if (!daCoHoaDon) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Dừng hóa đơn hiện tại? Dữ liệu chưa thanh toán sẽ bị hủy.",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            resetHoaDon();
        }
    }

    private void refreshCartTable() {
        chiTietModel.setRowCount(0);
        int stt = 1;
        for (CartItem item : cart.values()) {
            double thanhTien = item.soLuong * item.sanPham.getGiaBan();
            chiTietModel.addRow(new Object[]{
                    stt++,
                    item.sanPham.getMaSanPham(),
                    item.sanPham.getTenSanPham(),
                    item.soLuong,
                    formatCurrency(item.sanPham.getGiaBan()),
                    formatCurrency(thanhTien)
            });
        }
    }

    private void resetHoaDon() {
        maHoaDonHienTai = "";
        daCoHoaDon = false;
        cart.clear();
        khachHangHienTai = null;
        suDungDiem = false;
        diemDaSuDung = 0;

        txtMaHoaDon.setText("");
        txtNhanVien.setText(tenNhanVienHienTai);
        txtNgayTao.setText("");

        txtSoLuong.setText("1");
        txtMaKhachHang.setText("");
        txtTenKhachHang.setText("");
        txtSoDienThoai.setText("");
        txtDiem.setText("");

        txtTongCong.setText(formatCurrency(0));
        txtGiamGia.setText(formatCurrency(0));
        txtSuDungDiem.setText(formatCurrency(0));
        txtTongTien.setText(formatCurrency(0));
        txtTienKhachDua.setText("0");
        txtTienThoi.setText(formatCurrency(0));

        if (cboKhuyenMai.getItemCount() > 0) {
            cboKhuyenMai.setSelectedIndex(0);
        }
        btnDungDiem.setText("Dùng điểm");

        refreshCartTable();
        updateButtonState();
    }

    private void updateButtonState() {
        boolean canOperate = daCoHoaDon;
        btnThemSanPham.setEnabled(canOperate);
        btnLamRong.setEnabled(canOperate && !cart.isEmpty());
        btnXoaSanPham.setEnabled(canOperate && tblChiTiet.getSelectedRow() >= 0);
        btnTimKhachHang.setEnabled(canOperate);
        btnDungDiem.setEnabled(canOperate && khachHangHienTai != null && khachHangHienTai.getSoDiem() > 0);
        btnThanhToan.setEnabled(canOperate && !cart.isEmpty());
        btnDung.setEnabled(canOperate);
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
