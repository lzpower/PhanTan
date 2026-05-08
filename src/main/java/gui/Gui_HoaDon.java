package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import dto.ChiTietHoaDonDto;
import dto.HoaDonDto;
import entity.PaymentMethod;
import service.ChiTietHoaDonService;
import service.HoaDonService;
import service.ServiceFactory;
import service.impl.ChiTietHoaDonServiceImpl;
import service.impl.HoaDonServiceImpl;
import util.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Gui_HoaDon extends JPanel {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final HoaDonService hoaDonService = ServiceFactory.get(HoaDonService.class, HoaDonServiceImpl::new);
    private final ChiTietHoaDonService chiTietHoaDonService = ServiceFactory.get(ChiTietHoaDonService.class, ChiTietHoaDonServiceImpl::new);

    private JTextField txtSearch;
    private DatePicker dpNgayLoc;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public Gui_HoaDon() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        refreshData();

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        dpNgayLoc.addDateChangeListener(e -> filter());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                showDetailForSelectedRow();
            }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("QUẢN LÝ HÓA ĐƠN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setBackground(UiStyle.PRIMARY);
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        searchPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(searchLabel.getFont().deriveFont(18f));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(420, 42));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập mã hóa đơn, khách hàng, nhân viên hoặc phương thức thanh toán...");

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchLabel);
        searchRow.add(txtSearch);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);

        JLabel dateLabel = new JLabel("Ngày:");
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(dateLabel.getFont().deriveFont(17f));

        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd/MM/yyyy");
        settings.setAllowKeyboardEditing(false);
        dpNgayLoc = new DatePicker(settings);
        UiStyle.styleDatePicker(dpNgayLoc);
        dpNgayLoc.setDate(null);

        filterRow.add(dateLabel);
        filterRow.add(dpNgayLoc);

        searchPanel.add(searchRow);
        searchPanel.add(filterRow);

        header.add(title);
        header.add(Box.createVerticalStrut(10));
        header.add(searchPanel);
        return header;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Mã HĐ", "Thời gian", "Số món", "Thanh toán", "Tổng tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
        sorter.sort();

        JScrollPane scrollPane = new JScrollPane(table);
        UiStyle.styleTable(table, scrollPane);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private void loadData(List<HoaDonDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (HoaDonDto item : items) {
            model.addRow(new Object[]{stt++, item.getMaHoaDon(), item.getNgayLap() != null ? item.getNgayLap().atStartOfDay().format(DATE_FORMATTER) : "", countItems(item.getMaHoaDon()), prettyPayment(item.getPhuongThucThanhToan()), formatCurrency(item.getTongTien())});
        }
    }

    private void filter() {
        String keyword = txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        LocalDate selectedDate = dpNgayLoc.getDate();
        List<HoaDonDto> items = new ArrayList<>(hoaDonService.loadAll());
        if (!keyword.isBlank()) {
            items.removeIf(item -> !(contains(item.getMaHoaDon(), keyword)
                    || contains(item.getMaNhanVien(), keyword)
                    || contains(item.getTenNhanVien(), keyword)
                    || contains(item.getMaKhachHang(), keyword)
                    || contains(item.getTenKhachHang(), keyword)
                    || contains(item.getMaKhuyenMai(), keyword)
                    || contains(item.getTenKhuyenMai(), keyword)
                    || contains(item.getPhuongThucThanhToan() == null ? null : item.getPhuongThucThanhToan().name(), keyword)
                    || contains(item.getPhuongThucThanhToan() == null ? null : item.getPhuongThucThanhToan().getDisplayName(), keyword)));
        }
        if (selectedDate != null) {
            items.removeIf(item -> item.getNgayLap() == null || !selectedDate.equals(item.getNgayLap()));
        }
        loadData(items);
    }

    public void refreshData() {
        filter();
        sorter.sort();
    }

    private void showDetailForSelectedRow() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String maHoaDon = String.valueOf(model.getValueAt(row, 1));
        HoaDonDto hoaDon = hoaDonService.findById(maHoaDon);
        if (hoaDon == null) {
            return;
        }
        List<ChiTietHoaDonDto> details = chiTietHoaDonService.loadByHoaDon(maHoaDon);
        HoaDonDetailDialogJpa dialog = new HoaDonDetailDialogJpa(SwingUtilities.getWindowAncestor(this), hoaDon, details);
        dialog.setVisible(true);
    }

    private int countItems(String maHoaDon) {
        return chiTietHoaDonService.loadByHoaDon(maHoaDon).stream().mapToInt(ChiTietHoaDonDto::getSoLuong).sum();
    }

    private String prettyPayment(PaymentMethod method) {
        if (method == null) {
            return PaymentMethod.TIENMAT.getDisplayName();
        }
        return method.getDisplayName();
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String formatCurrency(double value) {
        return String.format(Locale.ROOT, "%,.0f đ", Math.max(0, value)).replace(',', '.');
    }

    private static final class HoaDonDetailDialogJpa extends JDialog {

        private final DefaultTableModel detailModel;

        private HoaDonDetailDialogJpa(Window owner, HoaDonDto hoaDon, List<ChiTietHoaDonDto> details) {
            super(owner, "Chi tiết hóa đơn " + hoaDon.getMaHoaDon(), ModalityType.APPLICATION_MODAL);
            setSize(720, 520);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setBorder(new EmptyBorder(18, 18, 18, 18));
            content.setBackground(Color.WHITE);

            JPanel head = new JPanel(new GridLayout(0, 2, 12, 8));
            head.setOpaque(false);
            head.add(label("Mã hóa đơn", hoaDon.getMaHoaDon()));
            head.add(label("Thời gian", hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().toString() : ""));
            head.add(label("Nhân viên", hoaDon.getTenNhanVien()));
            head.add(label("Thanh toán", hoaDon.getPhuongThucThanhToan() == null ? PaymentMethod.TIENMAT.getDisplayName() : hoaDon.getPhuongThucThanhToan().getDisplayName()));
            content.add(head, BorderLayout.NORTH);

            detailModel = new DefaultTableModel(new Object[]{"STT", "Tên sản phẩm", "SL", "Đơn giá", "Thành tiền"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable detailTable = new JTable(detailModel);
            JScrollPane scrollPane = new JScrollPane(detailTable);
            UiStyle.styleTable(detailTable, scrollPane);
            content.add(scrollPane, BorderLayout.CENTER);

                JPanel footer = new JPanel(new BorderLayout(12, 0));
            footer.setOpaque(false);
                JButton btnInHoaDon = new JButton("In hóa đơn");
                UiStyle.styleButton(btnInHoaDon, new Color(46, 125, 50));
                btnInHoaDon.setPreferredSize(new Dimension(140, 40));
                btnInHoaDon.addActionListener(e -> {
                Gui_HoaDonPreview previewDialog = new Gui_HoaDonPreview(
                    getOwner(),
                    hoaDon,
                    details,
                    Gui_HoaDonPreview.PaymentSummary.reprint(
                        details.stream().mapToDouble(ChiTietHoaDonDto::getThanhTien).sum(),
                        hoaDon.getTongTien()
                    )
                );
                previewDialog.setVisible(true);
                });

                JLabel total = new JLabel("Tổng cộng: " + String.format(Locale.ROOT, "%,.0f đ", hoaDon.getTongTien()).replace(',', '.'));
            total.setFont(new Font("Segoe UI", Font.BOLD, 20));
            total.setForeground(UiStyle.PRIMARY);
                footer.add(btnInHoaDon, BorderLayout.WEST);
            footer.add(total, BorderLayout.EAST);
            content.add(footer, BorderLayout.SOUTH);

            setContentPane(content);
            load(details);
        }

        private void load(List<ChiTietHoaDonDto> details) {
            detailModel.setRowCount(0);
            int stt = 1;
            for (ChiTietHoaDonDto item : details) {
                detailModel.addRow(new Object[]{stt++, item.getTenSanPham(), item.getSoLuong(), String.format(Locale.ROOT, "%,.0f đ", item.getDonGia()).replace(',', '.'), String.format(Locale.ROOT, "%,.0f đ", item.getThanhTien()).replace(',', '.')});
            }
        }

        private JLabel label(String title, String value) {
            JLabel lbl = new JLabel("<html><b>" + title + ":</b> " + (value == null ? "" : value) + "</html>");
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            return lbl;
        }
    }
}