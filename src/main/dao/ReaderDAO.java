package dao;

import model.Reader;

import java.sql.*;

public class ReaderDAO {
    public Reader findByBarcode(String barcode) {
        String sql = "SELECT * FROM reader WHERE barcode = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reader r = new Reader();
                    r.setReaderId(rs.getInt("reader_id"));
                    r.setCode(rs.getString("code"));
                    r.setName(rs.getString("name"));
                    r.setBirthDate(rs.getString("birth_date"));
                    r.setAddress(rs.getString("address"));
                    r.setPhone(rs.getString("phone"));
                    r.setBarcode(rs.getString("barcode"));
                    return r;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // trả về số sách hiện đang mượn (status BORROWED)
    public int countCurrentlyBorrowed(int readerId) {
        String sql = "SELECT COUNT(*) FROM borrow_detail bd JOIN borrow_ticket bt ON bd.ticket_id=bt.ticket_id WHERE bt.reader_id=? AND bd.status='BORROWED'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
