package com.example.utils;

import com.example.dto.CandidateDTO;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.ArrayList;
import java.util.List;
import com.example.dto.MajorGroupDTO;

public class HibernateBatchUtil {

    public static class RowImportError {
        private final String cccd;
        private final String hoTen;
        private final String reason;
        private int rowNum;

        public RowImportError(String cccd, String hoTen, String reason) {
            this.cccd = cccd;
            this.hoTen = hoTen;
            this.reason = reason;
            this.rowNum = -1;
        }

        public RowImportError(int rowNum, String reason) {
            this.rowNum = rowNum;
            this.cccd = "";
            this.hoTen = "";
            this.reason = reason;
        }

        public String getCccd() {
            return cccd;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getReason() {
            return reason;
        }

        public int getRowNum() {
            return rowNum;
        }
    }

    public static class BatchImportResult {
        private final int inserted;
        private final int failed;
        private final int skipped;
        private final List<String> failedCccds;
        private final List<RowImportError> failedDetails;
        private final List<RowImportError> skippedDetails;

        public BatchImportResult(int inserted, int failed) {
            this(inserted, failed, 0, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        public BatchImportResult(int inserted, int failed, List<String> failedCccds) {
            this(inserted, failed, 0, failedCccds, new ArrayList<>(), new ArrayList<>());
        }

        // Constructor 4 tham số - dùng trong importInBatchesSafely
        public BatchImportResult(int inserted, int failed, List<String> failedCccds,
                                 List<RowImportError> failedDetails) {
            this(inserted, failed, 0, failedCccds, failedDetails, new ArrayList<>());
        }

        public BatchImportResult(int inserted, int failed, int skipped, List<String> failedCccds, List<RowImportError> failedDetails) {
            this(inserted, failed, skipped, failedCccds, failedDetails, new ArrayList<>());
        }

        public BatchImportResult(int inserted, int failed, int skipped, List<String> failedCccds, List<RowImportError> failedDetails, List<RowImportError> skippedDetails) {
            this.inserted = inserted;
            this.failed = failed;
            this.skipped = skipped;
            this.failedCccds = failedCccds != null ? failedCccds : new ArrayList<>();
            this.failedDetails = failedDetails != null ? failedDetails : new ArrayList<>();
            this.skippedDetails = skippedDetails != null ? skippedDetails : new ArrayList<>();
        }

        public int getInserted() {
            return inserted;
        }

        public int getFailed() {
            return failed;
        }

        public List<String> getFailedCccds() {
            return failedCccds;
        }

        public List<RowImportError> getFailedDetails() {
            return failedDetails;
        }
        public int getSkipped() {
            return skipped;
        }
        public List<RowImportError> getSkippedDetails() {
            return skippedDetails;
        }
    }

    // Import theo cụm: cụm nào lỗi mới fallback từng dòng trong cụm đó
    public static BatchImportResult importCandidatesInBatchesSafely(List<CandidateDTO> candidates, int batchSize) {
        if (candidates == null || candidates.isEmpty()) {
            return new BatchImportResult(0, 0, new ArrayList<>());
        }

        int inserted = 0;
        int failed = 0;
        int skipped = 0;
        List<String> failedCccds = new ArrayList<>();
        List<RowImportError> failedDetails = new ArrayList<>();
        int safeBatchSize = Math.max(1, batchSize);

        for (int start = 0; start < candidates.size(); start += safeBatchSize) {
            int end = Math.min(start + safeBatchSize, candidates.size());
            List<CandidateDTO> chunk = candidates.subList(start, end);

            boolean chunkSaved = false;
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                for (CandidateDTO candidate : chunk) {
                    session.save(candidate);
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

            // Chunk lỗi thì fallback từng dòng để không làm chậm toàn bộ file.
            for (CandidateDTO candidate : chunk) {
                Transaction rowTx = null;
                try (Session rowSession = HibernateUtil.getSessionFactory().openSession()) {
                    // Nếu CCCD đã tồn tại, bỏ qua ko nhập dữ liệu lần nữa
                    String cccd = candidate.getCccd();
                    boolean exists = false;
                    if (cccd != null && !cccd.isEmpty()) {
                        Long cnt = rowSession.createQuery("select count(c) from CandidateDTO c where c.cccd = :cccd", Long.class)
                                .setParameter("cccd", cccd)
                                .uniqueResult();
                        exists = cnt != null && cnt > 0;
                    }

                    if (exists) {
                        skipped++;
                        continue;
                    }

                    rowTx = rowSession.beginTransaction();
                    rowSession.save(candidate);
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
                    String cccd = candidate.getCccd();
                    if (cccd != null && !cccd.isEmpty()) {
                        failedCccds.add(cccd);
                    }
                    String hoTen = buildHoTen(candidate);
                    String reason = extractRootMessage(rowEx);
                    failedDetails.add(new RowImportError(cccd != null ? cccd : "", hoTen, reason));
                }
            }
        }

        return new BatchImportResult(inserted, failed, skipped, failedCccds, failedDetails);
    }

    private static String buildHoTen(CandidateDTO candidate) {
        if (candidate == null) {
            return "";
        }
        try {
            String ho = candidate.getHo();
            String ten = candidate.getTen();
            if (ho != null && !ho.isBlank()) {
                return (ho + " " + (ten != null ? ten : "")).trim();
            }
            return ten != null ? ten.trim() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String extractRootMessage(Throwable t) {
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
        // Giới hạn độ dài để popup không quá dài
        msg = msg.replaceAll("\\s+", " ").trim();
        if (msg.length() > 300) {
            msg = msg.substring(0, 300) + "...";
        }
        return msg;
    }

    /**
     * Hàm batch insert generic dùng chung cho mọi DTO (ExamScore, BonusScore, v.v.)
     * CandidateDAO vẫn dùng importCandidatesInBatchesSafely, các DAO khác dùng hàm này.
     */
    public static <T> BatchImportResult importInBatchesSafely(List<T> items, int batchSize,
            java.util.function.Function<T, String> getCccd) {

        if (items == null || items.isEmpty()) {
            return new BatchImportResult(0, 0, new ArrayList<>());
        }

        int inserted = 0;
        int failed = 0;
        List<String> failedCccds = new ArrayList<>();
        List<RowImportError> failedDetails = new ArrayList<>();
        int safeBatchSize = Math.max(1, batchSize);

        for (int start = 0; start < items.size(); start += safeBatchSize) {
            int end = Math.min(start + safeBatchSize, items.size());
            List<T> chunk = items.subList(start, end);

            boolean chunkSaved = false;
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                for (T item : chunk) {
                    session.persist(item);
                }
                session.flush();
                session.clear();
                tx.commit();
                chunkSaved = true;
                inserted += chunk.size();
            } catch (Exception ex) {
                if (tx != null) try { tx.rollback(); } catch (Exception ignored) {}
            }

            if (chunkSaved) continue;

            // Fallback: insert từng dòng trong chunk bị lỗi
            for (T item : chunk) {
                Transaction rowTx = null;
                try (Session rowSession = HibernateUtil.getSessionFactory().openSession()) {
                    rowTx = rowSession.beginTransaction();
                    rowSession.persist(item);
                    rowTx.commit();
                    inserted++;
                } catch (Exception rowEx) {
                    if (rowTx != null) try { rowTx.rollback(); } catch (Exception ignored) {}
                    failed++;
                    String cccd = getCccd.apply(item);
                    if (cccd != null && !cccd.isEmpty()) failedCccds.add(cccd);
                    String reason = extractRootMessage(rowEx);
                    failedDetails.add(new RowImportError(cccd != null ? cccd : "", "", reason));
                }
            }
        }

        return new BatchImportResult(inserted, failed, failedCccds, failedDetails);
    }

    // Batch import MajorGroup
    public static BatchImportResult importMajorGroupsInBatches(List<MajorGroupDTO> majorGroups, int batchSize) {
        if (majorGroups == null || majorGroups.isEmpty()) {
            return new BatchImportResult(0, 0, new ArrayList<>());
        }

        int inserted = 0;
        int failed = 0;
        int skipped = 0;
        List<String> failedCccds = new ArrayList<>();
        List<RowImportError> failedDetails = new ArrayList<>();
        int safeBatchSize = Math.max(1, batchSize);

        for (int start = 0; start < majorGroups.size(); start += safeBatchSize) {
            int end = Math.min(start + safeBatchSize, majorGroups.size());
            List<MajorGroupDTO> chunk = majorGroups.subList(start, end);

            boolean chunkSaved = false;
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                for (MajorGroupDTO mg : chunk) {
                    session.save(mg);
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

            // Chunk lỗi thì fallback từng dòng
            int rowNum = start + 1;
            for (MajorGroupDTO mg : chunk) {
                Transaction rowTx = null;
                try (Session rowSession = HibernateUtil.getSessionFactory().openSession()) {
                    String maNganh = mg.getMaNganh();
                    String maToHop = mg.getMaToHop();

                    // Kiểm tra đã tồn tại
                    boolean exists = false;
                    if (maNganh != null && !maNganh.isEmpty() && maToHop != null && !maToHop.isEmpty()) {
                        Long cnt = rowSession.createQuery(
                                        "select count(m) from MajorGroupDTO m where m.maNganh = :maNganh and m.maToHop = :maToHop",
                                        Long.class
                                )
                                .setParameter("maNganh", maNganh)
                                .setParameter("maToHop", maToHop)
                                .uniqueResult();
                        exists = cnt != null && cnt > 0;
                    }

                    if (exists) {
                        skipped++;
                        rowNum++;
                        continue;
                    }

                    rowTx = rowSession.beginTransaction();
                    rowSession.save(mg);
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
                    String reason = extractRootMessage(rowEx);
                    failedDetails.add(new RowImportError(rowNum, reason));
                    failedCccds.add(mg.getMaNganh() + "|" + mg.getMaToHop());
                }
                rowNum++;
            }
        }

        return new BatchImportResult(inserted, failed, skipped, failedCccds, failedDetails);
    }
}