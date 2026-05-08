package gui;

import dto.LoaiSanPhamDto;
import dto.SanPhamDto;
import service.LoaiSanPhamService;
import service.SanPhamService;
import service.ServiceFactory;
import service.impl.LoaiSanPhamServiceImpl;
import service.impl.SanPhamServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.swing.filechooser.FileNameExtensionFilter;
import util.TableUtility;
import util.UiStyle;

public class Gui_SanPham extends JPanel {

    private final SanPhamService service = ServiceFactory.get(SanPhamService.class, SanPhamServiceImpl::new);
    private final LoaiSanPhamService loaiSanPhamService = ServiceFactory.get(LoaiSanPhamService.class, LoaiSanPhamServiceImpl::new);

    private JTextField txtSearch;
    private JTextField txtMa;
    private JTextField txtTen;
    private JComboBox<LoaiSanPhamDto> cboLoai;
    private JTextField txtSoLuong;
    private JTextField txtGiaBan;
    private JTextField txtHinh;
    private JLabel lblHinhAnh;
    private JButton btnChonAnh;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnReset;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public Gui_SanPham() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadLoaiSanPham();
        loadData(service.loadAll());
        resetForm();

        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnReset.addActionListener(e -> resetForm());
        btnChonAnh.addActionListener(e -> chonHinhAnh());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                fillFormFromSelectedRow();
            }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("QUAN LY SAN PHAM");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setBackground(UiStyle.PRIMARY);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        searchPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel searchLabel = new JLabel("Tim kiem:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(searchLabel.getFont().deriveFont(18f));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(420, 42));

        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);

        header.add(title);
        header.add(Box.createVerticalStrut(10));
        header.add(searchPanel);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(390, 0));
        panel.setBackground(UiStyle.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel title = new JLabel("Thong tin san pham");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(title, gbc);

        int row = 1;
        panel.add(label("Ma san pham:"), gc(row++));
        txtMa = new JTextField();
        txtMa.setEditable(false);
        txtMa.setEnabled(false);
        panel.add(txtMa, gc(row++));

        panel.add(label("Ten san pham:"), gc(row++));
        txtTen = new JTextField();
        panel.add(txtTen, gc(row++));

        panel.add(label("Loai san pham:"), gc(row++));
        cboLoai = new JComboBox<>();
        panel.add(cboLoai, gc(row++));

        panel.add(label("So luong hien co:"), gc(row++));
        txtSoLuong = new JTextField();
        txtSoLuong.setEditable(false);
        txtSoLuong.setEnabled(false);
        panel.add(txtSoLuong, gc(row++));

        panel.add(label("Gia ban:"), gc(row++));
        txtGiaBan = new JTextField();
        panel.add(txtGiaBan, gc(row++));

        panel.add(label("URL hinh anh:"), gc(row++));
        txtHinh = new JTextField();
        txtHinh.setEditable(false);
        panel.add(txtHinh, gc(row++));

        btnChonAnh = UiStyle.createActionButton("Chọn ảnh", new Color(52, 152, 219), "/icon/tim.png");
        panel.add(btnChonAnh, gc(row++));

        lblHinhAnh = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblHinhAnh.setOpaque(true);
        lblHinhAnh.setBackground(Color.WHITE);
        lblHinhAnh.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblHinhAnh.setPreferredSize(new Dimension(0, 180));
        panel.add(lblHinhAnh, gc(row++));

        btnAdd = new JButton("Thêm");
        UiStyle.styleButton(btnAdd, new Color(16, 185, 129));
        btnUpdate = new JButton("Sửa");
        UiStyle.styleButton(btnUpdate, new Color(52, 152, 219));
        btnReset = new JButton("Làm mới");
        UiStyle.styleButton(btnReset, new Color(26, 107, 127));
        btnUpdate.setVisible(false);

        panel.add(btnAdd, gc(row++));
        panel.add(btnUpdate, gc(row++));
        panel.add(btnReset, gc(row));
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách sản phẩm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Hinh anh", "Ma", "Ten", "Loai", "So luong", "Gia ban", "Thao tac"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        UiStyle.styleTable(table, scrollPane);
        // Hiển thị ảnh preview trong cột ảnh của bảng
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageTableCellRenderer());

        TableUtility.addActionColumn(table, 7, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                chonSanPham(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                xoaSanPham(modelRow);
            }
        });
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private static class ImageTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                lbl.setBackground(table.getSelectionBackground());
            } else {
                lbl.setBackground(Color.WHITE);
            }
            String path = value == null ? "" : String.valueOf(value);
            if (path.isBlank()) {
                lbl.setText("");
                lbl.setIcon(null);
                return lbl;
            }
            ImageIcon icon = TableUtility.loadIcon(path, 80, 50);
            if (icon != null && icon.getIconWidth() > 0) {
                lbl.setIcon(icon);
                lbl.setText("");
            } else {
                lbl.setIcon(null);
                lbl.setText(new File(path).getName());
            }
            return lbl;
        }
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private GridBagConstraints gc(int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        return gbc;
    }

    private void loadLoaiSanPham() {
        cboLoai.removeAllItems();
        for (LoaiSanPhamDto dto : loaiSanPhamService.loadAll()) {
            cboLoai.addItem(dto);
        }
    }

    private void loadData(List<SanPhamDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (SanPhamDto item : items) {
            model.addRow(new Object[]{
                    stt++,
                    item.getUrlHinhAnh(),
                    item.getMaSanPham(),
                    item.getTenSanPham(),
                    item.getTenLoaiSanPham(),
                    item.getSoLuongHienCo(),
                    item.getGiaBan(),
                    ""
            });
        }
    }

    public void refreshData() {
        loadData(service.loadAll());
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

    private void fillFormFromSelectedRow() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        chonSanPham(row);
    }

    private void chonSanPham(int row) {
        txtMa.setText(String.valueOf(model.getValueAt(row, 2)));
        txtTen.setText(String.valueOf(model.getValueAt(row, 3)));
        selectLoai(String.valueOf(model.getValueAt(row, 4)));
        txtSoLuong.setText(String.valueOf(model.getValueAt(row, 5)));
        txtGiaBan.setText(String.valueOf(model.getValueAt(row, 6)));

        SanPhamDto dto = service.findById(txtMa.getText().trim());
        String imagePath = dto != null ? dto.getUrlHinhAnh() : null;
        txtHinh.setText(imagePath != null ? imagePath : "");
        updateImagePreview(imagePath);
        showEditMode();
    }

    private void selectLoai(String tenLoai) {
        for (int i = 0; i < cboLoai.getItemCount(); i++) {
            LoaiSanPhamDto dto = cboLoai.getItemAt(i);
            if (dto != null && dto.getTenLoaiSanPham() != null && dto.getTenLoaiSanPham().equalsIgnoreCase(tenLoai)) {
                cboLoai.setSelectedIndex(i);
                return;
            }
        }
    }

    private void addProduct() {
        try {
            SanPhamDto dto = collectForm();
            dto.setMaSanPham(service.nextId());
            service.save(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void updateProduct() {
        try {
            if (txtMa.getText().isBlank()) {
                showError("Chon mot san pham de cap nhat.");
                return;
            }
            SanPhamDto dto = collectForm();
            dto.setMaSanPham(txtMa.getText().trim());
            service.update(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void xoaSanPham(int row) {
        String ma = String.valueOf(model.getValueAt(row, 2));
        int confirm = JOptionPane.showConfirmDialog(this, "Xoa san pham nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(ma);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        }
    }

    private SanPhamDto collectForm() {
        LoaiSanPhamDto loai = (LoaiSanPhamDto) cboLoai.getSelectedItem();
        SanPhamDto dto = new SanPhamDto();
        dto.setTenSanPham(txtTen.getText().trim());
        dto.setMaLoaiSanPham(loai != null ? loai.getMaLoaiSanPham() : null);
        dto.setTenLoaiSanPham(loai != null ? loai.getTenLoaiSanPham() : null);
        dto.setSoLuongHienCo(Integer.parseInt(txtSoLuong.getText().trim()));
        dto.setGiaBan(Double.parseDouble(txtGiaBan.getText().trim()));
        dto.setUrlHinhAnh(txtHinh.getText().trim());
        return dto;
    }

    private void resetForm() {
        txtMa.setText(service.nextId());
        txtTen.setText("");
        if (cboLoai.getItemCount() > 0) {
            cboLoai.setSelectedIndex(0);
        }
        txtSoLuong.setText("0");
        txtGiaBan.setText("0");
        txtHinh.setText("");
        updateImagePreview(null);
        table.clearSelection();
        showCreateMode();
    }

    private void showCreateMode() {
        if (btnAdd != null) btnAdd.setVisible(true);
        if (btnUpdate != null) btnUpdate.setVisible(false);
    }

    private void showEditMode() {
        if (btnAdd != null) btnAdd.setVisible(false);
        if (btnUpdate != null) btnUpdate.setVisible(true);
    }

    private void chonHinhAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hinh anh", "png", "jpg", "jpeg", "gif", "bmp"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                txtHinh.setText(file.getAbsolutePath());
                updateImagePreview(file.getAbsolutePath());
            }
        }
    }

    private void updateImagePreview(String imagePath) {
        if (lblHinhAnh == null) {
            return;
        }
        if (imagePath == null || imagePath.isBlank()) {
            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText("Chưa có ảnh");
            return;
        }
        ImageIcon icon = TableUtility.loadIcon(imagePath, Math.max(220, lblHinhAnh.getWidth()), Math.max(160, lblHinhAnh.getHeight()));
        if (icon == null || icon.getIconWidth() <= 0) {
            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText("Không tải được ảnh");
            return;
        }
        lblHinhAnh.setText("");
        lblHinhAnh.setIcon(icon);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }
}
