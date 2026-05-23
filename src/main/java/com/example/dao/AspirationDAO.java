package com.example.dao;

import com.example.dto.AspirationDTO;
import com.example.utils.HibernateUtil;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class AspirationDAO {

    // ----------------------------------------------------------------
    // CRUD CƠ BẢN
    // ----------------------------------------------------------------

    public List<AspirationDTO> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from AspirationDTO", AspirationDTO.class).list();
        }
    }

    public AspirationDTO findById(int idnv) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(AspirationDTO.class, idnv);
        }
    }

    public AspirationDTO findByNvKeys(String nvKeys) {
        if (nvKeys == null || nvKeys.isBlank()) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a where a.nvKeys = :keys",
                    AspirationDTO.class)
                    .setParameter("keys", nvKeys.trim())
                    .uniqueResult();
        }
    }

    public List<AspirationDTO> findByCccd(String cccd) {
        if (cccd == null || cccd.isBlank()) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a where a.nnCccd = :cccd order by a.nvTt asc",
                    AspirationDTO.class)
                    .setParameter("cccd", cccd.trim())
                    .list();
        }
    }

    public List<AspirationDTO> findByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a where a.nvManganh = :ma order by a.nvTt asc",
                    AspirationDTO.class)
                    .setParameter("ma", maNganh.trim())
                    .list();
        }
    }

    public void save(AspirationDTO aspiration) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(aspiration);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void update(AspirationDTO aspiration) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(aspiration);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void delete(AspirationDTO aspiration) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(aspiration);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public long getCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "select count(a) from AspirationDTO a", Long.class).uniqueResult();
            return count != null ? count : 0L;
        }
    }

    // ----------------------------------------------------------------
    // PHÂN TRANG & TÌM KIẾM
    // ----------------------------------------------------------------

    public List<AspirationDTO> getAllWithPagination(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a order by a.nvManganh asc, a.nvTt asc",
                    AspirationDTO.class)
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }

    public List<AspirationDTO> searchByCccdOrMaNganh(String keyword, int offset, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return getAllWithPagination(offset, limit);
        }
        String kw = "%" + keyword.trim() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a " +
                    "where a.nnCccd like :kw or a.nvManganh like :kw " +
                    "order by a.nvManganh asc, a.nvTt asc",
                    AspirationDTO.class)
                    .setParameter("kw", kw)
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }

    public long countSearch(String keyword) {
        if (keyword == null || keyword.isBlank()) return getCount();
        String kw = "%" + keyword.trim() + "%";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "select count(a) from AspirationDTO a " +
                    "where a.nnCccd like :kw or a.nvManganh like :kw",
                    Long.class)
                    .setParameter("kw", kw)
                    .uniqueResult();
            return count != null ? count : 0L;
        }
    }

    // ----------------------------------------------------------------
    // CHO THUẬT TOÁN XÉT TUYỂN
    // ----------------------------------------------------------------

    /**
     * Lấy TOÀN BỘ danh sách nguyện vọng đã được sắp xếp để đưa vào vòng lặp
     * xét tuyển.
     *
     * Thứ tự ưu tiên (quan trọng — quyết định ai được xét trước):
     *   1. nvTt ASC          — Nguyện vọng ưu tiên nhỏ hơn được xét trước (NV1 > NV2 > …)
     *   2. diem_xettuyen DESC — Trong cùng thứ tự nguyện vọng, thí sinh điểm cao hơn được xét trước
     *   3. nnCccd ASC        — Tie-break cuối cùng theo CCCD để kết quả ổn định (deterministic)
     *
     * Chỉ lấy những nguyện vọng chưa có kết quả (nvKetqua IS NULL hoặc rỗng)
     * để tránh xử lý lại những hồ sơ đã trúng/rớt.
     */
    public List<AspirationDTO> getAllSortedForAdmission() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a " +
                    "where (a.nvKetqua is null or trim(a.nvKetqua) = '') " +
                    "order by a.nvTt asc, " +
                    "         coalesce(a.diemXettuyen, 0) desc, " +
                    "         a.nnCccd asc",
                    AspirationDTO.class)
                    .list();
        }
    }

    /**
     * Lấy danh sách nguyện vọng theo ngành cụ thể, đã sắp xếp giảm dần theo
     * điểm xét tuyển. Dùng khi cần kiểm tra chỉ tiêu từng ngành độc lập.
     *
     * @param maNganh mã ngành cần lấy
     */
    public List<AspirationDTO> getSortedByMaNganhForAdmission(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a " +
                    "where a.nvManganh = :ma " +
                    "  and (a.nvKetqua is null or trim(a.nvKetqua) = '') " +
                    "order by coalesce(a.diemXettuyen, 0) desc, " +
                    "         a.nvTt asc, " +
                    "         a.nnCccd asc",
                    AspirationDTO.class)
                    .setParameter("ma", maNganh.trim())
                    .list();
        }
    }

    /**
     * Lấy danh sách nguyện vọng cả đã có và chưa có kết quả, sắp xếp để
     * review / báo cáo sau khi chạy xét tuyển xong.
     */
    public List<AspirationDTO> getAllSortedForReview() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a " +
                    "order by a.nvManganh asc, " +
                    "         coalesce(a.diemXettuyen, 0) desc, " +
                    "         a.nvTt asc",
                    AspirationDTO.class)
                    .list();
        }
    }

    // ----------------------------------------------------------------
    // CẬP NHẬT KẾT QUẢ
    // ----------------------------------------------------------------

    /**
     * Cập nhật nhanh trạng thái kết quả của một nguyện vọng.
     *
     * Đây là hàm được gọi liên tục trong vòng lặp xét tuyển (hàng chục nghìn lần),
     * nên dùng HQL UPDATE trực tiếp thay vì load entity → sửa → save để tránh overhead.
     *
     * @param aspirationId id của dòng nguyện vọng (idnv)
     * @param status       "ĐẬU" | "RỚT" | hoặc chuỗi khác theo nghiệp vụ
     * @return true nếu cập nhật thành công ít nhất 1 dòng
     */
    public boolean updateResultStatus(int aspirationId, String status) {
        if (aspirationId <= 0) return false;
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createQuery(
                    "update AspirationDTO a " +
                    "set a.nvKetqua = :status " +
                    "where a.idnv = :id")
                    .setParameter("status", status)
                    .setParameter("id", aspirationId)
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    /**
     * Cập nhật kết quả kèm theo điểm xét tuyển đã tính toán — dùng khi cần
     * ghi lại toàn bộ kết quả tính điểm của một lần chạy xét tuyển.
     *
     * @param aspirationId   id nguyện vọng
     * @param status         "ĐẬU" | "RỚT"
     * @param diemXettuyen   tổng điểm xét tuyển đã cộng ưu tiên
     * @param diemThxt       điểm tổ hợp xét tuyển (chưa cộng ưu tiên)
     * @param diemCong       điểm cộng (chứng chỉ + giải thưởng)
     * @param diemUtqd       điểm ưu tiên theo quy định
     * @return true nếu cập nhật thành công
     */
    public boolean updateResultFull(int aspirationId, String status,
                                    double diemXettuyen, double diemThxt,
                                    double diemCong, double diemUtqd) {
        if (aspirationId <= 0) return false;
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createQuery(
                    "update AspirationDTO a " +
                    "set a.nvKetqua    = :status, " +
                    "    a.diemXettuyen = :dxt, " +
                    "    a.diemThxt     = :dthxt, " +
                    "    a.diemCong     = :dcong, " +
                    "    a.diemUtqd     = :dutqd " +
                    "where a.idnv = :id")
                    .setParameter("status", status)
                    .setParameter("dxt",   diemXettuyen)
                    .setParameter("dthxt", diemThxt)
                    .setParameter("dcong", diemCong)
                    .setParameter("dutqd", diemUtqd)
                    .setParameter("id",    aspirationId)
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    /**
     * Batch update kết quả cho một danh sách id — tối ưu khi cần đặt trạng thái
     * hàng loạt (ví dụ: đặt tất cả về null trước khi chạy lại xét tuyển).
     *
     * @param ids    danh sách aspirationId cần cập nhật
     * @param status trạng thái muốn gán
     * @return số dòng đã cập nhật
     */
    public int batchUpdateStatus(List<Integer> ids, String status) {
        if (ids == null || ids.isEmpty()) return 0;
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createQuery(
                    "update AspirationDTO a " +
                    "set a.nvKetqua = :status " +
                    "where a.idnv in (:ids)")
                    .setParameter("status", status)
                    .setParameterList("ids", ids)
                    .executeUpdate();
            tx.commit();
            return affected;
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    /**
     * Reset toàn bộ kết quả — gọi trước khi chạy lại thuật toán xét tuyển
     * từ đầu (set nvKetqua = null cho tất cả dòng).
     *
     * @return số dòng đã reset
     */
    public int resetAllResults() {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createQuery(
                    "update AspirationDTO a set a.nvKetqua = null")
                    .executeUpdate();
            tx.commit();
            return affected;
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    // ----------------------------------------------------------------
    // THỐNG KÊ
    // ----------------------------------------------------------------

    /** Đếm số thí sinh đã trúng tuyển (status = "ĐẬU") theo ngành. */
    public long countAdmittedByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return 0L;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "select count(a) from AspirationDTO a " +
                    "where a.nvManganh = :ma " +
                    "  and (upper(coalesce(a.nvKetqua, '')) like 'ĐẬU%' or upper(coalesce(a.nvKetqua, '')) like 'DAU%')",
                    Long.class)
                    .setParameter("ma", maNganh.trim())
                    .uniqueResult();
            return count != null ? count : 0L;
        }
    }

    /** Đếm số nguyện vọng theo trạng thái kết quả (ĐẬU / RỚT / null). */
    public long countByStatus(String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = (status == null)
                    ? "select count(a) from AspirationDTO a where a.nvKetqua is null"
                    : "select count(a) from AspirationDTO a where a.nvKetqua = :status";

            var query = session.createQuery(hql, Long.class);
            if (status != null) query.setParameter("status", status);

            Long count = query.uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public void batchUpdateAll(List<AspirationDTO> list) {
        if (list == null || list.isEmpty()) return;
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            for (AspirationDTO asp : list) {
                // Dùng merge để ép Hibernate kiểm tra thay đổi của các field như diemCong, diemXettuyen
                session.merge(asp); 
            }
            tx.commit();
            System.out.println("✅ Đã ghi thành công " + list.size() + " bản ghi vào xt_nguyenvongxettuyen");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Lỗi ghi DB: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // ----------------------------------------------------------------
    // TỐI ƯU HOÁ IMPORT EXCEL HÀNG LOẠT
    // ----------------------------------------------------------------

    /** Lấy tất cả khóa nvKeys lên RAM để kiểm tra trùng lặp siêu tốc */
    public java.util.Set<String> getAllNvKeys() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<String> keys = session.createQuery("select a.nvKeys from AspirationDTO a", String.class).list();
            return new java.util.HashSet<>(keys);
        }
    }

    /** Lưu hàng loạt (Batch Insert) */
    public int saveAllBatch(List<AspirationDTO> list) {
        if (list == null || list.isEmpty()) return 0;
        int count = 0;
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                session.save(list.get(i));
                
                // Cứ 50 dòng thì đẩy lệnh xuống DB một lần và dọn RAM (Tránh tràn bộ nhớ)
                if (i > 0 && i % 50 == 0) {
                    session.flush();
                    session.clear();
                }
                count++;
            }
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
        return count;
    }


    // ========================================================================
    // CHỨC NĂNG KẾT QUẢ XẾT TUYỂN
    // ========================================================================

    /**
     * 1. Lấy danh sách trúng tuyển (Trạng thái 'ĐẬU') chi tiết theo ngành
     */
    public List<AspirationDTO> getAdmittedCandidatesByMaNganh(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from AspirationDTO a " +
                    "where a.nvManganh = :ma " +
                    "  and (upper(coalesce(a.nvKetqua, '')) like 'ĐẬU%' or upper(coalesce(a.nvKetqua, '')) like 'DAU%') " +
                    "order by coalesce(a.diemXettuyen, 0) desc, a.nvTt asc",
                    AspirationDTO.class)
                    .setParameter("ma", maNganh.trim())
                    .list();
        }
    }

    /**
     * 2. Thống kê số lượng trúng tuyển từng phương thức theo ngành
     * Thống kê dựa trên trường phương thức xét tuyển 'ttPhuongthuc' của các nguyện vọng có kết quả 'ĐẬU'
     */
    public List<Object[]> countAdmittedByMethod(String maNganh) {
        if (maNganh == null || maNganh.isBlank()) return new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select case " +
                    "  when upper(coalesce(a.ttPhuongthuc, '')) like '%DGNL%' or upper(coalesce(a.ttPhuongthuc, '')) like '%ĐGNL%' then 'DGNL' " +
                    "  when upper(coalesce(a.ttPhuongthuc, '')) like '%VSAT%' or upper(coalesce(a.ttPhuongthuc, '')) like '%V-SAT%' then 'VSAT' " +
                    "  else 'THPT' " +
                    "end, count(a.idnv) " +
                    "from AspirationDTO a " +
                    "where a.nvManganh = :ma " +
                    "  and (upper(coalesce(a.nvKetqua, '')) like 'ĐẬU%' or upper(coalesce(a.nvKetqua, '')) like 'DAU%') " +
                    "group by case " +
                    "  when upper(coalesce(a.ttPhuongthuc, '')) like '%DGNL%' or upper(coalesce(a.ttPhuongthuc, '')) like '%ĐGNL%' then 'DGNL' " +
                    "  when upper(coalesce(a.ttPhuongthuc, '')) like '%VSAT%' or upper(coalesce(a.ttPhuongthuc, '')) like '%V-SAT%' then 'VSAT' " +
                    "  else 'THPT' " +
                    "end",
                    Object[].class)
                    .setParameter("ma", maNganh.trim())
                    .list();
        }
    }


    public List<AspirationDTO> getAllSortedForAdmissionPaginated(int offset, int limit) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "FROM AspirationDTO a " +
                         "ORDER BY a.nvTt ASC, a.diemXettuyen DESC, a.nnCccd ASC";
            Query<AspirationDTO> query = session.createQuery(hql, AspirationDTO.class);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            session.close();
        }
    }
    
    /**
     * Đếm tổng số lượng nguyện vọng để phục vụ vòng lặp phân trang
     */
    public long countAllAspirations() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            String hql = "SELECT COUNT(a.idnv) FROM AspirationDTO a";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            session.close();
        }
    }
}