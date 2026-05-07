package gui;

import dto.KhachHangDto;
import service.KhachHangService;
import service.ServiceFactory;
import service.impl.KhachHangServiceImpl;

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
import util.TableUtility;
import util.UiStyle;

public class Gui_QuanLiKhachHang extends JPanel {

    private static final String TEN_REGEX = "^[A-ZÀ-Ỹ][a-zà-ỹ]+(\\s[A-ZÀ-Ỹ][a-zà-ỹ]*)*$";
    private static final String SO_DIEN_THOAI_REGEX = "^0[35789][0-9]{8}$";

    private final KhachHangService service = ServiceFactory.get(KhachHangService.class, KhachHangServiceImpl::new);

    private JTextField txtSearch;
    private JTextField txtMa;
    private JTextField txtTen;
    private JTextField txtSoDienThoai;
    private JTextField txtSoDiem;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnReset;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public Gui_QuanLiKhachHang() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData(service.loadAll());
        resetForm();

        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnReset.addActionListener(e -> resetForm());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                chonKhachHang();
            }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblTieuDe = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTieuDe.setForeground(Color.WHITE);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setBackground(UiStyle.PRIMARY);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        searchPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(searchLabel.getFont().deriveFont(18f));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(420, 45));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập mã, tên hoặc số điện thoại...");

        searchPanel.add(searchLabel);
        searchPanel.add(txtSearch);

        header.add(lblTieuDe);
        header.add(Box.createVerticalStrut(10));
        header.add(searchPanel);
        return header;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(380, 0));
        panel.setBackground(UiStyle.LIGHT_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel lblTieuDe = new JLabel("Thông tin khách hàng");
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTieuDe.setForeground(UiStyle.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(lblTieuDe, gbc);

        gbc.insets = new Insets(6, 0, 2, 0);
        gbc.gridy = 1;
        panel.add(new JLabel("Mã khách hàng:"), gbc);
        txtMa = new JTextField();
        txtMa.setEnabled(false);
        txtMa.setEditable(false);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(txtMa, gbc);

        gbc.insets = new Insets(6, 0, 2, 0);
        gbc.gridy = 3;
        panel.add(new JLabel("Họ tên khách hàng:"), gbc);
        txtTen = new JTextField();
        txtTen.putClientProperty("JTextField.placeholderText", "Ví dụ: Nguyễn Văn A");
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(txtTen, gbc);

        gbc.insets = new Insets(6, 0, 2, 0);
        gbc.gridy = 5;
        panel.add(new JLabel("Số điện thoại:"), gbc);
        txtSoDienThoai = new JTextField();
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(txtSoDienThoai, gbc);

        gbc.insets = new Insets(6, 0, 2, 0);
        gbc.gridy = 7;
        panel.add(new JLabel("Số điểm (chỉ xem):"), gbc);
        txtSoDiem = new JTextField();
        txtSoDiem.setEditable(false);
        txtSoDiem.setEnabled(false);
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(txtSoDiem, gbc);

        btnAdd = new JButton("Thêm");
        UiStyle.styleButton(btnAdd, new Color(16, 185, 129));
        btnUpdate = new JButton("Sửa");
        UiStyle.styleButton(btnUpdate, new Color(52, 152, 219));
        btnReset = new JButton("Làm mới");
        UiStyle.styleButton(btnReset, new Color(26, 107, 127));
        btnUpdate.setVisible(false);

        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridy = 9;
        panel.add(btnAdd, gbc);
        gbc.gridy = 10;
        panel.add(btnUpdate, gbc);
        gbc.gridy = 11;
        panel.add(btnReset, gbc);

        gbc.weighty = 1.0;
        gbc.gridy = 12;
        panel.add(new JPanel(), gbc);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Mã", "Họ tên", "SĐT", "Số điểm", "Thao tác"}, 0) {
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
        TableUtility.addActionColumn(table, 5, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                chonKhachHang(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                xoaKhachHang(modelRow);
            }
        });
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private void loadData(List<KhachHangDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (KhachHangDto item : items) {
            model.addRow(new Object[]{stt++, item.getMaKhachHang(), item.getTenKhachHang(), item.getSoDienThoai(), item.getSoDiem(), ""});
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
                    String value = String.valueOf(entry.getValue(i)).toLowerCase(Locale.ROOT);
                    if (value.contains(keyword)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void chonKhachHang() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        chonKhachHang(row);
    }

    private void chonKhachHang(int row) {
        txtMa.setText(String.valueOf(model.getValueAt(row, 1)));
        txtTen.setText(String.valueOf(model.getValueAt(row, 2)));
        txtSoDienThoai.setText(String.valueOf(model.getValueAt(row, 3)));
        txtSoDiem.setText(String.valueOf(model.getValueAt(row, 4)));
        showEditMode();
    }

    private void addCustomer() {
        if (!kiemTraDuLieu()) {
            return;
        }
        try {
            String ma = service.nextId();
            KhachHangDto dto = new KhachHangDto(ma, txtTen.getText().trim(), txtSoDienThoai.getText().trim(), 0);
            service.save(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void updateCustomer() {
        if (!kiemTraDuLieu()) {
            return;
        }
        try {
            if (txtMa.getText().isBlank()) {
                showError("Chọn một khách hàng để cập nhật.");
                return;
            }
            KhachHangDto dto = new KhachHangDto(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    txtSoDienThoai.getText().trim(),
                    parsePoints());
            service.update(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void xoaKhachHang(int row) {
        String ma = String.valueOf(model.getValueAt(row, 0));
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(ma);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        }
    }

    private void resetForm() {
        txtMa.setText(service.nextId());
        txtTen.setText("");
        txtSoDienThoai.setText("");
        txtSoDiem.setText("0");
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

    private int parsePoints() {
        try {
            return Integer.parseInt(txtSoDiem.getText().trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private boolean kiemTraDuLieu() {
        String tenKhachHang = txtTen.getText().trim();
        if (tenKhachHang.isEmpty()) {
            showError("Vui lòng nhập họ tên khách hàng.");
            txtTen.requestFocus();
            return false;
        }
        if (!tenKhachHang.matches(TEN_REGEX)) {
            showError("Họ tên phải viết hoa chữ cái đầu và chỉ chứa chữ cái!");
            txtTen.requestFocus();
            return false;
        }

        String soDienThoai = txtSoDienThoai.getText().trim();
        if (soDienThoai.isEmpty()) {
            showError("Vui lòng nhập số điện thoại.");
            txtSoDienThoai.requestFocus();
            return false;
        }
        if (!soDienThoai.matches(SO_DIEN_THOAI_REGEX)) {
            showError("Số điện thoại phải bắt đầu bằng 0, tiếp theo là 3|8|7|5|9 và đủ 10 chữ số.");
            txtSoDienThoai.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}