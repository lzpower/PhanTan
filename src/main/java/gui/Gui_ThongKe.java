package gui;

import dto.DashboardStatsDto;
import service.ThongKeService;
import service.ServiceFactory;
import service.impl.ThongKeServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import util.UiStyle;

public class Gui_ThongKe extends JPanel {

    private final ThongKeService service = ServiceFactory.get(ThongKeService.class, ThongKeServiceImpl::new);

    private JSpinner spYear;
    private JSpinner spCompareYear;
    private ComparisonChartPanel revenueChart;
    private BarChartPanel topCustomerChart;
    private BarChartPanel topProductChart;

    public Gui_ThongKe() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        refreshCharts();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("THỐNG KÊ");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        control.setBackground(UiStyle.PRIMARY);

        int currentYear = java.time.LocalDate.now().getYear();
        spYear = new JSpinner(new SpinnerNumberModel(currentYear, 2000, 2100, 1));
        spCompareYear = new JSpinner(new SpinnerNumberModel(currentYear - 1, 2000, 2100, 1));
        JButton btnRefresh = UiStyle.createActionButton("Tải dữ liệu", new Color(46, 125, 50), "/icon/thongke.png");
        btnRefresh.addActionListener(e -> refreshCharts());

        control.add(makeLabel("Năm:"));
        control.add(spYear);
        control.add(makeLabel("So sánh với:"));
        control.add(spCompareYear);
        control.add(btnRefresh);
        header.add(control, BorderLayout.EAST);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new GridLayout(3, 1, 14, 14));
        content.setBorder(new EmptyBorder(14, 14, 14, 14));
        revenueChart = new ComparisonChartPanel("Doanh thu theo tháng");
        topCustomerChart = new BarChartPanel("Top khách hàng");
        topProductChart = new BarChartPanel("Top sản phẩm");
        content.add(revenueChart);
        content.add(topCustomerChart);
        content.add(topProductChart);
        return content;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    private void refreshCharts() {
        int year = (Integer) spYear.getValue();
        int compareYear = (Integer) spCompareYear.getValue();
        DashboardStatsDto stats = service.thongKeDoanhThu(year, compareYear);
        revenueChart.setData(stats.getMonthlyRevenue(), stats.getCompareMonthlyRevenue(), year, compareYear);
        topCustomerChart.setData(service.topKhachHang(year, 5));
        topProductChart.setData(service.topSanPham(year, 5));
    }

    private abstract static class AbstractChartPanel extends JPanel {
        protected final String title;
        private final Color normalBg = Color.WHITE;
        private final Color hoverBg = new Color(245, 249, 255);

        protected AbstractChartPanel(String title) {
            this.title = title;
            setBackground(normalBg);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 225, 230)),
                    new EmptyBorder(10, 10, 10, 10)));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverBg);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(normalBg);
                    repaint();
                }
            });
        }

        protected void drawTitle(Graphics2D g2) {
            g2.setColor(new Color(40, 50, 70));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.drawString(title, 16, 26);
        }
    }

    private static final class ComparisonChartPanel extends AbstractChartPanel {
        private Map<Integer, Double> primary = new LinkedHashMap<>();
        private Map<Integer, Double> compare = new LinkedHashMap<>();
        private int year;
        private int compareYear;

        private ComparisonChartPanel(String title) {
            super(title);
            setPreferredSize(new Dimension(1000, 260));
        }

        private void setData(Map<Integer, Double> primary, Map<Integer, Double> compare, int year, int compareYear) {
            this.primary = primary != null ? primary : new LinkedHashMap<>();
            this.compare = compare != null ? compare : new LinkedHashMap<>();
            this.year = year;
            this.compareYear = compareYear;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawTitle(g2);
            int left = 60;
            int top = 44;
            int width = getWidth() - 90;
            int height = getHeight() - 80;
            g2.setColor(new Color(245, 247, 250));
            g2.fillRoundRect(left, top, width, height, 20, 20);
            g2.setColor(new Color(220, 225, 230));
            g2.drawRoundRect(left, top, width, height, 20, 20);

            double max = 1;
            for (int m = 1; m <= 12; m++) {
                max = Math.max(max, Math.max(primary.getOrDefault(m, 0D), compare.getOrDefault(m, 0D)));
            }
            int chartLeft = left + 24;
            int chartTop = top + 16;
            int chartWidth = width - 48;
            int chartHeight = height - 52;
            int columnWidth = chartWidth / 12;

            for (int i = 0; i <= 4; i++) {
                int y = chartTop + chartHeight - (chartHeight * i / 4);
                g2.setColor(new Color(225, 230, 235));
                g2.drawLine(chartLeft, y, chartLeft + chartWidth, y);
                g2.setColor(new Color(90, 100, 120));
                g2.drawString(String.format("%.0f", max * i / 4), 10, y + 5);
            }

            for (int month = 1; month <= 12; month++) {
                int x = chartLeft + (month - 1) * columnWidth + 6;
                double primaryValue = primary.getOrDefault(month, 0D);
                double compareValue = compare.getOrDefault(month, 0D);
                int primaryHeight = (int) Math.round(chartHeight * (primaryValue / max));
                int compareHeight = (int) Math.round(chartHeight * (compareValue / max));

                g2.setColor(new Color(52, 152, 219));
                g2.fillRoundRect(x, chartTop + chartHeight - primaryHeight, columnWidth / 3, primaryHeight, 8, 8);
                g2.setColor(new Color(231, 76, 60));
                g2.fillRoundRect(x + columnWidth / 3 + 4, chartTop + chartHeight - compareHeight, columnWidth / 3, compareHeight, 8, 8);

                g2.setColor(new Color(60, 70, 90));
                g2.drawString(String.valueOf(month), x + columnWidth / 6, chartTop + chartHeight + 18);
            }

            g2.setColor(new Color(52, 152, 219));
            g2.fillRect(chartLeft + chartWidth - 160, chartTop - 8, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.valueOf(year), chartLeft + chartWidth - 140, chartTop + 2);
            g2.setColor(new Color(231, 76, 60));
            g2.fillRect(chartLeft + chartWidth - 80, chartTop - 8, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(String.valueOf(compareYear), chartLeft + chartWidth - 60, chartTop + 2);
            g2.dispose();
        }
    }

    private static final class BarChartPanel extends AbstractChartPanel {
        private Map<String, Double> data = new LinkedHashMap<>();

        private BarChartPanel(String title) {
            super(title);
            setPreferredSize(new Dimension(1000, 220));
        }

        private void setData(Map<String, Double> data) {
            this.data = data != null ? data : new LinkedHashMap<>();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawTitle(g2);

            int left = 60;
            int top = 44;
            int width = getWidth() - 90;
            int height = getHeight() - 70;
            g2.setColor(new Color(245, 247, 250));
            g2.fillRoundRect(left, top, width, height, 20, 20);
            g2.setColor(new Color(220, 225, 230));
            g2.drawRoundRect(left, top, width, height, 20, 20);

            double max = Math.max(1D, data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1D));
            int barArea = height - 48;
            int barWidth = Math.max(50, width / Math.max(1, data.size()) - 16);
            int index = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int x = left + 18 + index * (barWidth + 18);
                int barHeight = (int) Math.round(barArea * (entry.getValue() / max));
                g2.setColor(new Color(18, 78, 130));
                g2.fillRoundRect(x, top + barArea - barHeight, barWidth, barHeight, 12, 12);
                g2.setColor(new Color(60, 70, 90));
                String label = entry.getKey();
                if (label.length() > 14) {
                    label = label.substring(0, 14) + "...";
                }
                g2.drawString(label, x, top + barArea + 18);
                g2.drawString(String.format("%.0f", entry.getValue()), x, top + barArea - barHeight - 6);
                index++;
            }
            g2.dispose();
        }
    }
}