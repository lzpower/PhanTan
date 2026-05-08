package gui;

import dto.ChiTietPhieuNhapDto;
import dto.NhaCungCapDto;
import dto.PhieuNhapDto;
import dto.SanPhamDto;
import service.NhaCungCapService;
import service.PhieuNhapService;
import service.SanPhamService;
import service.ServiceFactory;
import service.impl.NhaCungCapServiceImpl;
import service.impl.PhieuNhapServiceImpl;
import service.impl.SanPhamServiceImpl;
import util.TableUtility;
import util.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Gui_PhieuNhap extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final PhieuNhapService phieuNhapService = ServiceFactory.get(PhieuNhapService.class, PhieuNhapServiceImpl::new);
    private final NhaCungCapService nhaCungCapService = ServiceFactory.get(NhaCungCapService.class, NhaCungCapServiceImpl::new);

    private JTextField txtSearch;
    private JButton btnCreate;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public Gui_PhieuNhap() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshData();

        btnCreate.addActionListener(e -> openCreateDialog());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PHIẾU NHẬP HÀNG");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Quản lý lịch sử nhập hàng và tồn kho");
        subtitle.setForeground(new Color(220, 235, 245));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subtitle);
        header.add(titleBox, BorderLayout.WEST);

        btnCreate = new JButton("Tạo Phiếu Nhập");
        UiStyle.styleButton(btnCreate, new Color(34, 197, 94));
        btnCreate.setPreferredSize(new Dimension(220, 50));
        header.add(btnCreate, BorderLayout.EAST);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchRow.setOpaque(false);
        searchRow.setBorder(new EmptyBorder(14, 0, 0, 0));

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(searchLabel.getFont().deriveFont(17f));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(360, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập mã phiếu, nhà cung cấp hoặc ghi chú...");

        searchRow.add(searchLabel);
        searchRow.add(txtSearch);

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BorderLayout());
        container.add(header, BorderLayout.NORTH);
        container.add(searchRow, BorderLayout.SOUTH);
        return container;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        model = new DefaultTableModel(new Object[]{"STT", "Mã phiếu", "Nhà cung cấp", "Số mặt hàng", "Tổng tiền", "Thời gian", "Thao tác"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
//        sorter.setSortKeys(List.of(new RowSorter.SortKey(5, SortOrder.ASCENDING)));
        sorter.sort();

        JScrollPane scrollPane = new JScrollPane(table);
        UiStyle.styleTable(table, scrollPane);
        TableUtility.addActionColumn(table, 6, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                showDetail(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                deleteReceipt(modelRow);
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    showDetail(table.convertRowIndexToModel(table.getSelectedRow()));
                }
            }
        });
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private void refreshData() {
        loadData(phieuNhapService.search(txtSearch.getText()));
        sorter.sort();
    }

    private void loadData(List<PhieuNhapDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (PhieuNhapDto item : items) {
            model.addRow(new Object[]{stt++, item.getMaPhieuNhap(), item.getTenNhaCungCap(), item.getSoMatHang(), formatCurrency(item.getTongTien()), formatDate(item.getNgayNhap()), ""});
        }
    }

    private void filter() {
        String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                for (int i = 0; i < entry.getValueCount(); i++) {
                    if (String.valueOf(entry.getValue(i)).toLowerCase(Locale.ROOT).contains(keyword)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void openCreateDialog() {
        CreateReceiptDialog dialog = new CreateReceiptDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
            // refresh product and sales panels if they exist
            Window w = SwingUtilities.getWindowAncestor(this);
            Gui_SanPham sanPhamPanel = findComponentRecursively(w, Gui_SanPham.class);
            if (sanPhamPanel != null) sanPhamPanel.refreshData();
            Gui_BanHang banHangPanel = findComponentRecursively(w, Gui_BanHang.class);
            if (banHangPanel != null) banHangPanel.loadProducts();
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

    private void showDetail(int modelRow) {
        String maPhieuNhap = String.valueOf(model.getValueAt(modelRow, 1));
        PhieuNhapDto phieuNhap = phieuNhapService.findById(maPhieuNhap);
        if (phieuNhap == null) {
            return;
        }
        List<ChiTietPhieuNhapDto> details = phieuNhapService.loadDetails(maPhieuNhap);
        ReceiptDetailDialog dialog = new ReceiptDetailDialog(SwingUtilities.getWindowAncestor(this), phieuNhap, details);
        dialog.setVisible(true);
    }

    private void deleteReceipt(int modelRow) {
        String maPhieuNhap = String.valueOf(model.getValueAt(modelRow, 1));
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa phiếu nhập này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            phieuNhapService.delete(maPhieuNhap);
            refreshData();
        }
    }

    private String formatCurrency(double value) {
        return String.format(Locale.ROOT, "%,.0f đ", Math.max(0, value)).replace(',', '.');
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private static final class ReceiptDetailDialog extends JDialog {

        private ReceiptDetailDialog(Window owner, PhieuNhapDto phieuNhap, List<ChiTietPhieuNhapDto> details) {
            super(owner, "Chi Tiết Phiếu Nhập " + phieuNhap.getMaPhieuNhap(), ModalityType.APPLICATION_MODAL);
            setSize(920, 620);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setBorder(new EmptyBorder(18, 18, 18, 18));
            content.setBackground(Color.WHITE);

            JPanel head = new JPanel(new GridLayout(0, 2, 12, 8));
            head.setOpaque(false);
            head.add(label("Nhà cung cấp", phieuNhap.getTenNhaCungCap()));
            head.add(label("Thời gian", phieuNhap.getNgayNhap() == null ? "" : phieuNhap.getNgayNhap().format(DATE_TIME_FORMATTER)));
            head.add(label("Ghi chú", phieuNhap.getGhiChu() == null || phieuNhap.getGhiChu().isBlank() ? "Không có" : phieuNhap.getGhiChu()));
            head.add(label("Tổng tiền", String.format(Locale.ROOT, "%,.0f đ", phieuNhap.getTongTien()).replace(',', '.')));
            content.add(head, BorderLayout.NORTH);

            DefaultTableModel detailModel = new DefaultTableModel(new Object[]{"STT", "Sản phẩm", "Số lượng", "Giá nhập", "Thành tiền"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable detailTable = new JTable(detailModel);
            JScrollPane scrollPane = new JScrollPane(detailTable);
            UiStyle.styleTable(detailTable, scrollPane);
            content.add(scrollPane, BorderLayout.CENTER);

            int stt = 1;
            for (ChiTietPhieuNhapDto item : details) {
                detailModel.addRow(new Object[]{stt++, item.getTenSanPham(), item.getSoLuong(), String.format(Locale.ROOT, "%,.0f đ", item.getGiaNhap()).replace(',', '.'), String.format(Locale.ROOT, "%,.0f đ", item.getThanhTien()).replace(',', '.')});
            }

            setContentPane(content);
        }

        private JLabel label(String title, String value) {
            JLabel lbl = new JLabel("<html><b>" + title + ":</b> " + (value == null ? "" : value) + "</html>");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            return lbl;
        }
    }

    private final class CreateReceiptDialog extends JDialog {
        private final SanPhamService sanPhamService = ServiceFactory.get(SanPhamService.class, SanPhamServiceImpl::new);
        private final DefaultTableModel detailModel;
        private final JLabel lblTongTien = new JLabel("0 đ");
        private boolean saved;
        private boolean syncingTotals;
        private JComboBox<NhaCungCapOption> cboSupplier;
        private JTextArea txtGhiChu;

        private CreateReceiptDialog(Window owner) {
            super(owner, "Tạo Phiếu Nhập", ModalityType.APPLICATION_MODAL);
            setSize(1080, 760);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setBorder(new EmptyBorder(18, 18, 18, 18));
            content.setBackground(Color.WHITE);

            JPanel top = new JPanel(new BorderLayout(12, 0));
            top.setOpaque(false);

            JPanel left = new JPanel(new GridBagLayout());
            left.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.insets = new Insets(5, 0, 5, 0);

            gbc.gridy = 0;
            left.add(labelText("Nhà cung cấp"), gbc);
            cboSupplier = new JComboBox<>();
            cboSupplier.addItem(new NhaCungCapOption(null));
            for (NhaCungCapDto dto : nhaCungCapService.loadAll()) {
                cboSupplier.addItem(new NhaCungCapOption(dto));
            }
            cboSupplier.setEditable(false);
            cboSupplier.setSelectedIndex(0);
            gbc.gridy = 1;
            left.add(cboSupplier, gbc);

            gbc.gridy = 2;
            left.add(labelText("Ghi chú"), gbc);
            txtGhiChu = new JTextArea(3, 20);
            txtGhiChu.setLineWrap(true);
            txtGhiChu.setWrapStyleWord(true);
            gbc.gridy = 3;
            gbc.weighty = 1;
            gbc.fill = GridBagConstraints.BOTH;
            left.add(new JScrollPane(txtGhiChu), gbc);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            JButton btnAddRow = new JButton("Thêm mặt hàng");
            UiStyle.styleButton(btnAddRow, new Color(59, 130, 246));
            btnAddRow.setPreferredSize(new Dimension(160, 44));
            right.add(btnAddRow);

            top.add(left, BorderLayout.CENTER);
            top.add(right, BorderLayout.EAST);
            content.add(top, BorderLayout.NORTH);

            detailModel = new DefaultTableModel(new Object[]{"Sản phẩm", "Số lượng", "Giá nhập", "Thành tiền", "Xóa"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column < 3;
                }
            };
            JTable detailTable = new JTable(detailModel);
            detailTable.setRowHeight(36);
            detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            detailTable.setFillsViewportHeight(true);
            JScrollPane scrollPane = new JScrollPane(detailTable);
            UiStyle.styleTable(detailTable, scrollPane);
            content.add(scrollPane, BorderLayout.CENTER);

            JComboBox<ProductOption> productCombo = new JComboBox<>();
            productCombo.setEditable(true);
            productCombo.addItem(new ProductOption(null));
            for (SanPhamDto dto : sanPhamService.loadAll()) {
                productCombo.addItem(new ProductOption(dto));
            }
            productCombo.setSelectedIndex(0);
            detailTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(productCombo));
            detailTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JTextField()));
            detailTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JTextField()));
            TableUtility.addActionColumn(detailTable, 4, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
                @Override
                public void onEdit(int modelRow) {
                    detailTable.editCellAt(modelRow, 0);
                }

                @Override
                public void onDelete(int modelRow) {
                    if (modelRow >= 0 && modelRow < detailModel.getRowCount()) {
                        detailModel.removeRow(modelRow);
                        updateTotals();
                    }
                }
            });

            detailModel.addTableModelListener(e -> updateTotals());
            btnAddRow.addActionListener(e -> addBlankRow());
//            thêmHàngTrống();

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTongTien.setForeground(UiStyle.PRIMARY);
            footer.add(lblTongTien, BorderLayout.EAST);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actions.setOpaque(false);
            JButton btnSave = new JButton("Xác nhận Nhập Hàng");
            UiStyle.styleButton(btnSave, new Color(34, 197, 94));
            btnSave.setPreferredSize(new Dimension(200,70));
            JButton btnCancel = new JButton("Hủy");
            UiStyle.styleButton(btnCancel, new Color(148, 163, 184));
            btnCancel.setPreferredSize(new Dimension(100,70));
            actions.add(btnSave);
            actions.add(btnCancel);

            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.add(footer, BorderLayout.NORTH);
            bottom.add(actions, BorderLayout.SOUTH);

            content.add(bottom, BorderLayout.SOUTH);

            setContentPane(content);
            updateTotals();

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> saveReceipt());
        }

        private void addBlankRow() {
            detailModel.addRow(new Object[]{new ProductOption(null), 1, 0D, 0D, ""});
            updateTotals();
        }

        private void updateTotals() {
            if (syncingTotals) {
                return;
            }
            syncingTotals = true;
            double tong = 0D;
            try {
                for (int row = 0; row < detailModel.getRowCount(); row++) {
                    Object productCell = detailModel.getValueAt(row, 0);
                    int soLuong = parseInt(detailModel.getValueAt(row, 1), 0);
                    double giaNhap = parseDouble(detailModel.getValueAt(row, 2), 0D);
                    if (productCell instanceof ProductOption option && option.dto != null && giaNhap <= 0D) {
                        giaNhap = option.dto.getGiaBan() > 0 ? option.dto.getGiaBan() : 0D;
                        detailModel.setValueAt(giaNhap, row, 2);
                    }
                    double thanhTien = Math.max(0D, soLuong) * Math.max(0D, giaNhap);
                    detailModel.setValueAt(thanhTien, row, 3);
                    tong += thanhTien;
                }
            } finally {
                syncingTotals = false;
            }
            lblTongTien.setText(String.format(Locale.ROOT, "Tổng tiền nhập: %,.0f đ", tong).replace(',', '.'));
        }

        private void saveReceipt() {
            try {
                NhaCungCapOption supplier = (NhaCungCapOption) cboSupplier.getSelectedItem();
                if (supplier == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                List<ChiTietPhieuNhapDto> details = new ArrayList<>();
                for (int row = 0; row < detailModel.getRowCount(); row++) {
                    Object productCell = detailModel.getValueAt(row, 0);
                    if (!(productCell instanceof ProductOption option)) {
                        continue;
                    }
                    int soLuong = parseInt(detailModel.getValueAt(row, 1), 0);
                    double giaNhap = parseDouble(detailModel.getValueAt(row, 2), 0D);
                    if (soLuong <= 0 || giaNhap <= 0) {
                        continue;
                    }
                    details.add(ChiTietPhieuNhapDto.builder()
                            .maSanPham(option.dto.getMaSanPham())
                            .tenSanPham(option.dto.getTenSanPham())
                            .soLuong(soLuong)
                            .giaNhap(giaNhap)
                            .build());
                }

                if (details.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Chưa có mặt hàng hợp lệ.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                PhieuNhapDto dto = PhieuNhapDto.builder()
                        .maPhieuNhap(phieuNhapService.nextId())
                        .ngayNhap(LocalDateTime.now())
                        .maNhaCungCap(supplier.dto.getMaNhaCungCap())
                        .ghiChu(txtGhiChu.getText().trim())
                        .build();

                phieuNhapService.save(dto, details);
                saved = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }

        private int parseInt(Object value, int defaultValue) {
            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (Exception ex) {
                return defaultValue;
            }
        }

        private double parseDouble(Object value, double defaultValue) {
            try {
                return Double.parseDouble(String.valueOf(value).trim());
            } catch (Exception ex) {
                return defaultValue;
            }
        }

        private JLabel labelText(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            return label;
        }

        private boolean isSaved() {
            return saved;
        }
    }

    private static final class NhaCungCapOption {
        private final NhaCungCapDto dto;

        private NhaCungCapOption(NhaCungCapDto dto) {
            this.dto = dto;
        }

        @Override
        public String toString() {
            if (dto == null) {
                return "Chọn nhà cung cấp";
            }
            return dto.getMaNhaCungCap() + " - " + dto.getTenNhaCungCap();
        }
    }

    private static final class ProductOption {
        private final SanPhamDto dto;

        private ProductOption(SanPhamDto dto) {
            this.dto = dto;
        }

        @Override
        public String toString() {
            if (dto == null) {
                return "Chọn sản phẩm";
            }
            return dto.getMaSanPham() + " - " + dto.getTenSanPham();
        }
    }
}