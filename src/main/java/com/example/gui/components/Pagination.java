package com.example.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Pagination extends JPanel {
	private final JPanel pagesPanel;
	private final JPanel leftNavPanel;
	private final JPanel rightNavPanel;

	private final PaginationItem firstItem;
	private final PaginationItem prevItem;
	private final PaginationItem nextItem;
	private final PaginationItem lastItem;

	private int totalPages = 1;
	private int currentPage = 1;
	private int totalItems = 0;
	private int pageSize = 10;

	private final List<IntConsumer> listeners = new ArrayList<>();

	public Pagination() {
		setOpaque(false);
		// Dùng BorderLayout để cụm nút điều hướng trái/phải không bị "nhảy"
		// khi số token trang thay đổi (do thêm/bớt dấu ...).
		setLayout(new BorderLayout(10, 0));

		pagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		pagesPanel.setOpaque(false);
		pagesPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

		leftNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		leftNavPanel.setOpaque(false);
		leftNavPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

		rightNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		rightNavPanel.setOpaque(false);
		rightNavPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

		firstItem = new PaginationItem(loadIcon("/assets/images/double_arrow_left.png"), 16);
		prevItem = new PaginationItem(loadIcon("/assets/images/arrow_left.png"), 16);
		nextItem = new PaginationItem(loadIcon("/assets/images/arrow_right.png"), 16);
		lastItem = new PaginationItem(loadIcon("/assets/images/double_arrow_right.png"), 16);

		firstItem.addActionListener(e -> setCurrentPage(1));
		prevItem.addActionListener(e -> setCurrentPage(currentPage - 1));
		nextItem.addActionListener(e -> setCurrentPage(currentPage + 1));
		lastItem.addActionListener(e -> setCurrentPage(totalPages));

		leftNavPanel.add(firstItem);
		leftNavPanel.add(prevItem);

		rightNavPanel.add(nextItem);
		rightNavPanel.add(lastItem);

		add(leftNavPanel, BorderLayout.WEST);
		add(pagesPanel, BorderLayout.CENTER);
		add(rightNavPanel, BorderLayout.EAST);

		rebuildPages();
		syncNavEnabled();
	}

	public Pagination(int totalPages, int currentPage) {
		this();
		setTotalPages(totalPages);
		setCurrentPage(currentPage);
	}

	// Tiện ích: set tổng item + kích thước trang, tự tính số trang.
	public Pagination(int totalItems, int pageSize, int currentPage) {
		this();
		setTotalItems(totalItems, pageSize);
		setCurrentPage(currentPage);
	}

	public int getTotalPages() {
		return totalPages;
	}

	public int getTotalItems() {
		return totalItems;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setTotalItems(int totalItems, int pageSize) {
		this.totalItems = Math.max(0, totalItems);
		this.pageSize = Math.max(1, pageSize);
		int pages = (this.totalItems + this.pageSize - 1) / this.pageSize;
		setTotalPages(Math.max(1, pages));
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = Math.max(1, totalPages);
		if (currentPage > this.totalPages) currentPage = this.totalPages;
		rebuildPages();
		syncNavEnabled();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int page) {
		int newPage = clamp(page, 1, totalPages);
		if (newPage == currentPage) {
			// Giữ trạng thái selected đồng bộ khi gọi set lại cùng số trang.
			updateSelected();
			return;
		} 
		currentPage = newPage;
		rebuildPages();
		syncNavEnabled();
		firePageChanged(currentPage);
	}

	// Đổi trang nhưng không bắn listener (tránh loop khi đang đồng bộ UI theo filter/search).
	public void setCurrentPageSilently(int page) {
		int newPage = clamp(page, 1, totalPages);
		if (newPage == currentPage) {
			updateSelected();
			return;
		}
		currentPage = newPage;
		updateSelected();
		syncNavEnabled();
		// Không gọi firePageChanged.
	}

	public void addPageChangeListener(IntConsumer listener) {
		if (listener != null) listeners.add(listener);
	}

	private void firePageChanged(int page) {
		for (IntConsumer listener : listeners) {
			listener.accept(page);
		}
	}

	private void rebuildPages() {
		pagesPanel.removeAll();

		for (PageToken token : computeTokens(totalPages, currentPage)) {
			if (token.isEllipsis) {
				PaginationItem dots = new PaginationItem("...");
				dots.setEnabled(false);
				pagesPanel.add(dots);
				continue;
			}

			int page = token.page;
			PaginationItem item = new PaginationItem(String.valueOf(page));
			item.setSelectedItem(page == currentPage);
			item.addActionListener(e -> setCurrentPage(page));
			pagesPanel.add(item);
		}

		applyStablePagesPanelWidth();

		revalidate();
		repaint();
	}

	// Giữ width ổn định cho khu vực số trang để bố cục không bị giật khi token thay đổi.
	private void applyStablePagesPanelWidth() {
		if (!(pagesPanel.getLayout() instanceof FlowLayout flow)) return;

		int stableSlots = getStableTokenSlots(totalPages);
		// Dùng width chuẩn của PaginationItem để tính chiều rộng ổn định.
		int slotW = PaginationItem.getTextMinWidth();
		Insets insets = pagesPanel.getInsets();
		int hgap = flow.getHgap();
		int width = insets.left + insets.right + stableSlots * slotW + Math.max(0, stableSlots - 1) * hgap + hgap * 2;

		Dimension pref = pagesPanel.getPreferredSize();
		pagesPanel.setPreferredSize(new Dimension(width, pref.height));
	}

	private static int getStableTokenSlots(int totalPages) {
		int windowSize = 3;
		int headEdge = 3;
		int tailEdge = 3;
		int maxTokensWithoutEllipsis = headEdge + windowSize + tailEdge; // 9
		if (totalPages <= maxTokensWithoutEllipsis) return Math.max(1, totalPages);
		return maxTokensWithoutEllipsis + 2; // +2 ellipsis slots => 11
	}

	private void updateSelected() {
		for (int i = 0; i < pagesPanel.getComponentCount(); i++) {
			if (pagesPanel.getComponent(i) instanceof PaginationItem item) {
				String text = item.getText();
				int page = safeParseInt(text, -1);
				item.setSelectedItem(page == currentPage);
			}
		}
	}

	private void syncNavEnabled() {
		firstItem.setEnabled(currentPage > 1);
		prevItem.setEnabled(currentPage > 1);
		nextItem.setEnabled(currentPage < totalPages);
		lastItem.setEnabled(currentPage < totalPages);
		syncVisibility();
	}

	private void syncVisibility() {
		boolean show = totalPages > 1;
		leftNavPanel.setVisible(show);
		rightNavPanel.setVisible(show);
		pagesPanel.setVisible(show);
		revalidate();
		repaint();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static int safeParseInt(String s, int fallback) {
		try {
			return Integer.parseInt(s);
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private static class PageToken {
		final boolean isEllipsis;
		final int page;

		private PageToken(boolean isEllipsis, int page) {
			this.isEllipsis = isEllipsis;
			this.page = page;
		}

		static PageToken page(int page) {
			return new PageToken(false, page);
		}

		static PageToken ellipsis() {
			return new PageToken(true, -1);
		}
	}

	private static List<PageToken> computeTokens(int total, int current) {
		List<PageToken> out = new ArrayList<>();
		int windowSize = 3; // số trang hiển thị trong "cửa sổ" xung quanh trang hiện tại
		int headEdge = 3;   // luôn hiển thị N trang đầu tiên
		int tailEdge = 3;   // luôn hiển thị N trang cuối cùng

		// Nếu tổng trang nhỏ hơn hoặc bằng số trang có thể hiển thị mà không cần '...', thì hiển thị tất cả trang và không dùng ellipsis.
		int maxTokensWithoutEllipsis = headEdge + windowSize + tailEdge;
		if (total <= Math.max(windowSize, maxTokensWithoutEllipsis)) {
			for (int i = 1; i <= total; i++) out.add(PageToken.page(i));
			return out;
		}

		boolean[] show = new boolean[total + 1];

		// Head
		for (int i = 1; i <= Math.min(headEdge, total); i++) {
			show[i] = true;
		}

		// Tail
		int tailStart = Math.max(1, total - tailEdge + 1);
		for (int i = tailStart; i <= total; i++) {
			show[i] = true;
		}

		// Cửa sổ trang xung quanh current.
		int half = windowSize / 2;
		int windowStart = clamp(current - half, 1, total);
		int windowEnd = clamp(windowStart + windowSize - 1, 1, total);
		windowStart = clamp(windowEnd - windowSize + 1, 1, total);
		for (int i = windowStart; i <= windowEnd; i++) {
			show[i] = true;
		}

		int lastShown = 0;
		for (int i = 1; i <= total; i++) {
			if (!show[i]) continue;
			if (lastShown != 0 && i - lastShown > 1) out.add(PageToken.ellipsis());
			out.add(PageToken.page(i));
			lastShown = i;
		}

		return out;
	}

	private ImageIcon loadIcon(String resourcePath) {
		if (resourcePath == null || resourcePath.isBlank()) return null;
		URL url = getClass().getResource(resourcePath);
		if (url == null) {
			System.err.println("Không tìm thấy icon tại: " + resourcePath);
			return null;
		}
		return new ImageIcon(url);
	}
}
