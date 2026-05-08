package gui;

import dto.ChiTietHoaDonDto;
import dto.HoaDonDto;
import entity.PaymentMethod;
import util.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Gui_HoaDonPreview extends JDialog {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm d 'thg' M, yyyy", new Locale("vi", "VN"));

    private final JPanel printablePanel;
    private final HoaDonDto hoaDon;
    private final PaymentSummary paymentSummary;

    public Gui_HoaDonPreview(Window owner, HoaDonDto hoaDon, List<ChiTietHoaDonDto> details, PaymentSummary paymentSummary) {
        super(owner, "Hóa đơn " + hoaDon.getMaHoaDon(), ModalityType.APPLICATION_MODAL);
        this.hoaDon = hoaDon;
        this.paymentSummary = paymentSummary;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(620, 780);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(241, 245, 249));

        printablePanel = buildPrintablePanel(details);
        JScrollPane scrollPane = new JScrollPane(printablePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(getContentPane().getBackground());
        add(scrollPane, BorderLayout.CENTER);

        // Footer nút đặt ngoài printablePanel để không bị in ra giấy
        JPanel footerWrapper = new JPanel(new BorderLayout());
        footerWrapper.setBackground(getContentPane().getBackground());
        footerWrapper.setBorder(new EmptyBorder(8, 18, 12, 18));
        footerWrapper.add(buildFooter(), BorderLayout.CENTER);
        add(footerWrapper, BorderLayout.SOUTH);
    }

    private JPanel buildPrintablePanel(List<ChiTietHoaDonDto> details) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(getContentPane().getBackground());
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        boolean successMode = paymentSummary.hasPaymentData();

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        addStacked(card, gbc, buildHeader(successMode), 0);
        addGap(card, gbc, 14);
        addStacked(card, gbc, buildInvoiceMeta(), 0);
        addGap(card, gbc, 14);
        addStacked(card, gbc, divider(), 0);
        addGap(card, gbc, 12);
        addStacked(card, gbc, sectionTitle(successMode ? "Chi tiết thanh toán" : "Chi tiết hóa đơn"), 0);
        addGap(card, gbc, 8);
        addStacked(card, gbc, buildItems(details), 1);
        addGap(card, gbc, 14);
        addStacked(card, gbc, divider(), 0);
        addGap(card, gbc, 12);
        addStacked(card, gbc, buildSummary(), 0);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private JComponent buildHeader(boolean successMode) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleBox.setOpaque(false);

        JLabel icon = new JLabel();
        if (successMode) {
            java.net.URL res = getClass().getResource("/icon/thanhtoanthanhcong.png");
            if (res != null) {
                icon.setIcon(new ImageIcon(res));
            } else {
                icon.setText("\u2714");
                icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
                icon.setForeground(new Color(16, 185, 129));
            }
        } else {
            icon.setText("\u2605");
            icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
            icon.setForeground(UiStyle.PRIMARY);
        }
        titleBox.add(icon);
        titleBox.add(Box.createHorizontalStrut(10));

        JLabel title = new JLabel(successMode ? "Thanh toán thành công" : "Xem trước hóa đơn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(successMode ? new Color(5, 150, 105) : new Color(15, 23, 42));
        titleBox.add(title);

        header.add(titleBox, BorderLayout.WEST);
        return header;
    }

    private JComponent buildInvoiceMeta() {
        JPanel meta = new JPanel(new BorderLayout(10, 0));
        meta.setOpaque(false);

        JLabel left = new JLabel("Mã HĐ: #" + hoaDon.getMaHoaDon());
        left.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        left.setForeground(new Color(51, 65, 85));

        String timeText = paymentSummary.hasPaymentData()
                ? LocalDateTime.now().format(DISPLAY_TIME)
                : (hoaDon.getNgayLap() == null ? "" : hoaDon.getNgayLap().toString());
        JLabel right = new JLabel(timeText);
        right.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        right.setForeground(new Color(100, 116, 139));

        meta.add(left, BorderLayout.WEST);
        meta.add(right, BorderLayout.EAST);
        return meta;
    }

    private JComponent buildItems(List<ChiTietHoaDonDto> details) {
        JPanel items = new JPanel(new GridBagLayout());
        items.setOpaque(false);
        items.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int row = 0;

        for (ChiTietHoaDonDto item : details) {
            gbc.gridy = row++;
            items.add(buildItemCard(item), gbc);
            gbc.gridy = row++;
            items.add(spacer(10), gbc);
        }
        if (details.isEmpty()) {
            JLabel empty = new JLabel("Không có chi tiết hóa đơn.");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(new Color(100, 116, 139));
            gbc.gridy = row;
            items.add(empty, gbc);
        }
        return items;
    }

    private JComponent buildItemCard(ChiTietHoaDonDto item) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel name = new JLabel(item.getTenSanPham());
        name.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        name.setForeground(new Color(15, 23, 42));

        JLabel qty = new JLabel(item.getSoLuong() + " x " + formatCurrency(item.getDonGia()));
        qty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        qty.setForeground(new Color(100, 116, 139));

        JLabel amount = new JLabel(formatCurrency(item.getThanhTien()));
        amount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        amount.setForeground(new Color(15, 23, 42));

        JPanel left = new JPanel(new GridLayout(0, 1, 0, 2));
        left.setOpaque(false);
        left.add(name);
        left.add(qty);

        card.add(left, BorderLayout.CENTER);
        card.add(amount, BorderLayout.EAST);
        return card;
    }

    private JComponent buildSummary() {
        JPanel summary = new JPanel(new GridBagLayout());
        summary.setOpaque(false);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int row = 0;

        gbc.gridy = row++;
        summary.add(summaryRow("Tổng cộng", formatCurrency(paymentSummary.tongTien), true), gbc);
        if (paymentSummary.giamGiaKhuyenMai != null) {
            gbc.gridy = row++;
            summary.add(summaryRow("Giảm giá", "-" + formatCurrency(paymentSummary.giamGiaKhuyenMai), false), gbc);
        }
        if (paymentSummary.giamGiaDiem != null) {
            gbc.gridy = row++;
            summary.add(summaryRow("Sử dụng điểm", "-" + formatCurrency(paymentSummary.giamGiaDiem), false), gbc);
        }
        if (paymentSummary.tienKhachDua != null) {
            gbc.gridy = row++;
            summary.add(summaryRow("Khách đưa", formatCurrency(paymentSummary.tienKhachDua), false), gbc);
        }
        if (paymentSummary.tienThoi != null) {
            gbc.gridy = row++;
            summary.add(summaryRow("Tiền thối", formatCurrency(paymentSummary.tienThoi), false), gbc);
        }
        gbc.gridy = row;
        summary.add(summaryRow("Thanh toán", hoaDon.getPhuongThucThanhToan() == null ? PaymentMethod.TIENMAT.getDisplayName() : hoaDon.getPhuongThucThanhToan().getDisplayName(), false), gbc);
        return summary;
    }

    private JComponent summaryRow(String label, String value, boolean emphasized) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel left = new JLabel(label);
        left.setFont(new Font("Segoe UI", emphasized ? Font.BOLD : Font.PLAIN, emphasized ? 17 : 14));
        left.setForeground(emphasized ? new Color(15, 23, 42) : new Color(71, 85, 105));

        JLabel right = new JLabel(value);
        right.setFont(new Font("Segoe UI", emphasized ? Font.BOLD : Font.PLAIN, emphasized ? 17 : 14));
        right.setForeground(emphasized ? new Color(16, 185, 129) : new Color(71, 85, 105));

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnPrint = new JButton("In hóa đơn");
        UiStyle.styleButton(btnPrint, new Color(34, 197, 94));
        btnPrint.setPreferredSize(new Dimension(160, 40));
        btnPrint.addActionListener(e -> printReceipt());

        JButton btnPrimary = new JButton(paymentSummary.hasPaymentData() ? "Đơn mới" : "Đóng");
        UiStyle.styleButton(btnPrimary, paymentSummary.hasPaymentData() ? new Color(16, 185, 129) : new Color(148, 163, 184));
        btnPrimary.setPreferredSize(new Dimension(160, 40));
        btnPrimary.addActionListener(e -> dispose());

        if (paymentSummary.hasPaymentData()) {
            footer.add(btnPrint);
            footer.add(btnPrimary);
        } else {
            footer.add(btnPrimary);
            footer.add(btnPrint);
        }
        return footer;
    }

    private void addStacked(JPanel panel, GridBagConstraints base, JComponent component, double weighty) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.weighty = weighty;
        panel.add(component, gbc);
        base.gridy++;
    }

    private void addGap(JPanel panel, GridBagConstraints base, int height) {
        addStacked(panel, base, spacer(height), 0);
    }

    private JComponent spacer(int height) {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(0, height));
        return spacer;
    }

    private JComponent divider() {
        JPanel line = new JPanel();
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));
        line.setBackground(new Color(226, 232, 240));
        return line;
    }

    private void printReceipt() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("HoaDon-" + hoaDon.getMaHoaDon());
            PageFormat pageFormat = job.defaultPage();

            // Xác định chiều rộng panel in: ưu tiên chiều rộng imageable của trang,
            // nhưng đổi sang pixel màn hình (72 point/inch → screen DPI)
            int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
            // pageFormat width là point (1/72 inch) → đổi sang pixel màn hình
            int panelWidth = (int) (pageFormat.getImageableWidth() / 72.0 * screenDpi);
            if (panelWidth <= 0) panelWidth = 520;

            // Build panel in riêng biệt hoàn toàn độc lập với ScrollPane
            // để tránh bị clip theo viewport
            final JPanel forPrint = buildPrintablePanel_forPrint(panelWidth);

            job.setPrintable((graphics, pf, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) graphics.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.translate(pf.getImageableX(), pf.getImageableY());
                    Dimension size = forPrint.getSize();
                    double scaleX = pf.getImageableWidth() / size.width;
                    double scaleY = pf.getImageableHeight() / size.height;
                    double scale = Math.min(scaleX, scaleY);
                    if (Double.isNaN(scale) || Double.isInfinite(scale) || scale <= 0) scale = 1.0;
                    g2.scale(scale, scale);
                    forPrint.printAll(g2);
                } finally {
                    g2.dispose();
                }
                return Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Không thể in hóa đơn: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Build panel dành riêng cho việc in — không nằm trong ScrollPane,
     * được layout hoàn chỉnh với chiều rộng cố định trước khi render.
     */
    private JPanel buildPrintablePanel_forPrint(int width) {
        // Lấy details từ printablePanel không khả thi trực tiếp,
        // nên dùng lại printablePanel (đã build sẵn), chỉ force layout đúng width.
        // Wrap nó vào một container độc lập để tránh bị JScrollPane clip.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(printablePanel.getBackground());
        // Clone panel bằng cách đặt width cố định rồi validate
        printablePanel.setPreferredSize(null); // reset về preferred tự nhiên
        // Force layout với đúng width
        printablePanel.setSize(width, Short.MAX_VALUE);
        printablePanel.validate();
        Dimension pref = printablePanel.getPreferredSize();
        printablePanel.setSize(width, pref.height > 0 ? pref.height : 800);
        printablePanel.doLayout();
        // Trả về chính printablePanel đã được layout đúng
        return printablePanel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(15, 23, 42));
        return label;
    }

    private String formatCurrency(double value) {
        return String.format(Locale.ROOT, "%,.0f đ", Math.max(0, value)).replace(',', '.');
    }

    public static final class PaymentSummary {
        private final double tamTinh;
        private final Double giamGiaKhuyenMai;
        private final Double giamGiaDiem;
        private final double tongTien;
        private final Double tienKhachDua;
        private final Double tienThoi;

        private PaymentSummary(double tamTinh, Double giamGiaKhuyenMai, Double giamGiaDiem, double tongTien, Double tienKhachDua, Double tienThoi) {
            this.tamTinh = tamTinh;
            this.giamGiaKhuyenMai = giamGiaKhuyenMai;
            this.giamGiaDiem = giamGiaDiem;
            this.tongTien = tongTien;
            this.tienKhachDua = tienKhachDua;
            this.tienThoi = tienThoi;
        }

        public static PaymentSummary afterSale(double tamTinh, double giamGiaKhuyenMai, double giamGiaDiem, double tongTien, Double tienKhachDua, Double tienThoi) {
            return new PaymentSummary(tamTinh, giamGiaKhuyenMai, giamGiaDiem, tongTien, tienKhachDua, tienThoi);
        }

        public static PaymentSummary reprint(double tamTinh, double tongTien) {
            return new PaymentSummary(tamTinh, null, null, tongTien, null, null);
        }

        private boolean hasPaymentData() {
            return tienKhachDua != null || tienThoi != null;
        }
    }
}
