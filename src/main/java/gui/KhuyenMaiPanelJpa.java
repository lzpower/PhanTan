package gui;

import dto.KhuyenMaiDto;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import service.KhuyenMaiService;
import service.impl.KhuyenMaiServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import util.TableUtility;
import util.UiStyle;

public class KhuyenMaiPanelJpa extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final KhuyenMaiService service = new KhuyenMaiServiceImpl();

    private JTextField txtSearch;
    private JTextField txtMa;
    private JTextField txtTen;
    private JTextField txtGiaTri;
    private DatePicker dpNgayBatDau;
    private DatePicker dpNgayKetThuc;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnReset;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public KhuyenMaiPanelJpa() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadData(service.loadAll());
        resetForm();

        btnAdd.addActionListener(e -> addPromotion());
        btnUpdate.addActionListener(e -> updatePromotion());
        btnReset.addActionListener(e -> resetForm());
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

        JLabel title = new JLabel("QUẢN LÝ KHUYẾN MÃI");
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập mã hoặc tên khuyến mãi...");

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

        JLabel title = new JLabel("Thông tin khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(title, gbc);

        int row = 1;
        panel.add(label("Mã khuyến mãi:"), gc(row++));
        txtMa = new JTextField();
        txtMa.setEnabled(false);
        txtMa.setEditable(false);
        panel.add(txtMa, gc(row++));

        panel.add(label("Tên khuyến mãi:"), gc(row++));
        txtTen = new JTextField();
        panel.add(txtTen, gc(row++));

        panel.add(label("Giá trị khuyến mãi (%):"), gc(row++));
        txtGiaTri = new JTextField();
        panel.add(txtGiaTri, gc(row++));

        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        dateSettings.setAllowKeyboardEditing(false);

        panel.add(label("Ngày bắt đầu:"), gc(row++));
        dpNgayBatDau = new DatePicker(dateSettings);
        UiStyle.styleDatePicker(dpNgayBatDau);
        panel.add(dpNgayBatDau, gc(row++));

        DatePickerSettings endDateSettings = new DatePickerSettings();
        endDateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        endDateSettings.setAllowKeyboardEditing(false);
        panel.add(label("Ngày kết thúc:"), gc(row++));
        dpNgayKetThuc = new DatePicker(endDateSettings);
        UiStyle.styleDatePicker(dpNgayKetThuc);
        panel.add(dpNgayKetThuc, gc(row++));

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
        gbc.weighty = 1.0;
        gbc.gridy = row + 1;
        panel.add(new JPanel(), gbc);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Mã", "Tên", "Giá trị (%)", "Ngày bắt đầu", "Ngày kết thúc", "Thao tác"}, 0) {
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
        TableUtility.addActionColumn(table, 6, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                chonKhuyenMai(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                xoaKhuyenMai(modelRow);
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

    private GridBagConstraints gc(int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        return gbc;
    }

    private void loadData(List<KhuyenMaiDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (KhuyenMaiDto item : items) {
            model.addRow(new Object[]{
                    stt++,
                    item.getMaKhuyenMai(),
                    item.getTenKhuyenMai(),
                    item.getGiaTriKhuyenMai(),
                    item.getNgayBatDau() != null ? item.getNgayBatDau().format(DATE_FORMATTER) : "",
                    item.getNgayKetThuc() != null ? item.getNgayKetThuc().format(DATE_FORMATTER) : "",
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
        chonKhuyenMai(row);
    }

    private void chonKhuyenMai(int row) {
        txtMa.setText(String.valueOf(model.getValueAt(row, 1)));
        txtTen.setText(String.valueOf(model.getValueAt(row, 2)));
        txtGiaTri.setText(String.valueOf(model.getValueAt(row, 3)));
        String ngayBatDau = String.valueOf(model.getValueAt(row, 4));
        String ngayKetThuc = String.valueOf(model.getValueAt(row, 5));
        dpNgayBatDau.setDate(ngayBatDau == null || ngayBatDau.isBlank() ? null : LocalDate.parse(ngayBatDau, DATE_FORMATTER));
        dpNgayKetThuc.setDate(ngayKetThuc == null || ngayKetThuc.isBlank() ? null : LocalDate.parse(ngayKetThuc, DATE_FORMATTER));
        showEditMode();
    }

    private void addPromotion() {
        try {
            KhuyenMaiDto dto = collectForm();
            dto.setMaKhuyenMai(service.nextId());
            service.save(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void updatePromotion() {
        try {
            if (txtMa.getText().isBlank()) {
                showError("Chọn một khuyến mãi để cập nhật.");
                return;
            }
            KhuyenMaiDto dto = collectForm();
            dto.setMaKhuyenMai(txtMa.getText().trim());
            service.update(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void xoaKhuyenMai(int row) {
        String ma = String.valueOf(model.getValueAt(row, 1));
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa khuyến mãi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(ma);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        }
    }

    private KhuyenMaiDto collectForm() {
        KhuyenMaiDto dto = new KhuyenMaiDto();
        dto.setTenKhuyenMai(txtTen.getText().trim());
        dto.setGiaTriKhuyenMai(Double.parseDouble(txtGiaTri.getText().trim()));
        dto.setNgayBatDau(dpNgayBatDau.getDate());
        dto.setNgayKetThuc(dpNgayKetThuc.getDate());
        return dto;
    }

    private void resetForm() {
        txtMa.setText(service.nextId());
        txtTen.setText("");
        txtGiaTri.setText("");
        dpNgayBatDau.clear();
        dpNgayKetThuc.clear();
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