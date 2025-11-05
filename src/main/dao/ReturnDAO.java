package dao;

import model.Book;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ReturnDAO {
    private final BookDAO bookDAO = new BookDAO();

    private static final double LATE_RATE = 0.2; // 20%

    // Trả sách: list borrowDetailIds (id trong borrow_detail) được quét/đánh dấu trả
    // Trả có thể là 1 phần
    public double returnBooks(List<Integer> borrowDetailIds) throws Exception {
        if (borrowDetailIds == null || borrowDetailIds.isEmpty()) return 0;
        Connection c = null;
        PreparedStatement psSelect = null, psUpdateDetail = null, psUpdateBook = null, psInsertPenalty = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            String sqlSelect = "SELECT bd.id, bd.book_id, bd.due_date, bd.status, b.price FROM borrow_detail bd JOIN book b ON bd.book_id=b.book_id WHERE bd.id = ?";
            psSelect = c.prepareStatement(sqlSelect);

            String sqlUpdateDetail = "UPDATE borrow_detail SET return_date = ?, fine_amount = ?, status = 'RETURNED' WHERE id = ?";
            psUpdateDetail = c.prepareStatement(sqlUpdateDetail);

            String sqlUpdateBook = "UPDATE book SET quantity = quantity + 1 WHERE book_id = ?";
            psUpdateBook = c.prepareStatement(sqlUpdateBook);

            String sqlInsertPenalty = "INSERT INTO penalty(reader_id, borrow_detail_id, description, amount) VALUES(?,?,?,?)";
            psInsertPenalty = c.prepareStatement(sqlInsertPenalty);

            double totalFine = 0;
            for (int id : borrowDetailIds) {
                psSelect.setInt(1, id);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (!"BORROWED".equals(status)) continue; // đã trả rồi
                        int bookId = rs.getInt("book_id");
                        Date dueDate = rs.getDate("due_date");
                        double price = rs.getDouble("price");
                        LocalDate today = LocalDate.now();
                        double fine = 0;
                        if (dueDate != null && today.isAfter(dueDate.toLocalDate())) {
                            fine = Math.round(price * LATE_RATE); // làm tròn
                        }
                        // update detail
                        psUpdateDetail.setDate(1, Date.valueOf(today));
                        psUpdateDetail.setDouble(2, fine);
                        psUpdateDetail.setInt(3, id);
                        psUpdateDetail.executeUpdate();

                        // update book qty
                        psUpdateBook.setInt(1, bookId);
                        psUpdateBook.executeUpdate();

                        // insert penalty nếu có
                        if (fine > 0) {
                            // lấy reader_id của borrow_detail -> bằng cách join borrow_ticket
                            int readerId = getReaderIdByBorrowDetail(c, id);
                            psInsertPenalty.setInt(1, readerId);
                            psInsertPenalty.setInt(2, id);
                            psInsertPenalty.setString(3, "Trả muộn");
                            psInsertPenalty.setDouble(4, fine);
                            psInsertPenalty.executeUpdate();
                            totalFine += fine;
                        }
                    }
                }
            }
            c.commit();
            return totalFine;
        } catch (Exception ex) {
            if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            try { if (psSelect != null) psSelect.close(); } catch (Exception ignored) {}
            try { if (psUpdateDetail != null) psUpdateDetail.close(); } catch (Exception ignored) {}
            try { if (psUpdateBook != null) psUpdateBook.close(); } catch (Exception ignored) {}
            try { if (psInsertPenalty != null) psInsertPenalty.close(); } catch (Exception ignored) {}
            try { if (c != null) c.setAutoCommit(true); c.close(); } catch (Exception ignored) {}
        }
    }

    private int getReaderIdByBorrowDetail(Connection c, int borrowDetailId) throws SQLException {
        String sql = "SELECT bt.reader_id FROM borrow_detail bd JOIN borrow_ticket bt ON bd.ticket_id = bt.ticket_id WHERE bd.id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, borrowDetailId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("reader_id");
            }
        }
        return -1;
    }
}
