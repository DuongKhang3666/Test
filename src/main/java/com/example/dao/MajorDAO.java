package com.example.dao;

import com.example.dto.MajorDTO;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class MajorDAO {

	public List<MajorDTO> getAll() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("from MajorDTO", MajorDTO.class).list();
		}
	}

	public MajorDTO findById(int idNganh) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(MajorDTO.class, idNganh);
		}
	}

	public MajorDTO findByMaNganh(String maNganh) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery(
							"from MajorDTO m where m.maNganh = :maNganh",
							MajorDTO.class)
					.setParameter("maNganh", maNganh)
					.uniqueResult();
		}
	}

	public MajorDTO findByTenNganh(String tenNganh) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery(
							"from MajorDTO m where lower(m.tenNganh) = :tenNganh",
							MajorDTO.class)
					.setParameter("tenNganh", tenNganh == null ? null : tenNganh.toLowerCase())
					.uniqueResult();
		}
	}

	public void save(MajorDTO major) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.save(major);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		}
	}

	public void update(MajorDTO major) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			// Use merge to safely reattach detached instances
			session.merge(major);
			session.flush();
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) {
				tx.rollback();
			}
			throw ex;
		}
	}

	public void delete(MajorDTO major) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			MajorDTO attached = major;
			if (!session.contains(major)) {
				attached = (MajorDTO) session.merge(major);
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
