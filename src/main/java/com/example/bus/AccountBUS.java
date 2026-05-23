package com.example.bus;

import java.util.List;
import com.example.dao.AccountDAO;
import com.example.dto.AccountDTO;

public class AccountBUS {
	private final AccountDAO accountDAO;
	private static final String USERNAME_REGEX = "^[A-Za-z0-9._]{3,50}$";

	public AccountBUS() {
		this.accountDAO = new AccountDAO();
	}

	public AccountDTO login(String username, String password) {
		return accountDAO.findByUsernameAndPassword(username, password);
	}

	public java.util.List<AccountDTO> getAll() {
		return accountDAO.getAll();
	}

	public java.util.List<AccountDTO> getAllWithPagination(int offset, int limit) {
		return accountDAO.getAllWithPagination(offset, limit);
	}

	public long getCount() {
		return accountDAO.getCount();
	}

	public long getSearchCount(String keyword) {
		return accountDAO.getSearchCount(keyword);
	}

	public java.util.List<AccountDTO> searchByUsernameOrName(String keyword, int offset, int limit) {
		return accountDAO.searchByUsernameOrName(keyword, offset, limit);
	}

	public AccountDTO getById(int id) {
	    return accountDAO.getById(id);
	}

	public AccountDTO findByUsername(String username) {
	    if (username == null || username.trim().isEmpty()) {
	        return null;
	    }
	    return accountDAO.findByUsername(username.trim());
	}

	public void save(AccountDTO account) {
	    validateForSave(account);
	    accountDAO.save(account);
	}

	public void update(AccountDTO account) {
	    validateForUpdate(account);
	    accountDAO.update(account);
	}

	public void delete(AccountDTO account) {
	    accountDAO.delete(account);
	}

	public void ensureDefaultAdmin() {
		AccountDTO existingAdmin = accountDAO.findByUsername("admin");
		if (existingAdmin != null) {
			return;
		}

		AccountDTO defaultAdmin = new AccountDTO(
				"admin",
				"123",
				"Administrator",
				"Admin",
				true
		);
		accountDAO.save(defaultAdmin);
	}

	private void validateForSave(AccountDTO account) {
		validateCommon(account);

		String password = account.getPassword();
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Mật khẩu không được để trống.");
		}
		if (password.length() < 6) {
			throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
		}

		AccountDTO existing = accountDAO.findByUsername(account.getUsername().trim());
		if (existing != null) {
			throw new IllegalArgumentException("Username đã tồn tại.");
		}
	}

	private void validateForUpdate(AccountDTO account) {
		validateCommon(account);

		if (account.getId() <= 0) {
			throw new IllegalArgumentException("ID tài khoản không hợp lệ.");
		}

		AccountDTO existing = accountDAO.findByUsername(account.getUsername().trim());
		if (existing != null && existing.getId() != account.getId()) {
			throw new IllegalArgumentException("Username đã tồn tại.");
		}
	}

	private void validateCommon(AccountDTO account) {
		if (account == null) {
			throw new IllegalArgumentException("Dữ liệu tài khoản không hợp lệ.");
		}

		String username = account.getUsername() != null ? account.getUsername().trim() : "";
		if (username.isEmpty()) {
			throw new IllegalArgumentException("Username không được để trống.");
		}
		if (!username.matches(USERNAME_REGEX)) {
			throw new IllegalArgumentException("Username chỉ được chứa chữ, số, dấu chấm, gạch dưới và phải có ít nhất 3 ký tự.");
		}

		String fullName = account.getHoTen() != null ? account.getHoTen().trim() : "";
		if (fullName.isEmpty()) {
			throw new IllegalArgumentException("Họ tên không được để trống.");
		}

		String role = account.getRole() != null ? account.getRole().trim() : "";
		if (role.isEmpty()) {
			throw new IllegalArgumentException("Vai trò không hợp lệ.");
		}
		if (!("User".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role))) {
			throw new IllegalArgumentException("Vai trò chỉ được là User hoặc Admin.");
		}
	}
}
