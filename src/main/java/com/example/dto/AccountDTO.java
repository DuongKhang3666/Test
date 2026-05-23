package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tai_khoan")
public class AccountDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "role")
    private String role; 

    @Column(name = "is_active")
    private boolean isActive; 

    // 1. Hàm tạo rỗng (Bắt buộc cho Hibernate)
    public AccountDTO() {
    }

    // 2. Hàm tạo không có ID (Dùng để thêm mới, ID do DB tự tăng)
    public AccountDTO(String username, String password, String hoTen, String role, boolean isActive) {
        this.username = username;
        this.password = password;
        this.hoTen = hoTen;
        this.role = role;
        this.isActive = isActive;
    }

    // --- GETTER & SETTER ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}