package com.example.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "xt_nganh")
public class MajorDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnganh")
    private int idNganh;

    @Column(name = "manganh", nullable = false)
    private String maNganh;

    @Column(name = "tennganh", nullable = false)
    private String tenNganh;

    @Column(name = "n_tohopgoc")
    private String nToHopGoc;

    @Column(name = "n_chitieu", nullable = false)
    private int nChiTieu;

    @Column(name = "n_diemsan")
    private BigDecimal nDiemSan;

    @Column(name = "n_diemtrungtuyen")
    private BigDecimal nDiemTrungTuyen;

    @Column(name = "n_tuyenthang")
    private String nTuyenThang;

    @Column(name = "n_dgnl")
    private String nDgnl;

    @Column(name = "n_thpt")
    private String nThpt;

    @Column(name = "n_vsat")
    private String nVsat;

    @Column(name = "sl_xtt")
    private Integer slXtt;

    @Column(name = "sl_dgnl")
    private Integer slDgnl;

    @Column(name = "sl_vsat")
    private Integer slVsat;

    @Column(name = "sl_thpt")
    private String slThpt;

    // 1. Ham tao rong (Bat buoc cho Hibernate)
    public MajorDTO() {
    }

    // 2. Ham tao khong co ID (Dung de them moi, ID do DB tu tang)
    public MajorDTO(String maNganh, String tenNganh, String nToHopGoc, int nChiTieu,
                    BigDecimal nDiemSan, BigDecimal nDiemTrungTuyen, String nTuyenThang,
                    String nDgnl, String nThpt, String nVsat, Integer slXtt,
                    Integer slDgnl, Integer slVsat, String slThpt) {
        this.maNganh = maNganh;
        this.tenNganh = tenNganh;
        this.nToHopGoc = nToHopGoc;
        this.nChiTieu = nChiTieu;
        this.nDiemSan = nDiemSan;
        this.nDiemTrungTuyen = nDiemTrungTuyen;
        this.nTuyenThang = nTuyenThang;
        this.nDgnl = nDgnl;
        this.nThpt = nThpt;
        this.nVsat = nVsat;
        this.slXtt = slXtt;
        this.slDgnl = slDgnl;
        this.slVsat = slVsat;
        this.slThpt = slThpt;
    }

    // --- GETTER & SETTER ---
    public int getIdNganh() { return idNganh; }
    public void setIdNganh(int idNganh) { this.idNganh = idNganh; }

    public String getMaNganh() { return maNganh; }
    public void setMaNganh(String maNganh) { this.maNganh = maNganh; }

    public String getTenNganh() { return tenNganh; }
    public void setTenNganh(String tenNganh) { this.tenNganh = tenNganh; }

    public String getNToHopGoc() { return nToHopGoc; }
    public void setNToHopGoc(String nToHopGoc) { this.nToHopGoc = nToHopGoc; }

    public int getNChiTieu() { return nChiTieu; }
    public void setNChiTieu(int nChiTieu) { this.nChiTieu = nChiTieu; }

    public BigDecimal getNDiemSan() { return nDiemSan; }
    public void setNDiemSan(BigDecimal nDiemSan) { this.nDiemSan = nDiemSan; }

    public BigDecimal getNDiemTrungTuyen() { return nDiemTrungTuyen; }
    public void setNDiemTrungTuyen(BigDecimal nDiemTrungTuyen) { this.nDiemTrungTuyen = nDiemTrungTuyen; }

    public String getNTuyenThang() { return nTuyenThang; }
    public void setNTuyenThang(String nTuyenThang) { this.nTuyenThang = nTuyenThang; }

    public String getNDgnl() { return nDgnl; }
    public void setNDgnl(String nDgnl) { this.nDgnl = nDgnl; }

    public String getNThpt() { return nThpt; }
    public void setNThpt(String nThpt) { this.nThpt = nThpt; }

    public String getNVsat() { return nVsat; }
    public void setNVsat(String nVsat) { this.nVsat = nVsat; }

    public Integer getSlXtt() { return slXtt; }
    public void setSlXtt(Integer slXtt) { this.slXtt = slXtt; }

    public Integer getSlDgnl() { return slDgnl; }
    public void setSlDgnl(Integer slDgnl) { this.slDgnl = slDgnl; }

    public Integer getSlVsat() { return slVsat; }
    public void setSlVsat(Integer slVsat) { this.slVsat = slVsat; }

    public String getSlThpt() { return slThpt; }
    public void setSlThpt(String slThpt) { this.slThpt = slThpt; }
}
