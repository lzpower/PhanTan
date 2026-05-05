package util;

import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public final class UiStyle {

    public static final Color MENU_BG = new Color(18, 78, 130);
    public static final Color MENU_ITEM_BG = new Color(35, 45, 90);
    public static final Color MENU_FG = new Color(230, 230, 240);
    public static final Color PRIMARY = new Color(18, 78, 130);
    public static final Color LIGHT_BG = new Color(241, 245, 249);
    public static final Color ROW_ODD = new Color(225, 245, 254);
    public static final Color ROW_HOVER = new Color(240, 248, 255);
    public static final Color ROW_SELECTED = new Color(92, 188, 247);

    private UiStyle() {
    }

    public static ImageIcon loadIcon(String resourcePath, int width, int height) {
        URL url = UiStyle.class.getResource(resourcePath);
        if (url == null) {
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static JButton createActionButton(String text, Color color, String iconPath) {
        JButton button = new JButton(text);
        styleButton(button, color);
        ImageIcon icon = loadIcon(iconPath, 18, 18);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(10);
        }
        return button;
    }

    public static void styleButton(JButton button, Color color) {
        if (button == null) {
            return;
        }
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Integer.MAX_VALUE, 55));
        button.setBorder(new LineBorder(color.darker(), 1, true));
        button.setMargin(new Insets(10, 18, 10, 18));
        button.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    ButtonModel model = ((AbstractButton) c).getModel();
                    Color fill = c.getBackground();
                    if (model.isPressed()) {
                        fill = fill.darker();
                    } else if (model.isRollover()) {
                        fill = fill.brighter();
                    }
                    g2.setColor(fill);
                    g2.fill(new RoundRectangle2D.Double(0, 0, c.getWidth() - 1, c.getHeight() - 1, 18, 18));
                } finally {
                    g2.dispose();
                }
                super.paint(g, c);
            }
        });

        Color hover = color.brighter();
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
    }

    public static void styleTable(JTable table, JScrollPane scrollPane) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(new Color(222, 228, 236));
        table.setIntercellSpacing(new Dimension(8, 2));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));

        HoverRenderer renderer = new HoverRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                renderer.setHoverRow(table.rowAtPoint(e.getPoint()));
                table.repaint();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                renderer.setHoverRow(-1);
                table.repaint();
            }
        });

        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    public static void styleDatePicker(DatePicker datePicker) {
        if (datePicker == null) {
            return;
        }
        JTextField textField = datePicker.getComponentDateTextField();
        JButton toggleButton = datePicker.getComponentToggleCalendarButton();
        if (textField != null) {
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setBorder(UIManager.getBorder("TextField.border"));
            textField.setBackground(Color.WHITE);
            textField.setMargin(new Insets(5, 8, 5, 8));
        }
        if (toggleButton != null) {
            toggleButton.setText("");
            toggleButton.setIcon(loadIcon("/icon/datepicker.png", 20, 20));
            toggleButton.setPreferredSize(new Dimension(45, 45));
            toggleButton.setBackground(Color.WHITE);
        }
    }

    private static final class HoverRenderer extends DefaultTableCellRenderer {
        private int hoverRow = -1;

        private void setHoverRow(int hoverRow) {
            this.hoverRow = hoverRow;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                c.setBackground(ROW_SELECTED);
            } else if (row == hoverRow) {
                c.setBackground(ROW_HOVER);
            } else {
                c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ODD);
            }
            return c;
        }
    }
}