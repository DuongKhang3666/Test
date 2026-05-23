package com.example.bus;

import com.example.dao.MajorDAO;
import com.example.dao.MajorGroupDAO;
import com.example.dao.SubjectGroupDAO;
import com.example.dto.MajorDTO;
import com.example.dto.MajorGroupDTO;
import com.example.dto.SubjectGroupDTO;

import java.util.List;

public class SubjectGroupBUS {
    private final SubjectGroupDAO subjectGroupDAO;
    private final MajorGroupDAO majorGroupDAO;
    private final MajorDAO majorDAO;

    public SubjectGroupBUS() {
        this.subjectGroupDAO = new SubjectGroupDAO();
        this.majorGroupDAO = new MajorGroupDAO();
        this.majorDAO = new MajorDAO();
    }

    public List<SubjectGroupDTO> getAll() {
        return subjectGroupDAO.getAll();
    }

    public SubjectGroupDTO findById(int idToHop) {
        return subjectGroupDAO.findById(idToHop);
    }

    public SubjectGroupDTO findByMaToHop(String maToHop) {
        return subjectGroupDAO.findByMaToHop(maToHop);
    }

    public void save(SubjectGroupDTO subjectGroup) {
        subjectGroupDAO.save(subjectGroup);
    }

    public void update(SubjectGroupDTO subjectGroup) {
        subjectGroupDAO.update(subjectGroup);
    }

    /**
     * Delete a SubjectGroup and all MajorGroup rows that reference it.
     * This ensures xt_tohop_monthi is fully removed even when majors reference the combo.
     */
    public void delete(SubjectGroupDTO subjectGroup) {
        if (subjectGroup == null) return;

        String maToHop = subjectGroup.getMaToHop();
        if (maToHop != null && !maToHop.isBlank()) {
            
            // Bước 1: Xóa toàn bộ mapping trong bảng xt_nganh_tohop
            try {
                List<MajorGroupDTO> refs = majorGroupDAO.findByMaToHop(maToHop);
                if (refs != null) {
                    for (MajorGroupDTO mg : refs) {
                        try {
                            majorGroupDAO.delete(mg);
                        } catch (Exception ex) {
                            System.err.println("Lỗi khi xóa MajorGroup chứa " + maToHop + ": " + ex.getMessage());
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Lỗi truy xuất MajorGroup cho " + maToHop + ": " + ex.getMessage());
            }

            // Bước 2: Gỡ bỏ n_tohopgoc = 'A05' trong bảng xt_nganh (để tránh lỗi Foreign Key MySQL)
            try {
                List<MajorDTO> allMajors = majorDAO.getAll();
                for (MajorDTO major : allMajors) {
                    if (maToHop.equalsIgnoreCase(major.getNToHopGoc())) {
                        major.setNToHopGoc(null); // Gỡ thành rỗng
                        majorDAO.update(major);
                    }
                }
            } catch (Exception ex) {
                 System.err.println("Lỗi khi gỡ n_tohopgoc trong bảng ngành: " + ex.getMessage());
            }
        }

        // Bước 3: Cuối cùng, xóa tận gốc tổ hợp trong xt_tohop_monthi
        subjectGroupDAO.delete(subjectGroup);
    }
}
