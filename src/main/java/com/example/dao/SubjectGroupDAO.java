package com.example.dao;

import com.example.dto.SubjectGroupDTO;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class SubjectGroupDAO {

    public List<SubjectGroupDTO> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from SubjectGroupDTO", SubjectGroupDTO.class).list();
        }
    }

    public SubjectGroupDTO findById(int idToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(SubjectGroupDTO.class, idToHop);
        }
    }

    public SubjectGroupDTO findByMaToHop(String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from SubjectGroupDTO sg where sg.maToHop = :maToHop",
                            SubjectGroupDTO.class)
                    .setParameter("maToHop", maToHop)
                    .uniqueResult();
        }
    }

    public void save(SubjectGroupDTO subjectGroup) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(subjectGroup);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public void update(SubjectGroupDTO subjectGroup) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(subjectGroup);
            session.flush();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }

    public void delete(SubjectGroupDTO subjectGroup) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            SubjectGroupDTO attached = subjectGroup;
            if (!session.contains(subjectGroup)) {
                attached = (SubjectGroupDTO) session.merge(subjectGroup);
            }
            session.delete(attached);
            session.flush();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        }
    }
}