package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class TableUtility {

	public static final Color DEFAULT_HEADER_BG = new Color(18, 78, 130);
	public static final Color DEFAULT_HEADER_FG = Color.WHITE;
	public static final Color DEFAULT_ODD_ROW = new Color(225, 245, 254);
	public static final Color DEFAULT_HOVER_ROW = new Color(240, 248, 255);
	public static final Color DEFAULT_EDIT_NORMAL = new Color(52, 152, 219);
	public static final Color DEFAULT_EDIT_HOVER = new Color(41, 128, 185);
	public static final Color DEFAULT_DELETE_NORMAL = new Color(252, 28, 77);
	public static final Color DEFAULT_DELETE_HOVER = new Color(192, 57, 43);

	
	public static void formatTable(JTable tb, int cao, Color nenTieuDe, Color chuTieuDe, Color dongLe) {
		 if (cao > 0) {
		        tb.setRowHeight(cao);
		    }
		tb.getTableHeader().setReorderingAllowed(false);
		tb.setFillsViewportHeight(true);
		tb.setShowGrid(true);

		// Format header
		JTableHeader header = tb.getTableHeader();
		header.setBackground(nenTieuDe);
		header.setForeground(chuTieuDe);
		header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		// Thêm renderer cho các ô thường với hiệu ứng hover
		tb.setDefaultRenderer(Object.class, new HoverTableCellRenderer(dongLe));
	}
	public static void addActionColumn(JTable table, int columnIndex, String iconSua, String iconXoa,
			ActionButtonCallback callback) {
		TableColumn actionColumn = table.getColumnModel().getColumn(columnIndex);
		ActionButtonRenderer renderer = new ActionButtonRenderer(iconSua, iconXoa);
		actionColumn.setCellRenderer(renderer);

		table.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point point = e.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);

				// Repaint để cập nhật hover cho dòng
				table.repaint();

				if (column == columnIndex && row >= 0) {
					Rectangle cellRect = table.getCellRect(row, column, false);
					Point relativePoint = new Point(e.getX() - cellRect.x, e.getY() - cellRect.y);
					renderer.updateHover(row, relativePoint);
					table.repaint(cellRect);
					table.setCursor(new Cursor(Cursor.HAND_CURSOR));
				} else {
					renderer.clearHover();
					table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				renderer.clearHover();
				table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				table.repaint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				Point point = e.getPoint();
				int row = table.rowAtPoint(point);
				int column = table.columnAtPoint(point);

				if (column == columnIndex && row >= 0) {
					int modelRow = table.convertRowIndexToModel(row);
					Rectangle cellRect = table.getCellRect(row, column, false);
					Point relativePoint = new Point(e.getX() - cellRect.x, e.getY() - cellRect.y);

					// Kiểm tra click vào button nào
					if (renderer.isEditButtonClicked(relativePoint)) {
						if (callback != null) {
							callback.onEdit(modelRow);
						}
					} else if (renderer.isDeleteButtonClicked(relativePoint)) {
						if (callback != null) {
							callback.onDelete(modelRow);
						}
					}
				}
			}
		});
	}

	public static ImageIcon loadIcon(String path, int width, int height) {
		try {
			ImageIcon originalIcon = loadIconCandidate(path);
			if (originalIcon.getIconWidth() > 0) {
				Image img = originalIcon.getImage();
				Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
				return new ImageIcon(resizedImg);
			}
		} catch (Exception e) {
			System.err.println("Lỗi tải icon: " + path);
		}
		return null;
	}

	private static ImageIcon loadIconCandidate(String path) {
		if (path == null || path.isBlank()) {
			return new ImageIcon();
		}
		ImageIcon icon = loadFromClasspath(path);
		if (icon != null && icon.getIconWidth() > 0) {
			return icon;
		}
		if (!path.startsWith("/")) {
			icon = loadFromClasspath("/img/" + path);
			if (icon != null && icon.getIconWidth() > 0) {
				return icon;
			}
			icon = loadFromClasspath("/icon/" + path);
			if (icon != null && icon.getIconWidth() > 0) {
				return icon;
			}
		}
		File[] candidates = new File[] {
			new File(path),
			new File("src/main/resources/img", path),
			new File("src/main/resources/icon", path),
			new File("src/main/java/img", path)
		};
		for (File candidate : candidates) {
			if (candidate.exists()) {
				return new ImageIcon(candidate.getAbsolutePath());
			}
		}
		return new ImageIcon(path);
	}

	private static ImageIcon loadFromClasspath(String resourcePath) {
		URL url = TableUtility.class.getResource(resourcePath);
		if (url == null) {
			return null;
		}
		return new ImageIcon(url);
	}

	public interface ActionButtonCallback {
		void onEdit(int modelRow);

		void onDelete(int modelRow);
	}

	/**
	 * Renderer cho các ô thường có hiệu ứng hover
	 */
	static class HoverTableCellRenderer extends DefaultTableCellRenderer {
		private Color oddRowColor;

		public HoverTableCellRenderer(Color oddRowColor) {
			this.oddRowColor = oddRowColor;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (!isSelected) {
				Point mouse = table.getMousePosition();
				if (mouse != null) {
					int hoverRow = table.rowAtPoint(mouse);
					if (hoverRow == row) {
						c.setBackground(DEFAULT_HOVER_ROW);
					} else {
						c.setBackground(row % 2 == 0 ? Color.WHITE : oddRowColor);
					}
				} else {
					c.setBackground(row % 2 == 0 ? Color.WHITE : oddRowColor);
				}
			} else {
				c.setBackground(new Color(92, 188, 247));
			}

			return c;
		}
	}

	/**
	 * Renderer cho cột thao tác với 2 button
	 */
	static class ActionButtonRenderer extends JPanel implements TableCellRenderer {
		private JButton btnEdit;
		private JButton btnDelete;
		private int hoverRow = -1;  // Lưu row đang hover
		private boolean editHover = false;
		private boolean deleteHover = false;

		public ActionButtonRenderer(String editIconPath, String deleteIconPath) {
			setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(0, 2, 0, 2);

			// Button Sửa
			btnEdit = new JButton();
			btnEdit.setIcon(loadIcon(editIconPath, 20, 20));
			btnEdit.setPreferredSize(new Dimension(45, 30));
			btnEdit.setBackground(DEFAULT_EDIT_NORMAL);
			btnEdit.setForeground(Color.WHITE);
			btnEdit.setFocusPainted(false);
			btnEdit.setBorderPainted(false);
			btnEdit.setToolTipText("Sửa");

			// Button Xóa
			btnDelete = new JButton();
			btnDelete.setIcon(loadIcon(deleteIconPath, 20, 20));
			btnDelete.setPreferredSize(new Dimension(45, 30));
			btnDelete.setBackground(DEFAULT_DELETE_NORMAL);
			btnDelete.setForeground(Color.WHITE);
			btnDelete.setFocusPainted(false);
			btnDelete.setBorderPainted(false);
			btnDelete.setToolTipText("Xóa");

			gbc.gridx = 0;
			add(btnEdit, gbc);

			gbc.gridx = 1;
			add(btnDelete, gbc);
		}

		public void updateHover(int row, Point point) {
			// Lưu row đang hover
			hoverRow = row;
			
			// Lấy vị trí thực tế của các button trong panel
			Point editLocation = btnEdit.getLocation();
			Dimension editSize = btnEdit.getSize();
			Point deleteLocation = btnDelete.getLocation();
			Dimension deleteSize = btnDelete.getSize();

			boolean wasEditHover = editHover;
			boolean wasDeleteHover = deleteHover;

			// Kiểm tra hover button Edit dựa trên vị trí thực tế
			editHover = point.x >= editLocation.x && point.x <= editLocation.x + editSize.width
					&& point.y >= editLocation.y && point.y <= editLocation.y + editSize.height;

			// Kiểm tra hover button Delete dựa trên vị trí thực tế
			deleteHover = point.x >= deleteLocation.x && point.x <= deleteLocation.x + deleteSize.width
					&& point.y >= deleteLocation.y && point.y <= deleteLocation.y + deleteSize.height;

			// Cập nhật màu nếu trạng thái thay đổi
			if (editHover != wasEditHover) {
				btnEdit.setBackground(editHover ? DEFAULT_EDIT_HOVER : DEFAULT_EDIT_NORMAL);
			}
			if (deleteHover != wasDeleteHover) {
				btnDelete.setBackground(deleteHover ? DEFAULT_DELETE_HOVER : DEFAULT_DELETE_NORMAL);
			}
		}

		public void clearHover() {
			if (editHover || deleteHover) {
				hoverRow = -1;
				editHover = false;
				deleteHover = false;
				btnEdit.setBackground(DEFAULT_EDIT_NORMAL);
				btnDelete.setBackground(DEFAULT_DELETE_NORMAL);
			}
		}

		public boolean isEditButtonClicked(Point point) {
			Point editLocation = btnEdit.getLocation();
			Dimension editSize = btnEdit.getSize();

			return point.x >= editLocation.x && point.x <= editLocation.x + editSize.width && point.y >= editLocation.y
					&& point.y <= editLocation.y + editSize.height;
		}

		public boolean isDeleteButtonClicked(Point point) {
			Point deleteLocation = btnDelete.getLocation();
			Dimension deleteSize = btnDelete.getSize();

			return point.x >= deleteLocation.x && point.x <= deleteLocation.x + deleteSize.width
					&& point.y >= deleteLocation.y && point.y <= deleteLocation.y + deleteSize.height;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// Chỉ áp dụng hover color cho row đang được hover
			if (row == hoverRow) {
				btnEdit.setBackground(editHover ? DEFAULT_EDIT_HOVER : DEFAULT_EDIT_NORMAL);
				btnDelete.setBackground(deleteHover ? DEFAULT_DELETE_HOVER : DEFAULT_DELETE_NORMAL);
			} else {
				// Reset màu cho các row khác
				btnEdit.setBackground(DEFAULT_EDIT_NORMAL);
				btnDelete.setBackground(DEFAULT_DELETE_NORMAL);
			}
			
			if (isSelected) {
				setBackground(new Color(92, 188, 247));
			} else {
				Point mouse = table.getMousePosition();
				if (mouse != null) {
					int hoverTableRow = table.rowAtPoint(mouse);
					if (hoverTableRow == row) {
						setBackground(DEFAULT_HOVER_ROW);
					} else {
						setBackground(row % 2 == 0 ? Color.WHITE : DEFAULT_ODD_ROW);
					}
				} else {
					setBackground(row % 2 == 0 ? Color.WHITE : DEFAULT_ODD_ROW);
				}
			}
			return this;
		}
	}
	public static void autoResizeRowHeights(JTable table) {
	    for (int row = 0; row < table.getRowCount(); row++) {
	        int rowHeight = table.getRowHeight();
	        
	        for (int column = 0; column < table.getColumnCount(); column++) {
	            Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
	            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
	        }
	        
	        table.setRowHeight(row, rowHeight + 4);
	    }
	}
}