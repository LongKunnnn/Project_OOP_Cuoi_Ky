package dao;

import model.BorrowDetail;
import model.BorrowTicket;
import model.Book;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class BorrowDAO {
    private static final int MAX_BORROW = 5;
    private static final int BORROW_DAYS = 30;
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BookDAO bookDAO = new BookDAO();

    // tạo phiếu mượn: nhận readerId và danh sách barcode sách cần mượn
    public BorrowTicket createBorrow(int readerId, List<String> bookBarcodes) throws Exception {
        // kiểm tra số sách đang mượn hiện tại
        int currently = readerDAO.countCurrentlyBorrowed(readerId);
        int canBorrow = MAX_BORROW - currently;
        if (canBorrow <= 0) throw new Exception("Độc giả đã mượn tối đa 5 cuốn (chưa trả).");

        // giữ unique bookBarcodes và giới hạn canBorrow
        bookBarcodes = bookBarcodes.stream().distinct().limit(canBorrow).toList();
        if (bookBarcodes.isEmpty()) throw new Exception("Không có sách hợp lệ để mượn.");

        Connection c = null;
        PreparedStatement psTicket = null, psDetail = null, psUpdateQty = null;
        ResultSet rs = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            String ticketCode = "TICKET-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();

            String sqlTicket = "INSERT INTO borrow_ticket(ticket_code, reader_id, borrow_date, note) VALUES(?,?,?,?)";
            psTicket = c.prepareStatement(sqlTicket, Statement.RETURN_GENERATED_KEYS);
            psTicket.setString(1, ticketCode);
            psTicket.setInt(2, readerId);
            psTicket.setDate(3, Date.valueOf(LocalDate.now()));
            psTicket.setString(4, ""); 
            psTicket.executeUpdate();
            rs = psTicket.getGeneratedKeys();
            int ticketId;
            if (rs.next()) ticketId = rs.getInt(1);
            else throw new Exception("Không tạo được phiếu mượn");

            String sqlDetail = "INSERT INTO borrow_detail(ticket_id, book_id, book_barcode, borrow_date, due_date, status) VALUES(?,?,?,?,?,?)";
            psDetail = c.prepareStatement(sqlDetail, Statement.RETURN_GENERATED_KEYS);

            String sqlUpdateQty = "UPDATE book SET quantity = ? WHERE book_id = ?";
            psUpdateQty = c.prepareStatement(sqlUpdateQty);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(BORROW_DAYS);

            for (String barcode : bookBarcodes) {
                Book book = bookDAO.findByBarcode(barcode);
                if (book == null) {
                    c.rollback(); throw new Exception("Sách không tồn tại: " + barcode);
                }
                if (book.getQuantity() <= 0) {
                    c.rollback(); throw new Exception("Sách hiện không còn: " + book.getTitle());
                }

                // insert detail
                psDetail.setInt(1, ticketId);
                psDetail.setInt(2, book.getBookId());
                psDetail.setString(3, barcode);
                psDetail.setDate(4, Date.valueOf(borrowDate));
                psDetail.setDate(5, Date.valueOf(dueDate));
                psDetail.setString(6, "BORROWED");
                psDetail.executeUpdate();

                // update qty
                int newQty = book.getQuantity() - 1;
                psUpdateQty.setInt(1, newQty);
                psUpdateQty.setInt(2, book.getBookId());
                psUpdateQty.executeUpdate();
            }

            c.commit();

            BorrowTicket ticket = new BorrowTicket();
            ticket.setTicketCode(ticketCode);
            ticket.setReaderId(readerId);
            ticket.setBorrowDate(borrowDate.toString());
            return ticket;
        } catch (Exception ex) {
            if (c != null) try { c.rollback(); } catch (SQLException ignored) {}
            throw ex;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (psDetail != null) psDetail.close(); } catch (Exception ignored) {}
            try { if (psTicket != null) psTicket.close(); } catch (Exception ignored) {}
            try { if (psUpdateQty != null) psUpdateQty.close(); } catch (Exception ignored) {}
            try { if (c != null) c.setAutoCommit(true); c.close(); } catch (Exception ignored) {}
        }
    }

    // lấy danh sách sách đang mượn của 1 reader (chưa trả)
    public ResultSet getCurrentlyBorrowedResultSet(int readerId) {
        // trả ResultSet để UI có thể hiển thị JTable (UI nên close kết nối sau khi dùng)
        try {
            Connection c = DBConnection.getConnection();
            String sql = "SELECT bd.id, b.code, b.title, b.author, bd.book_barcode, bd.borrow_date, bd.due_date " +
                    "FROM borrow_detail bd " +
                    "JOIN borrow_ticket bt ON bd.ticket_id = bt.ticket_id " +
                    "JOIN book b ON bd.book_id = b.book_id " +
                    "WHERE bt.reader_id = ? AND bd.status = 'BORROWED'";
            PreparedStatement ps = c.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setInt(1, readerId);
            return ps.executeQuery(); // caller phải close rs and connection
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
