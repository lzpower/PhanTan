
package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import dto.TaiKhoanDto;
import service.TaiKhoanService;
import service.ServiceFactory;
import network.ClientSession;

public class Gui_Start extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
	private JTextField txtTK;
    private JPasswordField txtMK;
    private JButton btnDN;
    private JButton btnTogglePassword;
    private JButton btnQuenMatKhau;
    @SuppressWarnings("unused")
	private boolean passwordVisible = false;
    private final TaiKhoanService taiKhoanService;

    public Gui_Start() {
        super("Đăng Nhập");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        ClientSession.configureRemote("DESKTOP-HAK9M95", 9090, ClientSession.defaultClientName());
        taiKhoanService = ServiceFactory.get(TaiKhoanService.class, () -> null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel lblTD = new JLabel("ĐĂNG NHẬP TÀI KHOẢN", SwingConstants.CENTER);
        lblTD.setForeground(new Color(0, 153, 255));
        lblTD.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel lblTK = new JLabel("Tài khoản:", SwingConstants.RIGHT);
        lblTK.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel lblMK = new JLabel("Mật khẩu:", SwingConstants.RIGHT);
        lblMK.setFont(new Font("Arial", Font.PLAIN, 14));

        txtTK = new JTextField(20);
        txtTK.setPreferredSize(new Dimension(200, 30));

        txtMK = new JPasswordField();
        txtMK.setPreferredSize(new Dimension(130, 30));
        txtMK.setEchoChar('*');

        btnTogglePassword = new JButton("Hiện");
        btnTogglePassword.setPreferredSize(new Dimension(70, 30));
        btnTogglePassword.setBackground(new Color(230, 230, 230));
        btnTogglePassword.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        btnTogglePassword.setFocusPainted(false);

        btnDN = new JButton("Đăng nhập");
        btnDN.setPreferredSize(new Dimension(120, 30));
        btnDN.setBackground(new Color(230, 230, 230));
        btnDN.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        btnDN.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(lblTD, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(lblTK, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(txtTK, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 0, 5, 10);
        mainPanel.add(lblMK, gbc);

        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new GridBagLayout());
        GridBagConstraints passwordGbc = new GridBagConstraints();

        passwordGbc.gridx = 0;
        passwordGbc.gridy = 0;
        passwordGbc.weightx = 1.0;
        passwordGbc.fill = GridBagConstraints.HORIZONTAL;
        passwordPanel.add(txtMK, passwordGbc);

        passwordGbc.gridx = 1;
        passwordGbc.gridy = 0;
        passwordGbc.weightx = 0;
        passwordGbc.fill = GridBagConstraints.NONE;
        passwordGbc.insets = new Insets(0, 5, 0, 0);
        passwordPanel.add(btnTogglePassword, passwordGbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(passwordPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);
        mainPanel.add(btnDN, gbc);

        btnQuenMatKhau = new JButton("Quên mật khẩu");
        btnQuenMatKhau.setPreferredSize(new Dimension(140, 26));
        btnQuenMatKhau.setBorder(BorderFactory.createEmptyBorder());
        btnQuenMatKhau.setForeground(new Color(52, 152, 219));
        btnQuenMatKhau.setContentAreaFilled(false);
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 0, 0, 0);
        mainPanel.add(btnQuenMatKhau, gbc);

        btnTogglePassword.addActionListener(e -> {
            if (txtMK.getEchoChar() == '*') {
                txtMK.setEchoChar((char) 0);
                btnTogglePassword.setText("Ẩn");
                passwordVisible = true;
            } else {
                txtMK.setEchoChar('*');
                btnTogglePassword.setText("Hiện");
                passwordVisible = false;
            }
        });
        btnDN.addActionListener(this);
        txtTK.addActionListener(e -> txtMK.requestFocus());
        txtMK.addActionListener(e -> btnDN.doClick());

        try {
            setIconImage(new ImageIcon(getClass().getResource("/img/logo.png")).getImage());
        } catch (Exception e) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String tk = txtTK.getText().trim();
        String mk = new String(txtMK.getPassword()).trim();

        if (tk.isEmpty() || mk.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        TaiKhoanDto taiKhoan = taiKhoanService.findById(tk);
        if (taiKhoan != null && taiKhoan.getMatKhau().equals(mk)) {
            SwingUtilities.invokeLater(() -> {
                new Gui_Chinh(tk).setVisible(true);
                dispose(); // Đóng form đăng nhập
            });
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Tài khoản hoặc mật khẩu không đúng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args){
        System.setProperty("sun.java2d.uiScale", "1.0");
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.put("TitlePane.menuBarEmbedded", false);
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                    UIManager.put("MenuBar.foreground", Color.WHITE);
                    UIManager.put("defaultFont", new Font("Segoe UI Semibold", Font.PLAIN, 14));
                    UIManager.put("TextComponent.arc", 12);
                    UIManager.put("Button.arc", 12);
//                    new Gui_Start().setVisible(true);
                    new Gui_Chinh("admin").setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}