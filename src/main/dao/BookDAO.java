package dao;

import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public Book findByBarcode(String barcode) {
        String sql = "SELECT * FROM book WHERE barcode = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Book b = new Book();
                    b.setBookId(rs.getInt("book_id"));
                    b.setCode(rs.getString("code"));
                    b.setTitle(rs.getString("title"));
                    b.setAuthor(rs.getString("author"));
                    b.setPublishYear(rs.getInt("publish_year"));
                    b.setPrice(rs.getDouble("price"));
                    b.setQuantity(rs.getInt("quantity"));
                    b.setBarcode(rs.getString("barcode"));
                    b.setDescription(rs.getString("description"));
                    return b;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Book findById(int id) {
        String sql = "SELECT * FROM book WHERE book_id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Book b = new Book();
                    b.setBookId(rs.getInt("book_id"));
                    b.setCode(rs.getString("code"));
                    b.setTitle(rs.getString("title"));
                    b.setAuthor(rs.getString("author"));
                    b.setPublishYear(rs.getInt("publish_year"));
                    b.setPrice(rs.getDouble("price"));
                    b.setQuantity(rs.getInt("quantity"));
                    b.setBarcode(rs.getString("barcode"));
                    b.setDescription(rs.getString("description"));
                    return b;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Book> getAll() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM book ORDER BY title";
        try (Connection c = DBConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Book b = new Book();
                b.setBookId(rs.getInt("book_id"));
                b.setCode(rs.getString("code"));
                b.setTitle(rs.getString("title"));
                b.setAuthor(rs.getString("author"));
                b.setPublishYear(rs.getInt("publish_year"));
                b.setPrice(rs.getDouble("price"));
                b.setQuantity(rs.getInt("quantity"));
                b.setBarcode(rs.getString("barcode"));
                b.setDescription(rs.getString("description"));
                list.add(b);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateQuantity(int bookId, int newQuantity) {
        String sql = "UPDATE book SET quantity = ? WHERE book_id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
