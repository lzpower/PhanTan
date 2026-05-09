package gui;

import dto.LoaiSanPhamDto;
import dto.SanPhamDto;
import network.ImageLoader;
import network.ImageUploader;
import service.LoaiSanPhamService;
import service.SanPhamService;
import service.ServiceFactory;
import service.impl.LoaiSanPhamServiceImpl;
import service.impl.SanPhamServiceImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Locale;
import javax.swing.filechooser.FileNameExtensionFilter;
import util.TableUtility;
import util.UiStyle;

public class Gui_SanPham extends JPanel {

    private final SanPhamService service = ServiceFactory.get(SanPhamService.class, SanPhamServiceImpl::new);
    private final LoaiSanPhamService loaiSanPhamService = ServiceFactory.get(LoaiSanPhamService.class, LoaiSanPhamServiceImpl::new);

    private JTextField txtSearch;
    private JTextField txtMa;
    private JTextField txtTen;
    private JComboBox<LoaiSanPhamDto> cboLoai;
    private JTextField txtSoLuong;
    private JTextField txtGiaBan;
    private JTextField txtHinh;
    private JLabel lblHinhAnh;
    private JButton btnChonAnh;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnReset;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    public Gui_SanPham() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        loadLoaiSanPham();
        loadData(service.loadAll());
        resetForm();

        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnReset.addActionListener(e -> resetForm());
        btnChonAnh.addActionListener(e -> chonHinhAnh());
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

        // Reload dữ liệu mỗi khi panel được hiển thị lại (chuyển tab, quay lại từ màn hình khác)
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadData(service.loadAll());
            }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UiStyle.PRIMARY);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("QUẢN LÝ SẢN PHẨM");
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

        JLabel title = new JLabel("Thông tin sản phẩm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 16, 0);
        panel.add(title, gbc);

        int row = 1;
        panel.add(label("Mã sản phẩm:"), gc(row++));
        txtMa = new JTextField();
        txtMa.setEditable(false);
        txtMa.setEnabled(false);
        panel.add(txtMa, gc(row++));

        panel.add(label("Tên sản phẩm:"), gc(row++));
        txtTen = new JTextField();
        panel.add(txtTen, gc(row++));

        panel.add(label("Loại sản phẩm:"), gc(row++));
        cboLoai = new JComboBox<>();
        panel.add(cboLoai, gc(row++));

        panel.add(label("Số lượng hiện có:"), gc(row++));
        txtSoLuong = new JTextField();
        txtSoLuong.setEditable(false);
        txtSoLuong.setEnabled(false);
        panel.add(txtSoLuong, gc(row++));

        panel.add(label("Giá bán:"), gc(row++));
        txtGiaBan = new JTextField();
        panel.add(txtGiaBan, gc(row++));

        panel.add(label("URL hình ảnh:"), gc(row++));
        txtHinh = new JTextField();
        txtHinh.setEditable(false);
        panel.add(txtHinh, gc(row++));

        btnChonAnh = UiStyle.createActionButton("Chọn ảnh", new Color(52, 152, 219), "/icon/tim.png");
        panel.add(btnChonAnh, gc(row++));

        lblHinhAnh = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblHinhAnh.setOpaque(true);
        lblHinhAnh.setBackground(Color.WHITE);
        lblHinhAnh.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        lblHinhAnh.setPreferredSize(new Dimension(0, 180));
        panel.add(lblHinhAnh, gc(row++));

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
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 15));
        tablePanel.setBackground(UiStyle.LIGHT_BG);
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Danh sách sản phẩm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UiStyle.PRIMARY);
        tablePanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"STT", "Hình ảnh", "Mã", "Tên", "Loại", "Số lượng", "Gia hạn", "Thao tác"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        UiStyle.styleTable(table, scrollPane);

        // Giu nguyen renderer anh, them nhanh fetch tu server
        table.getColumnModel().getColumn(1).setCellRenderer(new ImageTableCellRenderer());

        // Giu nguyen nut Sua/Xoa dung nhu code goc
        TableUtility.addActionColumn(table, 7, "/icon/sua.png", "/icon/xoa.png", new TableUtility.ActionButtonCallback() {
            @Override
            public void onEdit(int modelRow) {
                chonSanPham(modelRow);
            }

            @Override
            public void onDelete(int modelRow) {
                xoaSanPham(modelRow);
            }
        });

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    // ImageTableCellRenderer: fix request trung lap bang FETCHING guard
    private static class ImageTableCellRenderer extends DefaultTableCellRenderer {
        // Cache icon da scale: key = ten file hoac URL
        private static final java.util.concurrent.ConcurrentHashMap<String, ImageIcon> IMAGE_CACHE
                = new java.util.concurrent.ConcurrentHashMap<>();
        // Guard: danh dau nhung file DANG duoc fetch, tranh nhieu thread fetch cung 1 file
        private static final java.util.Set<String> FETCHING
                = java.util.concurrent.ConcurrentHashMap.newKeySet();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);

            String path = value == null ? "" : String.valueOf(value).trim();
            if (path.isBlank()) {
                lbl.setIcon(null);
                return lbl;
            }

            // Co cache roi: hien thi ngay, KHONG fetch them bat ke repaint bao nhieu lan
            ImageIcon cached = IMAGE_CACHE.get(path);
            if (cached != null) {
                if (cached.getIconWidth() > 0) lbl.setIcon(cached);
                return lbl;
            }

            // Chua co cache: hien thi placeholder va fetch (neu chua ai dang fetch)
            lbl.setText("...");
            if (!FETCHING.add(path)) {
                // Co thread khac dang fetch file nay roi, bo qua
                return lbl;
            }

            // Nhanh 1: du lieu cu Backblaze / HTTP
            if (path.startsWith("http://") || path.startsWith("https://")) {
                new Thread(() -> {
                    try {
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                                new java.net.URL(path).openConnection();
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        conn.connect();
                        java.awt.Image img = javax.imageio.ImageIO.read(conn.getInputStream());
                        if (img != null) {
                            ImageIcon icon = new ImageIcon(img.getScaledInstance(80, 50, java.awt.Image.SCALE_SMOOTH));
                            IMAGE_CACHE.put(path, icon);
                            SwingUtilities.invokeLater(table::repaint);
                        } else {
                            IMAGE_CACHE.put(path, new ImageIcon()); // placeholder rong, khong retry
                        }
                    } catch (Exception ignored) {
                        IMAGE_CACHE.put(path, new ImageIcon()); // placeholder rong, khong retry
                    } finally {
                        FETCHING.remove(path);
                    }
                }, "img-http-" + path.hashCode()).start();
            }
            // Nhanh 2: ten file luu tren server, fetch bytes qua socket
            else {
                new Thread(() -> {
                    try {
                        byte[] bytes = ImageLoader.fetchBytes(path); // 1 socket request duy nhat
                        if (bytes != null && bytes.length > 0) {
                            java.awt.Image img = javax.imageio.ImageIO.read(
                                    new java.io.ByteArrayInputStream(bytes));
                            if (img != null) {
                                ImageIcon icon = new ImageIcon(img.getScaledInstance(80, 50, java.awt.Image.SCALE_SMOOTH));
                                IMAGE_CACHE.put(path, icon);
                                SwingUtilities.invokeLater(table::repaint);
                                return;
                            }
                        }
                        IMAGE_CACHE.put(path, new ImageIcon()); // placeholder rong, khong retry
                    } catch (Exception ignored) {
                        IMAGE_CACHE.put(path, new ImageIcon()); // placeholder rong, khong retry
                    } finally {
                        FETCHING.remove(path);
                    }
                }, "img-socket-" + path).start();
            }
            return lbl;
        }

        /** Xoa cache 1 file — goi sau khi upload anh moi cho san pham */
        static void evict(String fileName) {
            if (fileName != null) { IMAGE_CACHE.remove(fileName); FETCHING.remove(fileName); }
        }
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

    private void loadLoaiSanPham() {
        cboLoai.removeAllItems();
        for (LoaiSanPhamDto dto : loaiSanPhamService.loadAll()) {
            cboLoai.addItem(dto);
        }
    }

    private void loadData(List<SanPhamDto> items) {
        model.setRowCount(0);
        int stt = 1;
        for (SanPhamDto item : items) {
            model.addRow(new Object[]{
                    stt++,
                    item.getUrlHinhAnh(),
                    item.getMaSanPham(),
                    item.getTenSanPham(),
                    item.getTenLoaiSanPham(),
                    item.getSoLuongHienCo(),
                    item.getGiaBan(),
                    ""
            });
        }
    }

    public void refreshData() {
        loadData(service.loadAll());
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
        chonSanPham(row);
    }

    private void chonSanPham(int row) {
        txtMa.setText(String.valueOf(model.getValueAt(row, 2)));
        txtTen.setText(String.valueOf(model.getValueAt(row, 3)));
        selectLoai(String.valueOf(model.getValueAt(row, 4)));
        txtSoLuong.setText(String.valueOf(model.getValueAt(row, 5)));
        txtGiaBan.setText(String.valueOf(model.getValueAt(row, 6)));

        SanPhamDto dto = service.findById(txtMa.getText().trim());
        String imagePath = dto != null ? dto.getUrlHinhAnh() : null;
        txtHinh.setText(imagePath != null ? imagePath : "");
        updateImagePreview(imagePath);
        showEditMode();
    }

    private void selectLoai(String tenLoai) {
        for (int i = 0; i < cboLoai.getItemCount(); i++) {
            LoaiSanPhamDto dto = cboLoai.getItemAt(i);
            if (dto != null && dto.getTenLoaiSanPham() != null && dto.getTenLoaiSanPham().equalsIgnoreCase(tenLoai)) {
                cboLoai.setSelectedIndex(i);
                return;
            }
        }
    }

    private void addProduct() {
        try {
            SanPhamDto dto = collectForm();
            dto.setMaSanPham(service.nextId());
            service.save(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void updateProduct() {
        try {
            if (txtMa.getText().isBlank()) {
                showError("Chọn một sản phẩm để cập nhật.");
                return;
            }
            SanPhamDto dto = collectForm();
            dto.setMaSanPham(txtMa.getText().trim());
            service.update(dto);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void xoaSanPham(int row) {
        String ma = String.valueOf(model.getValueAt(row, 2));
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(ma);
            loadData(service.search(txtSearch.getText()));
            resetForm();
        }
    }

    private SanPhamDto collectForm() {
        LoaiSanPhamDto loai = (LoaiSanPhamDto) cboLoai.getSelectedItem();
        SanPhamDto dto = new SanPhamDto();
        dto.setTenSanPham(txtTen.getText().trim());
        dto.setMaLoaiSanPham(loai != null ? loai.getMaLoaiSanPham() : null);
        dto.setTenLoaiSanPham(loai != null ? loai.getTenLoaiSanPham() : null);
        dto.setSoLuongHienCo(Integer.parseInt(txtSoLuong.getText().trim()));
        dto.setGiaBan(Double.parseDouble(txtGiaBan.getText().trim()));
        dto.setUrlHinhAnh(txtHinh.getText().trim());
        return dto;
    }

    private void resetForm() {
        txtMa.setText(service.nextId());
        txtTen.setText("");
        if (cboLoai.getItemCount() > 0) {
            cboLoai.setSelectedIndex(0);
        }
        txtSoLuong.setText("0");
        txtGiaBan.setText("0");
        txtHinh.setText("");
        updateImagePreview(null);
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

    // chonHinhAnh: thay B2Uploader bang ImageUploader (gui qua socket)
    private void chonHinhAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hinh anh", "png", "jpg", "jpeg", "gif", "bmp"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                btnChonAnh.setEnabled(false);
                lblHinhAnh.setIcon(null);
                lblHinhAnh.setText("Đang gữi ảnh lên server...");
                txtHinh.setText("");

                new SwingWorker<String, Void>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        return ImageUploader.upload(file);
                    }

                    @Override
                    protected void done() {
                        try {
                            String fileName = get();
                            txtHinh.setText(fileName);
                            // Xoa cache cu cua file nay de bang hien thi anh moi ngay
                            ImageTableCellRenderer.evict(fileName);
                            updateImagePreview(fileName);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showError("Gui anh that bai: " + ex.getMessage());
                            lblHinhAnh.setText("Loi gui anh");
                        } finally {
                            btnChonAnh.setEnabled(true);
                        }
                    }
                }.execute();
            }
        }
    }

    // updateImagePreview: ten file thi fetch tu server, URL http giu nguyen
    private void updateImagePreview(String imagePath) {
        if (lblHinhAnh == null) return;

        if (imagePath == null || imagePath.isBlank()) {
            lblHinhAnh.setIcon(null);
            lblHinhAnh.setText("Chua co anh");
            return;
        }

        // Du lieu cu Backblaze / HTTP
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            lblHinhAnh.setText("Dang tai anh...");
            lblHinhAnh.setIcon(null);
            new Thread(() -> {
                try {
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection)
                            new java.net.URL(imagePath).openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.connect();
                    java.awt.Image img = javax.imageio.ImageIO.read(conn.getInputStream());
                    if (img != null) {
                        int w = Math.max(220, lblHinhAnh.getWidth());
                        int h = Math.max(160, lblHinhAnh.getHeight());
                        java.awt.Image scaled = img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
                        ImageIcon icon = new ImageIcon(scaled);
                        SwingUtilities.invokeLater(() -> {
                            lblHinhAnh.setIcon(icon);
                            lblHinhAnh.setText("");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> lblHinhAnh.setText("Khong tai duoc anh"));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> lblHinhAnh.setText("Loi tai anh: " + e.getMessage()));
                }
            }).start();
        }
        // Ten file moi: fetch bytes tu server qua socket
        else {
            lblHinhAnh.setText("Dang tai anh...");
            lblHinhAnh.setIcon(null);
            new Thread(() -> {
                try {
                    byte[] bytes = ImageLoader.fetchBytes(imagePath);
                    if (bytes != null && bytes.length > 0) {
                        java.awt.Image img = javax.imageio.ImageIO.read(
                                new java.io.ByteArrayInputStream(bytes));
                        if (img != null) {
                            int w = Math.max(220, lblHinhAnh.getWidth());
                            int h = Math.max(160, lblHinhAnh.getHeight());
                            java.awt.Image scaled = img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
                            ImageIcon icon = new ImageIcon(scaled);
                            SwingUtilities.invokeLater(() -> {
                                lblHinhAnh.setIcon(icon);
                                lblHinhAnh.setText("");
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> lblHinhAnh.setText("Khong doc duoc anh"));
                        }
                    } else {
                        SwingUtilities.invokeLater(() -> lblHinhAnh.setText("Khong tim thay anh"));
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> lblHinhAnh.setText("Loi tai anh: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }
}