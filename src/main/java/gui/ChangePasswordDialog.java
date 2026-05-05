package gui;

import service.TaiKhoanService;
import service.impl.TaiKhoanServiceImpl;
import dto.TaiKhoanDto;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final JTextField txtUser;
    private final JPasswordField txtCurrent;
    private final JPasswordField txtNew;
    private final JPasswordField txtConfirm;
    private final TaiKhoanService taiKhoanService = new TaiKhoanServiceImpl();

    public ChangePasswordDialog(Frame owner, String username) {
        super(owner, "Đổi mật khẩu", true);
        setSize(360, 260);
        setLocationRelativeTo(owner);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Tài khoản:"), gbc);
        txtUser = new JTextField(username == null ? "" : username);
        txtUser.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        add(txtUser, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Mật khẩu hiện tại:"), gbc);
        txtCurrent = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 1;
        add(txtCurrent, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Mật khẩu mới:"), gbc);
        txtNew = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 2;
        add(txtNew, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Xác nhận:"), gbc);
        txtConfirm = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 3;
        add(txtConfirm, gbc);

        JButton btnChange = new JButton("Đổi mật khẩu");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(btnChange, gbc);

        btnChange.addActionListener(e -> doChange());
    }

    private void doChange() {
        String user = txtUser.getText().trim();
        String current = new String(txtCurrent.getPassword());
        String n = new String(txtNew.getPassword());
        String c = new String(txtConfirm.getPassword());
        if (user.isEmpty() || current.isEmpty() || n.isEmpty() || c.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TaiKhoanDto dto = taiKhoanService.findById(user);
        if (dto == null || !dto.getMatKhau().equals(current)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu hiện tại không đúng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!n.equals(c)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok = taiKhoanService.resetPassword(user, n);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
