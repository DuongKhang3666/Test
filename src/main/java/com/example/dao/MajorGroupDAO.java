package com.example.dao;

import com.example.dto.MajorGroupDTO;
import com.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.ArrayList;
import com.example.utils.HibernateBatchUtil;

public class MajorGroupDAO {

    public List<MajorGroupDTO> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MajorGroupDTO", MajorGroupDTO.class).list();
        }
    }

    public MajorGroupDTO findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(MajorGroupDTO.class, id);
        }
    }

    public List<MajorGroupDTO> findByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from MajorGroupDTO mg where mg.maNganh = :maNganh",
                            MajorGroupDTO.class)
                    .setParameter("maNganh", maNganh)
                    .list();
        }
    }

    public MajorGroupDTO findByTbKeys(String tbKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from MajorGroupDTO mg where mg.tbKeys = :tbKeys",
                            MajorGroupDTO.class)
                    .setParameter("tbKeys", tbKeys)
                    .uniqueResult();
        }
    }

    public MajorGroupDTO findByMaNganhAndMaToHop(String maNganh, String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from MajorGroupDTO mg where mg.maNganh = :maNganh and mg.maToHop = :maToHop",
                            MajorGroupDTO.class)
                    .setParameter("maNganh", maNganh)
                    .setParameter("maToHop", maToHop)
                    .uniqueResult();
        }
    }

    public List<MajorGroupDTO> findByMaToHop(String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from MajorGroupDTO mg where mg.maToHop = :maToHop",
                            MajorGroupDTO.class)
                    .setParameter("maToHop", maToHop)
                    .list();
        }
    }

    public void save(MajorGroupDTO majorGroup) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (majorGroup.getId() == 0) {
                session.save(majorGroup);
            } else {
                session.merge(majorGroup);
            }
            session.flush();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) { }
            }
            throw ex;
        }
    }

    public void update(MajorGroupDTO majorGroup) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(majorGroup);
            session.flush();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) { }
            }
            throw ex;
        }
    }

    public void delete(MajorGroupDTO majorGroup) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MajorGroupDTO attached = majorGroup;
            if (!session.contains(majorGroup)) {
                attached = (MajorGroupDTO) session.merge(majorGroup);
            }
            session.delete(attached);
            session.flush();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) { }
            }
            throw ex;
        }
    }

    public List<String> findExistingMajorGroupKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> existingKeys = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            for (String key : keys) {
                String[] parts = key.split("\\|");
                if (parts.length != 2) continue;

                String maNganh = parts[0];
                String maToHop = parts[1];

                List<MajorGroupDTO> result = session.createQuery(
                                "from MajorGroupDTO mg where mg.maNganh = :maNganh and mg.maToHop = :maToHop",
                                MajorGroupDTO.class)
                        .setParameter("maNganh", maNganh)
                        .setParameter("maToHop", maToHop)
                        .list();

                if (!result.isEmpty()) {
                    existingKeys.add(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existingKeys;
    }

    public HibernateBatchUtil.BatchImportResult saveAllWithResult(List<MajorGroupDTO> majorGroups, int batchSize) {
        return HibernateBatchUtil.importMajorGroupsInBatches(majorGroups, batchSize);
    }
}