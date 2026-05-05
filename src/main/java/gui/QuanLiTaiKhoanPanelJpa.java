package gui;

import dto.TaiKhoanDto;
import dto.ChucVuDto;
import service.ChucVuService;
import service.TaiKhoanService;
import service.impl.ChucVuServiceImpl;
import service.impl.TaiKhoanServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.Locale;
import util.UiStyle;

public class QuanLiTaiKhoanPanelJpa extends JPanel {

    private final TaiKhoanService service = new TaiKhoanServiceImpl();

    private JTextField txtSearch;
    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JComboBox<ChucVuDto> cboVaiTro;
    private final ChucVuService roleService = new ChucVuServiceImpl();
    private JTextField txtMaNhanVien;
    private JTextField txtTenNhanVien;
    private JTextField txtEmail;
    private JButton btnUpdate;
    private JButton btnResetPassword;
    private JButton btnClear;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public QuanLiTaiKhoanPanelJpa() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadRoles();
        loadData(service.loadAll());
        resetForm();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadRoles();
                loadData(service.search(txtSearch.getText()));
            }
        });

        btnUpdate.addActionListener(e -> updateAccount());
        btnResetPassword.addActionListener(e -> resetPassword());
        btnClear.addActionListener(e -> resetForm());
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

        JLabel title = new JLabel("QUẢN LÝ TÀI KHOẢN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setBackground(UiStyle.PRIMARY);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        searchPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(searchLabel.getFont().deriveFont(18f));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(420, 42));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập tài khoản, tên nhân viên, email hoặc chức vụ...");

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

        JLabel title = new JLabel("Thông tin tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(title, gbc);

        int row = 1;
        panel.add(label("Tên đăng nhập:"), gc(row++));
        txtTenDangNhap = new JTextField();
        txtTenDangNhap.setEnabled(false);
        txtTenDangNhap.setEditable(false);
        panel.add(txtTenDangNhap, gc(row++));

        panel.add(label("Mật khẩu:"), gc(row++));
        txtMatKhau = new JPasswordField();
        panel.add(txtMatKhau, gc(row++));

        panel.add(label("Vai trò (Chức vụ):"), gc(row++));
        cboVaiTro = new JComboBox<>();
        cboVaiTro.setEnabled(false);
        panel.add(cboVaiTro, gc(row++));

        panel.add(label("Mã nhân viên:"), gc(row++));
        txtMaNhanVien = new JTextField();
        txtMaNhanVien.setEnabled(false);
        txtMaNhanVien.setEditable(false);
        panel.add(txtMaNhanVien, gc(row++));

        panel.add(label("Tên nhân viên:"), gc(row++));
        txtTenNhanVien = new JTextField();
        txtTenNhanVien.setEnabled(false);
        txtTenNhanVien.setEditable(false);
        panel.add(txtTenNhanVien, gc(row++));

        panel.add(label("Email:"), gc(row++));
        txtEmail = new JTextField();
        txtEmail.setEnabled(false);
        txtEmail.setEditable(false);
        panel.add(txtEmail, gc(row++));

        btnUpdate = new JButton("Sửa");
        UiStyle.styleButton(btnUpdate, new Color(52, 152, 219));
        btnResetPassword = new JButton("Đặt lại mật khẩu");
        UiStyle.styleButton(btnResetPassword, new Color(46, 125, 50));
        btnClear = new JButton("Làm mới");
        UiStyle.styleButton(btnClear, new Color(26, 107, 127));
        btnUpdate.setVisible(false);

        panel.add(btnUpdate, gc(row++));
        panel.add(btnResetPassword, gc(row++));
        panel.add(btnClear, gc(row));
        gbc.weighty = 1.0;
        gbc.gridy = row + 1;
        panel.add(new JPanel(), gbc);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Tài khoản", "Vai trò", "Mã NV", "Tên NV", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        UiStyle.styleTable(table, scrollPane);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
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

    private void loadData(List<TaiKhoanDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (TaiKhoanDto item : items) {
            model.addRow(new Object[]{
                    stt++,
                    item.getTenDangNhap(),
                    item.getTenChucVu() == null ? "" : item.getTenChucVu(),
                    item.getMaNhanVien(),
                    item.getTenNhanVien(),
                    item.getEmail()
            });
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

    private void fillFormFromSelectedRow() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        chonTaiKhoan(row);
    }

    private void chonTaiKhoan(int row) {
        txtTenDangNhap.setText(String.valueOf(model.getValueAt(row, 1)));
        String roleDisplay = String.valueOf(model.getValueAt(row, 2));
        // select role in combobox by display name or id
        for (int i = 0; i < cboVaiTro.getItemCount(); i++) {
            ChucVuDto r = cboVaiTro.getItemAt(i);
            if (r != null && (roleDisplay.equals(r.getTenChucVu()) || roleDisplay.equals(r.getMaChucVu()))) {
                cboVaiTro.setSelectedIndex(i);
                break;
            }
        }
        txtMaNhanVien.setText(String.valueOf(model.getValueAt(row, 3)));
        txtTenNhanVien.setText(String.valueOf(model.getValueAt(row, 4)));
        txtEmail.setText(String.valueOf(model.getValueAt(row, 5)));
        showEditMode();
        TaiKhoanDto dto = service.findById(txtTenDangNhap.getText().trim());
        if (dto != null && dto.getMatKhau() != null) {
            txtMatKhau.setText(dto.getMatKhau());
        }
    }

    private void updateAccount() {
        if (txtTenDangNhap.getText().isBlank()) {
            showError("Chọn một tài khoản để cập nhật.");
            return;
        }
        TaiKhoanDto dto = new TaiKhoanDto();
        dto.setTenDangNhap(txtTenDangNhap.getText().trim());
        dto.setMatKhau(new String(txtMatKhau.getPassword()));
        ChucVuDto sel = (ChucVuDto) cboVaiTro.getSelectedItem();
        dto.setMaChucVu(sel == null ? "" : sel.getMaChucVu());
        dto.setMaNhanVien(txtMaNhanVien.getText().trim());
        service.update(dto);
        loadData(service.search(txtSearch.getText()));
        resetForm();
    }

    private void resetPassword() {
        if (txtTenDangNhap.getText().isBlank()) {
            showError("Chọn một tài khoản để đặt lại mật khẩu.");
            return;
        }
        service.resetPassword(txtTenDangNhap.getText().trim(), "1");
        txtMatKhau.setText("123456");
    }

    private void resetForm() {
        txtTenDangNhap.setText("");
        txtMatKhau.setText("");
        if (cboVaiTro.getItemCount() > 0) cboVaiTro.setSelectedIndex(0);
        txtMaNhanVien.setText("");
        txtTenNhanVien.setText("");
        txtEmail.setText("");
        table.clearSelection();
        showCreateMode();
    }

    private void showCreateMode() {
        if (btnUpdate != null) btnUpdate.setVisible(false);
    }

    private void showEditMode() {
        if (btnUpdate != null) btnUpdate.setVisible(true);
    }

    private void loadRoles() {
        cboVaiTro.removeAllItems();
        for (ChucVuDto r : roleService.loadAll()) {
            cboVaiTro.addItem(r);
        }
        if (cboVaiTro.getItemCount() > 0) {
            cboVaiTro.setSelectedIndex(0);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}