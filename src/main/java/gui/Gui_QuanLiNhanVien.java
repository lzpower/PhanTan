package gui;

import dto.ChucVuDto;
import dto.NhanVienDto;
import dto.TaiKhoanDto;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import service.ChucVuService;
import service.NhanVienService;
import service.ServiceFactory;
import service.impl.ChucVuServiceImpl;
import service.impl.NhanVienServiceImpl;
import util.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import util.TableUtility;

public class Gui_QuanLiNhanVien extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TEN_REGEX = "^[A-ZÀ-Ỹ][a-zà-ỹ]+(\\s[A-ZÀ-Ỹ][a-zà-ỹ]*)*$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9][A-Za-z0-9_.%-]+@[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}$";
    private static final String SO_DIEN_THOAI_REGEX = "^0[35789][0-9]{8}$";

    private final NhanVienService service = ServiceFactory.get(NhanVienService.class, NhanVienServiceImpl::new);
    private final ChucVuService chucVuService = ServiceFactory.get(ChucVuService.class, ChucVuServiceImpl::new);

    private JTextField txtSearch;
    private JTextField txtMa;
    private JTextField txtTen;
    private DatePicker dpNgaySinh;
    private JRadioButton rdoNam;
    private JRadioButton rdoNu;
    private JTextField txtEmail;
    private JTextField txtSoDienThoai;
    private JComboBox<ChucVuDto> cboChucVu;
    private JTextField txtTenTaiKhoan;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnReset;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public Gui_QuanLiNhanVien() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadPositions();
        loadData(service.loadAll());
        resetForm();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadPositions();
            }
        });

        btnAdd.addActionListener(e -> addEmployee());
        btnUpdate.addActionListener(e -> updateEmployee());
        btnReset.addActionListener(e -> resetForm());
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }
        });
        txtTen.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateUsernamePreview();
            }

            public void removeUpdate(DocumentEvent e) {
                updateUsernamePreview();
            }

            public void changedUpdate(DocumentEvent e) {
                updateUsernamePreview();
            }
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

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập mã, tên, SĐT, email, tài khoản...");

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

        JLabel title = new JLabel("Thông tin nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(title, gbc);

        int row = 1;
        panel.add(label("Mã nhân viên:"), gc(0, row++, 6));
        txtMa = new JTextField();
        txtMa.setEditable(false);
        txtMa.setEnabled(false);
        panel.add(txtMa, gc(0, row++, 12));

        panel.add(label("Tên nhân viên:"), gc(0, row++, 6));
        txtTen = new JTextField();
        panel.add(txtTen, gc(0, row++, 12));

        panel.add(label("Ngày sinh:"), gc(0, row++, 6));
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        dateSettings.setAllowKeyboardEditing(false);
        dpNgaySinh = new DatePicker(dateSettings);
        dpNgaySinh.getComponentDateTextField().setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dpNgaySinh.getComponentDateTextField().setBorder(UIManager.getBorder("TextField.border"));
        UiStyle.styleDatePicker(dpNgaySinh);

        panel.add(dpNgaySinh, gc(0, row++, 12));

        panel.add(label("Giới tính:"), gc(0, row++, 6));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        genderPanel.setBackground(UiStyle.LIGHT_BG);
        rdoNam = new JRadioButton("Nam");
        rdoNam.setBackground(UiStyle.LIGHT_BG);
        rdoNu = new JRadioButton("Nữ");
        rdoNu.setBackground(UiStyle.LIGHT_BG);
        ButtonGroup group = new ButtonGroup();
        group.add(rdoNam);
        group.add(rdoNu);
        genderPanel.add(rdoNam);
        genderPanel.add(rdoNu);
        panel.add(genderPanel, gc(0, row++, 12));

        panel.add(label("Email:"), gc(0, row++, 6));
        txtEmail = new JTextField();
        panel.add(txtEmail, gc(0, row++, 12));

        panel.add(label("Số điện thoại:"), gc(0, row++, 6));
        txtSoDienThoai = new JTextField();
        panel.add(txtSoDienThoai, gc(0, row++, 12));

        panel.add(label("Chức vụ:"), gc(0, row++, 6));
        cboChucVu = new JComboBox<>();
        panel.add(cboChucVu, gc(0, row++, 12));

        panel.add(label("Tên tài khoản:"), gc(0, row++, 6));
        txtTenTaiKhoan = new JTextField();
        txtTenTaiKhoan.setEditable(false);
        txtTenTaiKhoan.setEnabled(false);
        panel.add(txtTenTaiKhoan, gc(0, row++, 12));

        btnAdd = new JButton("Thêm");
        UiStyle.styleButton(btnAdd, new Color(16, 185, 129));
        btnUpdate = new JButton("Sửa");
        UiStyle.styleButton(btnUpdate, new Color(52, 152, 219));
        btnReset = new JButton("Làm mới");
        UiStyle.styleButton(btnReset, new Color(26, 107, 127));
        btnUpdate.setVisible(false);

        panel.add(btnAdd, gc(0, row++, 10));
        panel.add(btnUpdate, gc(0, row++, 10));
        panel.add(btnReset, gc(0, row, 10));
        gbc.weighty = 1.0;
        gbc.gridy = row + 1;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách nhân viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Mã", "Tên", "Ngày sinh", "Giới tính", "Email", "SĐT", "Chức vụ", "Tên tài khoản", "Thao tác"}, 0) {
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
        TableUtility.addActionColumn(table, 9, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                chonNhanVien(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                xoaNhanVien(modelRow);
            }
        });
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private GridBagConstraints gc(int x, int y, int bottom) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, bottom, 0);
        return gbc;
    }

    private void loadPositions() {
        cboChucVu.removeAllItems();
        for (ChucVuDto dto : chucVuService.loadAll()) {
            cboChucVu.addItem(dto);
        }
        if (cboChucVu.getItemCount() > 0) {
            cboChucVu.setSelectedIndex(0);
        }
    }

    private void loadData(List<NhanVienDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (NhanVienDto item : items) {
            model.addRow(new Object[]{
                    stt++,
                    item.getMaNhanVien(),
                    item.getTenNhanVien(),
                    item.getNgaySinh() != null ? item.getNgaySinh().format(DATE_FORMATTER) : "",
                    item.getGioiTinh(),
                    item.getEmail(),
                    item.getSoDienThoai(),
                    item.getTenChucVu(),
                    item.getTenTaiKhoan(),
                    ""
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
                    String value = String.valueOf(entry.getValue(i)).toLowerCase(Locale.ROOT);
                    if (value.contains(keyword)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void fillFormFromSelectedRow() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        chonNhanVien(row);
    }

    private void chonNhanVien(int row) {
        txtMa.setText(String.valueOf(model.getValueAt(row, 1)));
        txtTen.setText(String.valueOf(model.getValueAt(row, 2)));
        String ngaySinh = String.valueOf(model.getValueAt(row, 3));
        if (ngaySinh == null || ngaySinh.isBlank()) {
            dpNgaySinh.clear();
        } else {
            dpNgaySinh.setDate(LocalDate.parse(ngaySinh, DATE_FORMATTER));
        }
        String gender = String.valueOf(model.getValueAt(row, 4));
        rdoNam.setSelected("Nam".equalsIgnoreCase(gender));
        rdoNu.setSelected("Nữ".equalsIgnoreCase(gender));
        txtEmail.setText(String.valueOf(model.getValueAt(row, 5)));
        txtSoDienThoai.setText(String.valueOf(model.getValueAt(row, 6)));
        selectPosition(String.valueOf(model.getValueAt(row, 7)));
        txtTenTaiKhoan.setText(String.valueOf(model.getValueAt(row, 8)));
        showEditMode();
    }

    private void selectPosition(String tenChucVu) {
        for (int i = 0; i < cboChucVu.getItemCount(); i++) {
            ChucVuDto dto = cboChucVu.getItemAt(i);
            if (dto != null && dto.getTenChucVu() != null && dto.getTenChucVu().equalsIgnoreCase(tenChucVu)) {
                cboChucVu.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updateUsernamePreview() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        txtTenTaiKhoan.setText(service.buildUsername(ma.isEmpty() ? service.nextId() : ma, ten));
    }

    private void addEmployee() {
        if (!kiemTraDuLieu()) {
            return;
        }
        try {
            NhanVienDto nhanVienDto = collectForm();
            nhanVienDto.setMaNhanVien(service.nextId());
            nhanVienDto.setTenTaiKhoan(service.buildUsername(nhanVienDto.getMaNhanVien(), nhanVienDto.getTenNhanVien()));
            service.save(nhanVienDto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!\nTài khoản: " + nhanVienDto.getTenTaiKhoan() + "\nMật khẩu: 1", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void updateEmployee() {
        if (!kiemTraDuLieu()) {
            return;
        }
        try {
            if (txtMa.getText().isBlank()) {
                showError("Chọn một nhân viên để cập nhật.");
                return;
            }
            NhanVienDto dto = collectForm();
            dto.setMaNhanVien(txtMa.getText().trim());
            service.update(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void xoaNhanVien(int row) {
        String ma = String.valueOf(model.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa nhân viên này? Tài khoản mặc định sẽ bị xóa theo.", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(ma);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        }
    }

    private NhanVienDto collectForm() {
        NhanVienDto dto = new NhanVienDto();
        dto.setTenNhanVien(txtTen.getText().trim());
        dto.setNgaySinh(dpNgaySinh.getDate());
        dto.setGioiTinh(rdoNam.isSelected() ? "Nam" : "Nữ");
        dto.setEmail(txtEmail.getText().trim());
        dto.setSoDienThoai(txtSoDienThoai.getText().trim());
        ChucVuDto chucVu = (ChucVuDto) cboChucVu.getSelectedItem();
        dto.setMaChucVu(chucVu != null ? chucVu.getMaChucVu() : null);
        dto.setTenChucVu(chucVu != null ? chucVu.getTenChucVu() : null);
        dto.setTenTaiKhoan(txtTenTaiKhoan.getText().trim());
        return dto;
    }

    private boolean kiemTraDuLieu() {
        String tenNhanVien = txtTen.getText().trim();
        if (tenNhanVien.isEmpty()) {
            showError("Vui lòng nhập tên nhân viên.");
            txtTen.requestFocus();
            return false;
        }
        if (!tenNhanVien.matches(TEN_REGEX)) {
            showError("Tên chỉ được chứa chữ cái và viết hoa chữ cái đầu mỗi từ!");
            txtTen.requestFocus();
            return false;
        }

        LocalDate ngaySinh = dpNgaySinh.getDate();
        if (ngaySinh == null) {
            showError("Vui lòng chọn ngày sinh.");
            dpNgaySinh.getComponentDateTextField().requestFocus();
            return false;
        }
        if (ngaySinh.isAfter(LocalDate.now().minusYears(18))) {
            showError("Nhân viên phải từ 18 tuổi trở lên!");
            dpNgaySinh.getComponentDateTextField().requestFocus();
            return false;
        }

        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            showError("Vui lòng nhập email.");
            txtEmail.requestFocus();
            return false;
        }
        if (!email.matches(EMAIL_REGEX)) {
            showError("Email không hợp lệ!");
            txtEmail.requestFocus();
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

        if (cboChucVu.getSelectedItem() == null) {
            showError("Vui lòng chọn chức vụ.");
            cboChucVu.requestFocus();
            return false;
        }

        return true;
    }

    private void resetForm() {
        txtMa.setText(service.nextId());
        txtTen.setText("");
        dpNgaySinh.clear();
        rdoNam.setSelected(true);
        txtEmail.setText("");
        txtSoDienThoai.setText("");
        if (cboChucVu.getItemCount() > 0) {
            cboChucVu.setSelectedIndex(0);
        }
        updateUsernamePreview();
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}