package com.example.dao;

import com.example.dto.BonusScoreDTO;
import com.example.utils.HibernateBatchUtil;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class BonusScoreDAO {

    public List<BonusScoreDTO> getAllWithPagination(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from BonusScoreDTO b order by b.tsCccd asc", BonusScoreDTO.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        }
    }

    public List<BonusScoreDTO> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from BonusScoreDTO b order by b.tsCccd asc", BonusScoreDTO.class).list();
        }
    }

    public long getCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(b) from BonusScoreDTO b", Long.class)
                    .getSingleResult();
        }
    }

    public List<BonusScoreDTO> getSearchWithPagination(String cccdPattern, int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from BonusScoreDTO b where b.tsCccd like :cccd order by b.tsCccd asc";
            return session.createQuery(hql, BonusScoreDTO.class)
                    .setParameter("cccd", cccdPattern)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        }
    }

    public List<BonusScoreDTO> getByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from BonusScoreDTO b where b.tsCccd = :cccd", BonusScoreDTO.class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    // Đếm tổng số kết quả tìm kiếm
    public long countSearchResults(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select count(b) from BonusScoreDTO b where b.tsCccd like :cccd ";
            return session.createQuery(hql, Long.class)
                    .setParameter("cccd", cccd)
                    .getSingleResult();
        }
    }

    public void save(BonusScoreDTO bonusScore) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(bonusScore);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public void update(BonusScoreDTO bonusScore) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(bonusScore);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public boolean deleteById(int idDiemCong) {
        if (idDiemCong <= 0) {
            return false;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int affected = session.createQuery("delete from BonusScoreDTO b where b.idDiemCong = :id")
                    .setParameter("id", idDiemCong)
                    .executeUpdate();
            tx.commit();
            return affected > 0;
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public List<Object[]> getAllNguyenVongBasic() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select a.nnCccd, a.nvManganh, a.ttThm, a.ttPhuongthuc from AspirationDTO a",
                            Object[].class)
                    .list();
        }
    }

    public List<Object[]> getAllNguyenVongBasicWithPagination(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select a.nnCccd, a.nvManganh, a.ttThm, a.ttPhuongthuc from AspirationDTO a";
            return session.createQuery(hql, Object[].class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        }
    }

    public boolean checkToHopChuaMon(String maToHop, String maMon) {
        if (maToHop == null || maToHop.isBlank() || maMon == null || maMon.isBlank()) {
            return false;
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "select count(s) from SubjectGroupDTO s where s.maToHop = :maToHop and (s.mon1 = :maMon or s.mon2 = :maMon or s.mon3 = :maMon)",
                            Long.class)
                    .setParameter("maToHop", maToHop)
                    .setParameter("maMon", maMon)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    // 1. Lấy danh sách nguyện vọng của riêng 1 CCCD
    public List<Object[]> getNguyenVongByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select a.nvManganh, a.ttThm, a.ttPhuongthuc, a.diemXettuyen from AspirationDTO a where a.nnCccd = :cccd order by a.nvTt asc",
                    Object[].class)
                    .setParameter("cccd", cccd)
                    .list();
        }
    }

    // 2. Kiểm tra xem Nguyện vọng này đã có điểm cộng chưa
    public boolean checkTonTaiDiemCong(String dcKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "select count(b) from BonusScoreDTO b where b.dcKeys = :keys", Long.class)
                    .setParameter("keys", dcKeys)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    // 3. Lấy điểm Tiếng Anh đã từng lưu của thí sinh này (để auto-fill)
    public Double getDiemTiengAnhDaCo(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "select max(b.diemCC) from BonusScoreDTO b where b.tsCccd = :cccd", Double.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
        }
    }

    // 4. Đồng bộ điểm Tiếng Anh cho toàn bộ nguyện vọng
    public void dongBoDiemTiengAnh(String cccd, double diemCC) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createQuery("UPDATE BonusScoreDTO b SET b.diemCC = :diemMoi WHERE b.tsCccd = :cccd")
                   .setParameter("diemMoi", diemCC)
                   .setParameter("cccd", cccd)
                   .executeUpdate();

            session.createQuery("UPDATE BonusScoreDTO b SET b.diemTong = CASE WHEN (b.diemCC + b.diemUtxt) > 3.0 THEN 3.0 ELSE (b.diemCC + b.diemUtxt) END WHERE b.tsCccd = :cccd")
                   .setParameter("cccd", cccd)
                   .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public HibernateBatchUtil.BatchImportResult saveAllUpsert(List<BonusScoreDTO> bonusScores) {
        return saveAllUpsert(bonusScores, 50);
    }

    public HibernateBatchUtil.BatchImportResult saveAllUpsert(List<BonusScoreDTO> bonusScores, int batchSize) {
        if (bonusScores == null || bonusScores.isEmpty()) {
            return new HibernateBatchUtil.BatchImportResult(0, 0, new ArrayList<>());
        }

        int inserted = 0;
        int failed = 0;
        int skipped = 0;
        List<String> failedKeys = new ArrayList<>();
        List<HibernateBatchUtil.RowImportError> failedDetails = new ArrayList<>();
        int safeBatchSize = Math.max(1, batchSize);

        for (int start = 0; start < bonusScores.size(); start += safeBatchSize) {
            int end = Math.min(start + safeBatchSize, bonusScores.size());
            List<BonusScoreDTO> chunk = bonusScores.subList(start, end);

            boolean chunkSaved = false;
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                for (BonusScoreDTO score : chunk) {
                    session.merge(score);
                }
                session.flush();
                session.clear();
                tx.commit();
                chunkSaved = true;
                inserted += chunk.size();
            } catch (Exception ex) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception ignored) {
                    }
                }
            }

            if (chunkSaved) {
                continue;
            }

            for (BonusScoreDTO score : chunk) {
                Transaction rowTx = null;
                try (Session rowSession = HibernateUtil.getSessionFactory().openSession()) {
                    rowTx = rowSession.beginTransaction();
                    rowSession.merge(score);
                    rowTx.commit();
                    inserted++;
                } catch (Exception rowEx) {
                    if (rowTx != null) {
                        try {
                            rowTx.rollback();
                        } catch (Exception ignored) {
                        }
                    }
                    failed++;
                    String key = score.getDcKeys() != null ? score.getDcKeys() : buildFallbackKey(score);
                    failedKeys.add(key);
                        failedDetails.add(new HibernateBatchUtil.RowImportError(
                            score.getTsCccd() != null ? score.getTsCccd() : "",
                            "",
                            extractRootMessage(rowEx)
                        ));
                }
            }
        }

        return new HibernateBatchUtil.BatchImportResult(inserted, failed, skipped, failedKeys, failedDetails);
    }

    private String buildFallbackKey(BonusScoreDTO score) {
        if (score == null) {
            return "";
        }
        String cccd = score.getTsCccd() != null ? score.getTsCccd() : "";
        String maNganh = score.getMaNganh() != null ? score.getMaNganh() : "";
        String maToHop = score.getMaToHop() != null ? score.getMaToHop() : "";
        return cccd + "_" + maNganh + "_" + maToHop;
    }

    private String extractRootMessage(Throwable t) {
        if (t == null) {
            return "";
        }
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = root.getClass().getName();
        }
        msg = msg.replaceAll("\\s+", " ").trim();
        if (msg.length() > 300) {
            msg = msg.substring(0, 300) + "...";
        }
        return msg;
    }

    public int batchUpdateAll(List<BonusScoreDTO> list) {
        int count = 0;
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                session.merge(list.get(i)); // TỐI QUAN TRỌNG: Dùng merge thay vì update
                
                if (i > 0 && i % 50 == 0) {
                    session.flush();
                    session.clear();
                }
                count++;
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            // Bắt buộc ném lỗi để tầng GUI nhận diện được thao tác thất bại
            throw new RuntimeException("Lỗi Rollback DB khi lưu Điểm Cộng: " + e.getMessage());
        }
        return count;
    }
}