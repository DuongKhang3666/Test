package com.example.dao;

import com.example.dto.ConversionTableDTO;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

public class ConversionTableDAO {

    // Lấy toàn bộ danh sách quy đổi từ bảng xt_bangquydoi
    public List<ConversionTableDTO> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ConversionTableDTO", ConversionTableDTO.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ConversionTableDTO getById(int idqd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ConversionTableDTO.class, idqd);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Thêm mới hoặc cập nhật thông tin quy đổi
    public boolean save(ConversionTableDTO dto) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(dto);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ConversionTableDTO dto) {
        return save(dto);
    }

    // Xóa một dòng quy đổi theo ID (idqd)
    public boolean delete(int idqd) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ConversionTableDTO dto = session.get(ConversionTableDTO.class, idqd);
            if (dto != null) {
                session.delete(dto);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(c) FROM ConversionTableDTO c", Long.class)
                    .uniqueResultOptional()
                    .orElse(0L);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public List<ConversionTableDTO> getPage(int pageIndex, int pageSize) {
        if (pageIndex < 0 || pageSize <= 0) return Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ConversionTableDTO> query = session.createQuery("FROM ConversionTableDTO ORDER BY idqd ASC", ConversionTableDTO.class);
            query.setFirstResult(pageIndex * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Tìm kiếm quy định quy đổi theo mã quy đổi (maQuydoi)
    public List<ConversionTableDTO> findByMaQuydoi(String maQuydoi) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ConversionTableDTO WHERE maQuydoi = :ma";
            return session.createQuery(hql, ConversionTableDTO.class)
                          .setParameter("ma", maQuydoi)
                          .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ConversionTableDTO> search(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        if (normalized.isEmpty()) {
            return getAll();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ConversionTableDTO c " +
                    "WHERE lower(c.phuongthuc) LIKE :kw " +
                    "OR lower(c.tohop) LIKE :kw " +
                    "OR lower(c.mon) LIKE :kw " +
                    "OR lower(c.maQuydoi) LIKE :kw " +
                    "OR lower(c.phanvi) LIKE :kw " +
                    "ORDER BY c.idqd ASC";
            return session.createQuery(hql, ConversionTableDTO.class)
                    .setParameter("kw", "%" + normalized.toLowerCase() + "%")
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> findDistinctPhuongThuc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT c.phuongthuc FROM ConversionTableDTO c WHERE c.phuongthuc IS NOT NULL AND trim(c.phuongthuc) <> '' ORDER BY c.phuongthuc",
                            String.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> findDistinctToHop() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT c.tohop FROM ConversionTableDTO c WHERE c.tohop IS NOT NULL AND trim(c.tohop) <> '' ORDER BY c.tohop",
                            String.class)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}