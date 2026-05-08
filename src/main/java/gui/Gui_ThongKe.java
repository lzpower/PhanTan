package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import dto.DashboardStatsDto;
import dto.ProductSalesDto;
import dto.SanPhamDto;
import dto.TaiKhoanDto;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import service.ServiceFactory;
import service.TaiKhoanService;
import service.ThongKeService;
import service.impl.ThongKeServiceImpl;
import util.UiStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Thống kê - Dashboard
 * Bao gồm: Thẻ tóm tắt, Biểu đồ doanh thu, Top sản phẩm và Cảnh báo tồn kho.
 */
public class Gui_ThongKe extends JPanel {

    private final ThongKeService service = ServiceFactory.get(ThongKeService.class, ThongKeServiceImpl::new);
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

    private String currentEmployeeId;
    private DatePicker dpStart, dpEnd;
    private JLabel lblRevSummary, lblInvSummary, lblProdSummary, lblEmpSummary;
    private JPanel pnlChartContainer;
    private JTable tblTopProducts, tblLowStock;
    private DefaultTableModel modelTopProducts, modelLowStock;

    public Gui_ThongKe() {
        this(null);
    }

    public Gui_ThongKe(String tenDangNhap) {
        if (tenDangNhap != null) {
            try {
                TaiKhoanService tkService = ServiceFactory.get(TaiKhoanService.class, () -> null);
                TaiKhoanDto tk = tkService.findById(tenDangNhap);
                if (tk != null) {
                    this.currentEmployeeId = tk.getMaNhanVien();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setLayout(new BorderLayout(10, 10));
        setBackground(UiStyle.LIGHT_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // 1. Header & Filter
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content
        JPanel pnlCenter = new JPanel(new BorderLayout(15, 15));
        pnlCenter.setOpaque(false);

        // Summary Cards
        pnlCenter.add(createSummaryPanel(), BorderLayout.NORTH);

        // Tabbed Pane for Charts and Tables
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Tab Biểu đồ
        pnlChartContainer = new JPanel(new BorderLayout());
        pnlChartContainer.setBackground(Color.WHITE);
        tabbedPane.addTab("Biểu đồ doanh thu", new ImageIcon(getClass().getResource("/icon/doanhthu.png")), pnlChartContainer);

        // Tab Top Sản Phẩm
        tabbedPane.addTab("Top sản phẩm bán chạy", new ImageIcon(getClass().getResource("/icon/banhang.png")), createTopProductsPanel());

        // Tab Cảnh báo tồn kho
        tabbedPane.addTab("Cảnh báo tồn kho", new ImageIcon(getClass().getResource("/icon/phieunhap.png")), createLowStockPanel());

        pnlCenter.add(tabbedPane, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("THỐNG KÊ & DASHBOARD");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(UiStyle.PRIMARY);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlFilter.setOpaque(false);

        DatePickerSettings s1 = new DatePickerSettings();
        s1.setFormatForDatesCommonEra("dd/MM/yyyy");
        dpStart = new DatePicker(s1);
        dpStart.setDate(LocalDate.now().minusDays(7));
        dpStart.setPreferredSize(new Dimension(170, 35));
        UiStyle.styleDatePicker(dpStart);

        DatePickerSettings s2 = new DatePickerSettings();
        s2.setFormatForDatesCommonEra("dd/MM/yyyy");
        dpEnd = new DatePicker(s2);
        dpEnd.setDate(LocalDate.now());
        dpEnd.setPreferredSize(new Dimension(170, 35));
        UiStyle.styleDatePicker(dpEnd);

        JButton btnRefresh = UiStyle.createActionButton("Lọc", new Color(46, 125, 50), "/icon/thongke.png");
        btnRefresh.setPreferredSize(new Dimension(100, 35));
        btnRefresh.addActionListener(e -> loadData());

        pnlFilter.add(new JLabel("Từ ngày:"));
        pnlFilter.add(dpStart);
        pnlFilter.add(new JLabel("Đến ngày:"));
        pnlFilter.add(dpEnd);
        pnlFilter.add(btnRefresh);

        pnlHeader.add(pnlFilter, BorderLayout.EAST);
        return pnlHeader;
    }

    private JPanel createSummaryPanel() {
        JPanel pnlSummary = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlSummary.setOpaque(false);
        pnlSummary.setPreferredSize(new Dimension(0, 120));

        lblRevSummary = new JLabel("0 VNĐ");
        lblInvSummary = new JLabel("0");
        lblProdSummary = new JLabel("0");
        lblEmpSummary = new JLabel("0 VNĐ");

        pnlSummary.add(createCard("Doanh thu kỳ này", lblRevSummary, new Color(41, 128, 185), "/icon/doanhthu.png"));
        pnlSummary.add(createCard("Doanh thu CÁ NHÂN", lblEmpSummary, new Color(155, 89, 182), "/icon/nhanvien.png"));
        pnlSummary.add(createCard("Tổng hóa đơn", lblInvSummary, new Color(39, 174, 96), "/icon/hoadon.png"));
        pnlSummary.add(createCard("Sản phẩm bán ra", lblProdSummary, new Color(230, 126, 34), "/icon/banhang.png"));

        return pnlSummary;
    }

    private JPanel createCard(String title, JLabel lblValue, Color color, String iconPath) {
        JPanel card = new JPanel(new BorderLayout(15, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(15, 12, 15, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);
        card.add(lblTitle, BorderLayout.NORTH);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(color);
        card.add(lblValue, BorderLayout.CENTER);

        JLabel lblIcon = new JLabel(UiStyle.loadIcon(iconPath, 35, 35));
        card.add(lblIcon, BorderLayout.EAST);

        return card;
    }

    private JPanel createTopProductsPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        
        String[] cols = {"Mã SP", "Tên sản phẩm", "Số lượng bán", "Doanh thu"};
        modelTopProducts = new DefaultTableModel(cols, 0);
        tblTopProducts = new JTable(modelTopProducts);
        JScrollPane scroll = new JScrollPane(tblTopProducts);
        UiStyle.styleTable(tblTopProducts, scroll);
        
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createLowStockPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        
        String[] cols = {"Mã SP", "Tên sản phẩm", "Loại", "Tồn kho", "Giá bán"};
        modelLowStock = new DefaultTableModel(cols, 0);
        tblLowStock = new JTable(modelLowStock);
        JScrollPane scroll = new JScrollPane(tblLowStock);
        UiStyle.styleTable(tblLowStock, scroll);
        
        pnl.add(scroll, BorderLayout.CENTER);
        return pnl;
    }

    private void loadData() {
        LocalDate start = dpStart.getDate();
        LocalDate end = dpEnd.getDate();
        
        if (start == null || end == null) return;

        // 1. Load Summary (Filtered by range and employee)
        DashboardStatsDto stats = service.getStats(start, end, currentEmployeeId);
        lblRevSummary.setText(String.format("%,.0f VNĐ", stats.getTotalRevenueToday()));
        lblInvSummary.setText(String.valueOf(stats.getTotalInvoicesToday()));
        lblProdSummary.setText(String.valueOf(stats.getTotalProductsSoldToday()));
        lblEmpSummary.setText(String.format("%,.0f VNĐ", stats.getEmployeeRevenueToday()));

        // 2. Load Chart and Top Products
        updateRevenueChart(start, end);
        updateTopProducts(start, end);

        // 3. Load Low Stock
        updateLowStock();
    }

    private void updateRevenueChart(LocalDate start, LocalDate end) {
        Map<LocalDate, Double> data = service.getRevenueByDateRange(start, end);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        LocalDate current = start;
        while (!current.isAfter(end)) {
            dataset.addValue(data.getOrDefault(current, 0.0), "Doanh thu", current.format(dtf));
            current = current.plusDays(1);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Biểu đồ doanh thu từ " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến " + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "Ngày", "Doanh thu (VNĐ)",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        styleChart(chart);

        pnlChartContainer.removeAll();
        pnlChartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
        pnlChartContainer.revalidate();
        pnlChartContainer.repaint();
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, UiStyle.PRIMARY);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(renderer);
        
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
    }

    private void updateTopProducts(LocalDate start, LocalDate end) {
        modelTopProducts.setRowCount(0);
        List<ProductSalesDto> list = service.getTopProducts(start, end, 10);
        for (ProductSalesDto dto : list) {
            modelTopProducts.addRow(new Object[]{
                    dto.getMaSanPham(),
                    dto.getTenSanPham(),
                    dto.getSoLuongBan(),
                    String.format("%,.0f", dto.getDoanhThu())
            });
        }
    }

    private void updateLowStock() {
        modelLowStock.setRowCount(0);
        List<SanPhamDto> list = service.getLowStockProducts(10);
        for (SanPhamDto dto : list) {
            modelLowStock.addRow(new Object[]{
                    dto.getMaSanPham(),
                    dto.getTenSanPham(),
                    dto.getTenLoaiSanPham(),
                    dto.getSoLuongHienCo(),
                    String.format("%,.0f", dto.getGiaBan())
            });
        }
    }
}
