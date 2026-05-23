package com.example.dao;

import com.example.dto.AccountDTO;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class AccountDAO {
    public List<AccountDTO> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from AccountDTO", AccountDTO.class).list();
        }
    }

    public List<AccountDTO> getAllWithPagination(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from AccountDTO order by id asc", AccountDTO.class)
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }

    public long getCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("select count(a) from AccountDTO a", Long.class).uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public long getSearchCount(String keyword) {
        String searchKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "select count(a) from AccountDTO a " +
                                    "where lower(a.username) like :keyword " +
                                    "or lower(coalesce(a.hoTen, '')) like :keyword",
                            Long.class)
                    .setParameter("keyword", "%" + searchKeyword + "%")
                    .uniqueResult();
            return count != null ? count : 0L;
        }
    }

    public List<AccountDTO> searchByUsernameOrName(String keyword, int offset, int limit) {
        String searchKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from AccountDTO a " +
                                    "where lower(a.username) like :keyword " +
                                    "or lower(coalesce(a.hoTen, '')) like :keyword " +
                                    "order by a.id asc",
                            AccountDTO.class)
                    .setParameter("keyword", "%" + searchKeyword + "%")
                    .setFirstResult(Math.max(0, offset))
                    .setMaxResults(Math.max(1, limit))
                    .list();
        }
    }

    public AccountDTO getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(AccountDTO.class, id);
        }
    }

    public AccountDTO findByUsernameAndPassword(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from AccountDTO a where a.username = :username and a.password = :password and a.isActive = true",
                            AccountDTO.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .uniqueResult();
        }
    }

    public AccountDTO findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from AccountDTO a where a.username = :username",
                            AccountDTO.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    public AccountDTO findById(int id) {
        return getById(id);
    }

    public void save(AccountDTO account) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(account);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public void update(AccountDTO account) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.update(account);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public void delete(AccountDTO account) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.delete(account);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }
}
