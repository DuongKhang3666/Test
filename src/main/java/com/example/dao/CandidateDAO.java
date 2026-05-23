package com.example.dao;

import com.example.dto.CandidateDTO;
import com.example.utils.HibernateUtil;
import com.example.utils.HibernateBatchUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {

    // Lấy danh sách có phân trang (20 dòng/trang)
    public List<CandidateDTO> getAllWithPagination(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from CandidateDTO", CandidateDTO.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        }
    }

    // Đếm tổng số thí sinh
    public long getCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("select count(c) from CandidateDTO c", Long.class)
                    .getSingleResult();
        }
    }

    // Tìm kiếm theo CCCD hoặc họ tên 
    public List<CandidateDTO> searchByCccdOrName(String keyword, int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "from CandidateDTO c where c.cccd like :keyword " +
                         "or concat(c.ho, ' ', c.ten) like :keyword";
            return session.createQuery(hql, CandidateDTO.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        }
    }

    // Đếm tổng số kết quả tìm kiếm
    public long countSearchResults(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select count(c) from CandidateDTO c where c.cccd like :keyword " +
                         "or concat(c.ho, ' ', c.ten) like :keyword";
            return session.createQuery(hql, Long.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getSingleResult();
        }
    }

    // Tìm chính xác theo CCCD (Dùng để kiểm tra trùng lặp khi import)
    public CandidateDTO findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from CandidateDTO c where c.cccd = :cccd", CandidateDTO.class)
                    .setParameter("cccd", cccd)
                    .uniqueResult();
        }
    }

    // Lấy danh sách CCCD đã tồn tại (trong 1 tập CCCD đầu vào) để tránh import trùng
    public List<String> findExistingCccds(List<String> cccds) {
        if (cccds == null || cccds.isEmpty()) {
            return new ArrayList<>();
        }

        // Tránh query IN quá dài bằng cách chia nhỏ
        final int batchSize = 500;
        List<String> existing = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (int start = 0; start < cccds.size(); start += batchSize) {
                int end = Math.min(start + batchSize, cccds.size());
                List<String> chunk = cccds.subList(start, end);
                existing.addAll(
                        session.createQuery(
                                        "select c.cccd from CandidateDTO c where c.cccd in (:cccds)",
                                        String.class
                                )
                                .setParameterList("cccds", chunk)
                                .list()
                );
            }
        }
        return existing;
    }

    // Lưu danh sách thí sinh 
    public HibernateBatchUtil.BatchImportResult saveAllWithResult(List<CandidateDTO> candidates, int batchSize) {
        return HibernateBatchUtil.importCandidatesInBatchesSafely(candidates, batchSize);
    }

    // --- Các hàm CRUD cơ bản ---

    public void save(CandidateDTO candidate) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(candidate);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            ex.printStackTrace();
            throw ex;
        }
    }

    public void update(CandidateDTO candidate) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(candidate);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }

    public void delete(CandidateDTO candidate) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(candidate);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) tx.rollback();
            throw ex;
        }
    }
}