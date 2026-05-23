package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xt_nguyenvongxettuyen")
public class AspirationDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnv")
    private int idnv;

    @Column(name = "nn_cccd", nullable = false)
    private String nnCccd;

    @Column(name = "nv_manganh", nullable = false)
    private String nvManganh;

    @Column(name = "nv_tt", nullable = false)
    private int nvTt;

    @Column(name = "diem_thxt")
    private Double diemThxt;

    @Column(name = "diem_utqd")
    private Double diemUtqd;

    @Column(name = "diem_cong")
    private Double diemCong;

    @Column(name = "diem_xettuyen")
    private Double diemXettuyen;

    @Column(name = "nv_ketqua")
    private String nvKetqua;

    @Column(name = "nv_keys", unique = true)
    private String nvKeys;

    @Column(name = "tt_phuongthuc")
    private String ttPhuongthuc;

    @Column(name = "tt_thm")
    private String ttThm;

    public AspirationDTO() {
    }

    public AspirationDTO(String nnCccd, String nvManganh, int nvTt, Double diemThxt, Double diemUtqd, 
                          Double diemCong, Double diemXettuyen, String nvKetqua, String nvKeys, 
                          String ttPhuongthuc, String ttThm) {
        this.nnCccd = nnCccd;
        this.nvManganh = nvManganh;
        this.nvTt = nvTt;
        this.diemThxt = diemThxt;
        this.diemUtqd = diemUtqd;
        this.diemCong = diemCong;
        this.diemXettuyen = diemXettuyen;
        this.nvKetqua = nvKetqua;
        this.nvKeys = nvKeys;
        this.ttPhuongthuc = ttPhuongthuc;
        this.ttThm = ttThm;
    }

    // --- GETTER & SETTER ---
    public int getIdnv() { return idnv; }
    public void setIdnv(int idnv) { this.idnv = idnv; }

    public String getNnCccd() { return nnCccd; }
    public void setNnCccd(String nnCccd) { this.nnCccd = nnCccd; }

    public String getNvManganh() { return nvManganh; }
    public void setNvManganh(String nvManganh) { this.nvManganh = nvManganh; }

    public int getNvTt() { return nvTt; }
    public void setNvTt(int nvTt) { this.nvTt = nvTt; }

    public Double getDiemThxt() { return diemThxt; }
    public void setDiemThxt(Double diemThxt) { this.diemThxt = diemThxt; }

    public Double getDiemUtqd() { return diemUtqd; }
    public void setDiemUtqd(Double diemUtqd) { this.diemUtqd = diemUtqd; }

    public Double getDiemCong() { return diemCong; }
    public void setDiemCong(Double diemCong) { this.diemCong = diemCong; }

    public Double getDiemXettuyen() { return diemXettuyen; }
    public void setDiemXettuyen(Double diemXettuyen) { this.diemXettuyen = diemXettuyen; }

    public String getNvKetqua() { return nvKetqua; }
    public void setNvKetqua(String nvKetqua) { this.nvKetqua = nvKetqua; }

    public String getNvKeys() { return nvKeys; }
    public void setNvKeys(String nvKeys) { this.nvKeys = nvKeys; }

    public String getTtPhuongthuc() { return ttPhuongthuc; }
    public void setTtPhuongthuc(String ttPhuongthuc) { this.ttPhuongthuc = ttPhuongthuc; }

    public String getTtThm() { return ttThm; }
    public void setTtThm(String ttThm) { this.ttThm = ttThm; }
}
