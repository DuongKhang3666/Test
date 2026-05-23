package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "xt_diemthixettuyen")
public class ExamScoreDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddiemthi")
    private int idDiemThi;

    // Tìm đến dòng này và sửa thành:
    @Column(name = "cccd", nullable = false, length = 20)
    private String cccd;

    @Column(name = "sobaodanh", length = 45)
    private String soBaoDanh;

    @Column(name = "d_phuongthuc", length = 10)
    private String phuongThuc;

    // ================================================================
    // ĐIỂM CÁC MÔN THI THPT / VSAT
    // ================================================================

    /** Toán */
    @Column(name = "`TO`", precision = 8, scale = 2)
    private Double diemToan;

    @Column(name = "`LI`", precision = 8, scale = 2)
    private Double diemLy;

    @Column(name = "`HO`", precision = 8, scale = 2)
    private Double diemHoa;

    @Column(name = "`SI`", precision = 8, scale = 2)
    private Double diemSinh;

    @Column(name = "`SU`", precision = 8, scale = 2)
    private Double diemSu;

    @Column(name = "`DI`", precision = 8, scale = 2)
    private Double diemDia;

    @Column(name = "`VA`", precision = 8, scale = 2)
    private Double diemVan;

    @Column(name = "`TI`", precision = 8, scale = 2)
    private Double diemTin;

    @Column(name = "`KTPL`", precision = 8, scale = 2)
    private Double diemKtpl;

    @Column(name = "`CNCN`", precision = 8, scale = 2)
    private Double diemCncn;

    @Column(name = "`CNNN`", precision = 8, scale = 2)
    private Double diemCnnn;

    @Column(name = "`N1_THI`", precision = 8, scale = 2)
    private Double n1Thi;

    @Column(name = "`N1_CC`", precision = 8, scale = 2)
    private Double n1Cc;

    @Column(name = "`NL1`", precision = 8, scale = 2)
    private Double nl1;

    @Column(name = "`NK1`", precision = 8, scale = 2)
    private Double nk1;

    @Column(name = "`NK2`", precision = 8, scale = 2)
    private Double nk2;

    // ==================== SOFT DELETE ====================
    @Column(name = "is_active")
    private Boolean isActive = true;

    public ExamScoreDTO() {}

    public ExamScoreDTO(String cccd, String soBaoDanh, String phuongThuc) {
        this.cccd       = cccd;
        this.soBaoDanh  = soBaoDanh;
        this.phuongThuc = phuongThuc;
        this.isActive = true;
    }

    public int getIdDiemThi()                   { return idDiemThi; }
    public void setIdDiemThi(int v)             { this.idDiemThi = v; }

    public String getCccd()                     { return cccd; }
    public void setCccd(String v)               { this.cccd = v; }

    public String getSoBaoDanh()                { return soBaoDanh; }
    public void setSoBaoDanh(String v)          { this.soBaoDanh = v; }

    public String getPhuongThuc()               { return phuongThuc; }
    public void setPhuongThuc(String v)         { this.phuongThuc = v; }

    public Double getDiemToan()                 { return diemToan; }
    public void setDiemToan(Double v)           { this.diemToan = v; }

    public Double getDiemLy()                   { return diemLy; }
    public void setDiemLy(Double v)             { this.diemLy = v; }

    public Double getDiemHoa()                  { return diemHoa; }
    public void setDiemHoa(Double v)            { this.diemHoa = v; }

    public Double getDiemSinh()                 { return diemSinh; }
    public void setDiemSinh(Double v)           { this.diemSinh = v; }

    public Double getDiemSu()                   { return diemSu; }
    public void setDiemSu(Double v)             { this.diemSu = v; }

    public Double getDiemDia()                  { return diemDia; }
    public void setDiemDia(Double v)            { this.diemDia = v; }

    public Double getDiemVan()                  { return diemVan; }
    public void setDiemVan(Double v)            { this.diemVan = v; }

    public Double getDiemTin()                  { return diemTin; }
    public void setDiemTin(Double v)            { this.diemTin = v; }

    public Double getDiemKtpl()                 { return diemKtpl; }
    public void setDiemKtpl(Double v)           { this.diemKtpl = v; }

    public Double getDiemCncn()                 { return diemCncn; }
    public void setDiemCncn(Double v)           { this.diemCncn = v; }

    public Double getDiemCnnn()                 { return diemCnnn; }
    public void setDiemCnnn(Double v)           { this.diemCnnn = v; }

    public Double getN1Thi()                    { return n1Thi; }
    public void setN1Thi(Double v)              { this.n1Thi = v; }

    public Double getN1Cc()                     { return n1Cc; }
    public void setN1Cc(Double v)               { this.n1Cc = v; }

    public Double getNl1()                      { return nl1; }
    public void setNl1(Double v)                { this.nl1 = v; }

    public Double getNk1()                      { return nk1; }
    public void setNk1(Double v)                { this.nk1 = v; }

    public Double getNk2()                      { return nk2; }
    public void setNk2(Double v)                { this.nk2 = v; }

    // SOFT DELETE
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    @Override
    public String toString() {
        return "ExamScoreDTO{idDiemThi=" + idDiemThi
                + ", cccd='" + cccd
                + "', phuongThuc='" + phuongThuc
                + "', TO=" + diemToan
                + ", N1_CC=" + n1Cc
                + ", isActive=" + isActive + "}";
    }
}