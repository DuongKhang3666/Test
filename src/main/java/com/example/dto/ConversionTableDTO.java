package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_bangquydoi")
public class ConversionTableDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idqd;

    @Column(name = "d_phuongthuc")
    private String phuongthuc;

    @Column(name = "d_tohop")
    private String tohop;

    @Column(name = "d_mon")
    private String mon;

    @Column(name = "d_diema")
    private BigDecimal diemA;

    @Column(name = "d_diemb")
    private BigDecimal diemB;

    @Column(name = "d_diemc")
    private BigDecimal diemC;

    @Column(name = "d_diemd")
    private BigDecimal diemD;

    @Column(name = "d_maquydoi")
    private String maQuydoi;

    @Column(name = "d_phanvi")
    private String phanvi;

    public ConversionTableDTO() {
    }

    public ConversionTableDTO(String phuongthuc, String tohop, String mon, BigDecimal diemA,
                              BigDecimal diemB, BigDecimal diemC, BigDecimal diemD,
                              String maQuydoi, String phanvi) {
        this.phuongthuc = phuongthuc;
        this.tohop = tohop;
        this.mon = mon;
        this.diemA = diemA;
        this.diemB = diemB;
        this.diemC = diemC;
        this.diemD = diemD;
        this.maQuydoi = maQuydoi;
        this.phanvi = phanvi;
    }

    public int getIdqd() { return idqd; }
    public void setIdqd(int idqd) { this.idqd = idqd; }

    public String getPhuongthuc() { return phuongthuc; }
    public void setPhuongthuc(String phuongthuc) { this.phuongthuc = phuongthuc; }

    public String getTohop() { return tohop; }
    public void setTohop(String tohop) { this.tohop = tohop; }

    public String getMon() { return mon; }
    public void setMon(String mon) { this.mon = mon; }

    public BigDecimal getDiemA() { return diemA; }
    public void setDiemA(BigDecimal diemA) { this.diemA = diemA; }

    public BigDecimal getDiemB() { return diemB; }
    public void setDiemB(BigDecimal diemB) { this.diemB = diemB; }

    public BigDecimal getDiemC() { return diemC; }
    public void setDiemC(BigDecimal diemC) { this.diemC = diemC; }

    public BigDecimal getDiemD() { return diemD; }
    public void setDiemD(BigDecimal diemD) { this.diemD = diemD; }

    public String getMaQuydoi() { return maQuydoi; }
    public void setMaQuydoi(String maQuydoi) { this.maQuydoi = maQuydoi; }

    public String getPhanvi() { return phanvi; }
    public void setPhanvi(String phanvi) { this.phanvi = phanvi; }
}
