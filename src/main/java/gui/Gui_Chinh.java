package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;
import java.util.Locale;

import util.UiStyle;
import service.TaiKhoanService;
import service.NhanVienService;
import service.ServiceFactory;
import dto.TaiKhoanDto;
import dto.NhanVienDto;
import service.impl.NhanVienServiceImpl;
import service.impl.TaiKhoanServiceImpl;

public class Gui_Chinh extends JFrame {

    private static final Color COLOR_PRIMARY = new Color(18, 78, 130);
    private static final Color COLOR_LIGHT_BG = new Color(241, 245, 249);
    private static final DateTimeFormatter FOOTER_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final boolean isManager;
    private final String currentUser;
    private final String currentEmployeeName;
    private final String currentEmployeeRole;

    public Gui_Chinh(String tenDangNhap) {
        setTitle("Cửa Hàng Tiện Lợi - Quản lý theo JPA ORM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        ImageIcon appIcon = UiStyle.loadIcon("/icon/logomoi.png", 48, 48);
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
        }

        boolean isManager = false;
        String employeeName = tenDangNhap;
        String employeeRole = "Nhân viên";
        if (tenDangNhap != null && !tenDangNhap.isBlank()) {
            TaiKhoanService tkService = ServiceFactory.get(TaiKhoanService.class, TaiKhoanServiceImpl::new);
            TaiKhoanDto dto = tkService.findById(tenDangNhap);
            if (dto != null && dto.getMaNhanVien() != null && !dto.getMaNhanVien().isBlank()) {
                NhanVienService nvService = ServiceFactory.get(NhanVienService.class, NhanVienServiceImpl::new);
                NhanVienDto nv = nvService.findById(dto.getMaNhanVien());
                if (nv != null && nv.getTenChucVu() != null) {
                    employeeName = nv.getTenNhanVien() != null && !nv.getTenNhanVien().isBlank() ? nv.getTenNhanVien() : tenDangNhap;
                    employeeRole = nv.getTenChucVu();
                    String normalized = Normalizer.normalize(nv.getTenChucVu(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
                    isManager = normalized.contains("quan");
                }
            }
        }
        this.isManager = isManager;
        this.currentUser = tenDangNhap;
        this.currentEmployeeName = employeeName == null || employeeName.isBlank() ? "Nhân viên" : employeeName;
        this.currentEmployeeRole = employeeRole == null || employeeRole.isBlank() ? (isManager ? "Quản lý" : "Nhân viên") : employeeRole;
        setJMenuBar(createMenuBar());

        contentPanel.add(new Gui_BanHang(tenDangNhap), "banhang");
        contentPanel.add(new Gui_QuanLiKhachHang(), "khachhang");
        contentPanel.add(new Gui_QuanLiNhanVien(), "nhanvien");
        contentPanel.add(new Gui_SanPham(), "sanpham");
        contentPanel.add(new Gui_NhaCungCap(), "nhacungcap");
        contentPanel.add(new Gui_PhieuNhap(), "phieunhap");
        contentPanel.add(new Gui_LoaiSanPham(), "loaisanpham");
        contentPanel.add(new Gui_TaiKhoan(), "taikhoan");
        contentPanel.add(new Gui_QuanLiKhuyenMai(), "khuyenmai");
        contentPanel.add(new Gui_ChucVu(), "chucvu");
        contentPanel.add(new Gui_HoaDon(), "hoadon");
        contentPanel.add(new Gui_ThongKe(tenDangNhap), "thongke");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        wrapper.add(contentPanel, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);

        showCard("banhang");
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(25, 35, 80));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder());

        bar.add(createMenu("Hệ thống", "/icon/hethong.png", new String[][]{
                {"Trang chủ", "banhang", "/icon/home.png"},
                {"Đổi mật khẩu", "doiMatKhau", "/icon/matkhau.png"},
                {"Đăng xuất", "logout", "/icon/dangxuat.png"},
                {"Thoát", "exit", "/icon/dong.png"}
        }));
        // Danh mục: hiển thị/ẩn các item admin dựa trên vai trò
        if (true) {
            // xây dựng các item một cách động
            java.util.List<String[]> items = new java.util.ArrayList<>();
            items.add(new String[]{"Khách hàng", "khachhang", "/icon/khachhang.png"});
            if (isManager) items.add(new String[]{"Nhân viên", "nhanvien", "/icon/nhanvien.png"});
            items.add(new String[]{"Sản phẩm", "sanpham", "/icon/ve.png"});
            items.add(new String[]{"Nhà cung cấp", "nhacungcap", "/icon/nhacungcap.png"});
            items.add(new String[]{"Hóa đơn", "hoadon", "/icon/hoadon.png"});
            if (isManager) items.add(new String[]{"Tài khoản", "taikhoan", "/icon/taikhoan.png"});
            items.add(new String[]{"Khuyến mãi", "khuyenmai", "/icon/khuyenmai.png"});
            bar.add(createMenu("Danh mục", "/icon/danhmuc.png", items.toArray(new String[0][])));
        }
        // Cập nhật menu: chỉ hiển thị 'Vai trò' cho quản lý viên
        if (true) {
            java.util.List<String[]> items = new java.util.ArrayList<>();
            items.add(new String[]{"Danh mục sản phẩm", "loaisanpham", "/icon/loaisanpham.png"});
            if (isManager) items.add(new String[]{"Chức vụ", "chucvu", "/icon/chucvu.png"});
            bar.add(createMenu("Cập nhật", "/icon/capnhat.png", items.toArray(new String[0][])));
        }
        bar.add(createMenu("Xử lý", "/icon/xuly.png", new String[][]{
                {"Bán hàng", "banhang", "/icon/banhang.png"},
                {"Nhập hàng", "phieunhap", "/icon/phieunhap.png"}
        }));
        bar.add(createMenu("Thống kê", "/icon/thongke.png", new String[][]{
                {"Biểu đồ doanh thu", "thongke", "/icon/doanhthu.png"}
        }));
        return bar;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
        footer.setBackground(new Color(25, 35, 80));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(224, 230, 238)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        JLabel lblGreeting = new JLabel("Xin chào, " + currentEmployeeName);
        lblGreeting.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGreeting.setForeground(Color.WHITE);
        lblGreeting.setPreferredSize(new Dimension(200, 25));
        lblGreeting.setMaximumSize(new Dimension(200, 25));

        JLabel lblRole = new JLabel("Chức vụ: " + currentEmployeeRole);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRole.setForeground(new Color(180, 180, 180));
        lblRole.setPreferredSize(new Dimension(150, 25));
        lblRole.setMaximumSize(new Dimension(150, 25));

        JLabel lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblClock.setForeground(new Color(180, 180, 180));
        lblClock.setPreferredSize(new Dimension(220, 25));
        lblClock.setMaximumSize(new Dimension(220, 25));
        updateClockLabel(lblClock);
        new javax.swing.Timer(1000, e -> updateClockLabel(lblClock)).start();

        footer.add(lblGreeting);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(lblRole);
        footer.add(Box.createHorizontalStrut(20));
        footer.add(lblClock);
        footer.add(Box.createHorizontalGlue());

        return footer;
    }

    private void updateClockLabel(JLabel label) {
        if (label != null) {
            label.setText("Thời gian: " + LocalDateTime.now().format(FOOTER_TIME_FORMATTER));
        }
    }

    private JMenu createMenu(String title, String menuIconPath, String[][] items) {
        JMenu menu = new JMenu(title);
        menu.setIcon(UiStyle.loadIcon(menuIconPath, 30, 30));
        menu.setForeground(UiStyle.MENU_FG);
        menu.setFont(menu.getFont().deriveFont(Font.BOLD));
        menu.setOpaque(false);
        menu.getPopupMenu().setBackground(UiStyle.MENU_ITEM_BG);
        menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menu.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menu.setForeground(UiStyle.MENU_FG);
            }
        });

        for (String[] item : items) {
            JMenuItem menuItem = new JMenuItem(item[0], UiStyle.loadIcon(item[2], 24, 24));
            menuItem.setBackground(UiStyle.MENU_ITEM_BG);
            menuItem.setForeground(UiStyle.MENU_FG);
            menuItem.setOpaque(true);
            menuItem.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            menuItem.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            menuItem.addActionListener(e -> handleAction(item[1]));
            menuItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    menuItem.setBackground(new Color(56, 72, 122));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    menuItem.setBackground(UiStyle.MENU_ITEM_BG);
                }
            });
            menu.add(menuItem);
        }
        return menu;
    }

    private void handleAction(String action) {
        switch (action) {
            case "banhang", "khachhang", "nhanvien", "sanpham", "nhacungcap", "phieunhap", "loaisanpham", "taikhoan",
                 "khuyenmai", "chucvu", "hoadon", "thongke" -> showCard(action);
            case "doiMatKhau" -> {
                Gui_DoiMatKhau dlg = new Gui_DoiMatKhau(this, currentUser);
                dlg.setVisible(true);
            }
            case "logout" -> logout();
            case "exit" -> dispose();
            default -> {
            }
        }
    }

    private void showCard(String key) {
        cardLayout.show(contentPanel, key);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có muốn đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new Gui_Start().setVisible(true);
        }
    }
}
