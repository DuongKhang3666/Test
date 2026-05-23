package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xt_tohop_monthi")
public class SubjectGroupDTO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idtohop")
	private int idToHop;

	@Column(name = "matohop", nullable = false, unique = true)
	private String maToHop;

	@Column(name = "mon1", nullable = false)
	private String mon1;

	@Column(name = "mon2", nullable = false)
	private String mon2;

	@Column(name = "mon3", nullable = false)
	private String mon3;

	@Column(name = "tentohop")
	private String tenToHop;

	// 1. Ham tao rong (Bat buoc cho Hibernate)
	public SubjectGroupDTO() {
	}

	// 2. Ham tao khong co ID (Dung de them moi, ID do DB tu tang)
	public SubjectGroupDTO(String maToHop, String mon1, String mon2, String mon3, String tenToHop) {
		this.maToHop = maToHop;
		this.mon1 = mon1;
		this.mon2 = mon2;
		this.mon3 = mon3;
		this.tenToHop = tenToHop;
	}

	// --- GETTER & SETTER ---
	public int getIdToHop() { return idToHop; }
	public void setIdToHop(int idToHop) { this.idToHop = idToHop; }

	public String getMaToHop() { return maToHop; }
	public void setMaToHop(String maToHop) { this.maToHop = maToHop; }

	public String getMon1() { return mon1; }
	public void setMon1(String mon1) { this.mon1 = mon1; }

	public String getMon2() { return mon2; }
	public void setMon2(String mon2) { this.mon2 = mon2; }

	public String getMon3() { return mon3; }
	public void setMon3(String mon3) { this.mon3 = mon3; }

	public String getTenToHop() { return tenToHop; }
	public void setTenToHop(String tenToHop) { this.tenToHop = tenToHop; }
}
