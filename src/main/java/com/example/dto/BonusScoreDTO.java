package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xt_diemcongxetuyen")
public class BonusScoreDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemcong")
    private int idDiemCong;

    @Column(name = "ts_cccd", nullable = false)
    private String tsCccd;

    @Column(name = "manganh")
    private String maNganh;

    @Column(name = "matohop")
    private String maToHop;

    @Column(name = "phuongthuc")
    private String phuongThuc;

    @Column(name = "diemCC")
    private Double diemCC;

    @Column(name = "diemUtxt")
    private Double diemUtxt;

    @Column(name = "diemTong")
    private Double diemTong;

    @Column(name = "ghichu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "dc_keys", nullable = false)
    private String dcKeys;

    public BonusScoreDTO() {
    }

    public BonusScoreDTO(String tsCccd, String maNganh, String maToHop, String phuongThuc, 
                       Double diemCC, Double diemUtxt, Double diemTong, String ghiChu, String dcKeys) {
        this.tsCccd = tsCccd;
        this.maNganh = maNganh;
        this.maToHop = maToHop;
        this.phuongThuc = phuongThuc;
        this.diemCC = diemCC;
        this.diemUtxt = diemUtxt;
        this.diemTong = diemTong;
        this.ghiChu = ghiChu;
        this.dcKeys = dcKeys;
    }

    public int getIdDiemCong() {
        return idDiemCong;
    }

    public void setIdDiemCong(int idDiemCong) {
        this.idDiemCong = idDiemCong;
    }

    public String getTsCccd() {
        return tsCccd;
    }

    public void setTsCccd(String tsCccd) {
        this.tsCccd = tsCccd;
    }

    public String getMaNganh() {
        return maNganh;
    }

    public void setMaNganh(String maNganh) {
        this.maNganh = maNganh;
    }

    public String getMaToHop() {
        return maToHop;
    }

    public void setMaToHop(String maToHop) {
        this.maToHop = maToHop;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public Double getDiemCC() {
        return diemCC;
    }

    public void setDiemCC(Double diemCC) {
        this.diemCC = diemCC;
    }

    public Double getDiemUtxt() {
        return diemUtxt;
    }

    public void setDiemUtxt(Double diemUtxt) {
        this.diemUtxt = diemUtxt;
    }

    public Double getDiemTong() {
        return diemTong;
    }

    public void setDiemTong(Double diemTong) {
        this.diemTong = diemTong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getDcKeys() {
        return dcKeys;
    }

    public void setDcKeys(String dcKeys) {
        this.dcKeys = dcKeys;
    }
}