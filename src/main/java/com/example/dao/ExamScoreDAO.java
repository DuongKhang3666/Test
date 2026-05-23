package com.example.dao;

import com.example.dto.ExamScoreDTO;
import com.example.utils.HibernateUtil;
import com.example.utils.HibernateBatchUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class ExamScoreDAO {

    /**
     * Upsert: nếu tổ hợp CCCD + Phương thức chưa tồn tại → INSERT mới.
     * Nếu đã tồn tại (kể cả is_active=0) → kích hoạt lại và UPDATE điểm.
     */
    public boolean insert(ExamScoreDTO dto) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // ĐỔI TẠI ĐÂY: Truy vấn dựa trên cả CCCD và d_phuongthuc (phuongThuc)
            Query<ExamScoreDTO> q = session.createQuery(
                "FROM ExamScoreDTO WHERE cccd = :cccd AND phuongThuc = :phuongThuc", ExamScoreDTO.class);
            q.setParameter("cccd", dto.getCccd());
            q.setParameter("phuongThuc", dto.getPhuongThuc());
            ExamScoreDTO existing = q.uniqueResult();

            if (existing != null) {
                // Đã tồn tại tổ hợp này -> kích hoạt lại + cập nhật toàn bộ điểm
                existing.setIsActive(true);
                existing.setSoBaoDanh(dto.getSoBaoDanh());
                
                // Cập nhật điểm chuyên biệt nền tảng cho phương thức này
                existing.setDiemToan(dto.getDiemToan());
                existing.setDiemVan(dto.getDiemVan());
                existing.setDiemLy(dto.getDiemLy());
                existing.setDiemHoa(dto.getDiemHoa());
                existing.setDiemSinh(dto.getDiemSinh());
                existing.setDiemSu(dto.getDiemSu());
                existing.setDiemDia(dto.getDiemDia());
                existing.setDiemKtpl(dto.getDiemKtpl());
                existing.setDiemCncn(dto.getDiemCncn());
                existing.setDiemCnnn(dto.getDiemCnnn());
                
                existing.setN1Thi(dto.getN1Thi());
                existing.setN1Cc(dto.getN1Cc());
                existing.setNl1(dto.getNl1());
                existing.setNk1(dto.getNk1());
                existing.setNk2(dto.getNk2());

                session.update(existing);
            } else {
                // Chưa tồn tại -> Tạo mới hoàn toàn một dòng độc lập trong DB
                session.save(dto);
            }

            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Tìm theo CCCD (trả về dòng đầu tiên nếu có nhiều phương thức)
     */
    public ExamScoreDTO findByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ExamScoreDTO> query = session.createQuery(
                "FROM ExamScoreDTO WHERE cccd = :cccd AND isActive = true " +
                "ORDER BY idDiemThi ASC", ExamScoreDTO.class);
            query.setParameter("cccd", cccd);
            List<ExamScoreDTO> list = query.list();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * SỬA HÀM XEM/SỬA: Tìm kiếm điểm theo CCCD và Phương thức xét tuyển cụ thể
     */
    public ExamScoreDTO findByCccdAndMethod(String cccd, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ExamScoreDTO> query = session.createQuery(
                "FROM ExamScoreDTO WHERE cccd = :cccd AND phuongThuc = :phuongThuc AND isActive = true", 
                ExamScoreDTO.class);
            query.setParameter("cccd", cccd);
            query.setParameter("phuongThuc", phuongThuc);
            
            // Lấy ra chính xác duy nhất dòng điểm cần xem/sửa
            return query.uniqueResult(); 
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * SỬA HÀM XÓA: Chỉ xóa mềm đúng dòng phương thức đang chọn của CCCD đó
     */
    public boolean deleteByCccdAndMethod(String cccd, String phuongThuc) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Query<?> q = session.createQuery(
                "UPDATE ExamScoreDTO SET isActive = false WHERE cccd = :cccd AND phuongThuc = :phuongThuc");
            q.setParameter("cccd", cccd);
            q.setParameter("phuongThuc", phuongThuc);
            
            int rowCount = q.executeUpdate();
            tx.commit();
            return rowCount > 0;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public List<ExamScoreDTO> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM ExamScoreDTO WHERE isActive = true ORDER BY idDiemThi ASC", 
                ExamScoreDTO.class
            ).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<ExamScoreDTO> findByPhuongThuc(String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ExamScoreDTO> query = session.createQuery(
                "FROM ExamScoreDTO WHERE phuongThuc = :pt AND isActive = true",
                ExamScoreDTO.class
            );
            query.setParameter("pt", phuongThuc);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<ExamScoreDTO> findAllPaged(int pageIndex, int pageSize) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ExamScoreDTO> query = session.createQuery(
                "FROM ExamScoreDTO WHERE isActive = true ORDER BY idDiemThi ASC",
                ExamScoreDTO.class
            );
            query.setFirstResult(pageIndex * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public long countAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT COUNT(*) FROM ExamScoreDTO WHERE isActive = true", Long.class
            ).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean update(ExamScoreDTO dto) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(dto);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateN1Cc(String cccd, double n1Cc) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE ExamScoreDTO SET n1Cc = :n1cc WHERE cccd = :cccd"
            );
            query.setParameter("n1cc", n1Cc);
            query.setParameter("cccd", cccd);
            query.executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }


    public double tinhDiemTrungBinh(String tenCot, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql;
            Query<Double> query;
            if (phuongThuc != null && !phuongThuc.isBlank()) {
                hql = "SELECT AVG(e." + tenCot + ") FROM ExamScoreDTO e "
                    + "WHERE e.phuongThuc = :pt AND e." + tenCot + " IS NOT NULL";
                query = session.createQuery(hql, Double.class);
                query.setParameter("pt", phuongThuc);
            } else {
                hql = "SELECT AVG(e." + tenCot + ") FROM ExamScoreDTO e "
                    + "WHERE e." + tenCot + " IS NOT NULL";
                query = session.createQuery(hql, Double.class);
            }
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public long countByPhuongThuc(String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(*) FROM ExamScoreDTO WHERE phuongThuc = :pt AND isActive = true",
                Long.class
            );
            query.setParameter("pt", phuongThuc);
            Long result = query.uniqueResult();
            return result != null ? result : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public List<String> findExistingCccds(List<String> cccds) {
        if (cccds == null || cccds.isEmpty()) return new java.util.ArrayList<>();
        try (org.hibernate.Session session = com.example.utils.HibernateUtil.getSessionFactory().openSession()) {
            org.hibernate.query.Query<String> query = session.createQuery(
                "SELECT cccd FROM ExamScoreDTO WHERE cccd IN :cccds AND isActive = true", String.class);
            query.setParameter("cccds", cccds);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    public HibernateBatchUtil.BatchImportResult saveAllWithResult(List<ExamScoreDTO> dtoList, int batchSize) {
        return HibernateBatchUtil.importInBatchesSafely(
                dtoList, batchSize, dto -> dto.getCccd() + "_" + dto.getPhuongThuc());
    }
    
        /**
     * Tìm kiếm theo CCCD (partial match - chứa từ khóa)
     */
    public List<ExamScoreDTO> searchByCccdPartial(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAllPaged(0, 100); // fallback
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ExamScoreDTO> query = session.createQuery(
                "FROM ExamScoreDTO WHERE cccd LIKE :keyword AND isActive = true ORDER BY idDiemThi ASC", 
                ExamScoreDTO.class);
            query.setParameter("keyword", "%" + keyword.trim() + "%");
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}