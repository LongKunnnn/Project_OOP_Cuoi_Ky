package ui;

import dao.BookDAO;
import dao.BorrowDAO;
import dao.ReaderDAO;
import dao.DBConnection;


import model.Book;
import model.BorrowDetail;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowBookPanel extends JPanel {
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();

    private JTextField txtReaderBarcode;
    private JLabel lblReaderName;
    private JTable tblCurrent;
    private DefaultTableModel currentModel;
    private JTextField txtBookBarcodes; // nhập barcode sách, ngăn cách bằng dấu phẩy
    private JButton btnConfirm;

    private int currentReaderId = -1;

    public BorrowBookPanel() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Barcode thẻ độc giả:"));
        txtReaderBarcode = new JTextField(20);
        top.add(txtReaderBarcode);
        JButton btnScan = new JButton("Quét");
        top.add(btnScan);
        lblReaderName = new JLabel("Chưa quét độc giả");
        top.add(lblReaderName);

        add(top, BorderLayout.NORTH);

        currentModel = new DefaultTableModel(new String[]{"ID", "Mã", "Tựa", "Tác giả", "Barcode", "Ngày mượn", "Hạn trả"}, 0);
        tblCurrent = new JTable(currentModel);
        add(new JScrollPane(tblCurrent), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Barcode sách (ngăn cách dấu phẩy):"));
        txtBookBarcodes = new JTextField(40);
        bottom.add(txtBookBarcodes);
        btnConfirm = new JButton("Xác nhận mượn");
        bottom.add(btnConfirm);

        add(bottom, BorderLayout.SOUTH);

        btnScan.addActionListener(e -> scanReader());
        btnConfirm.addActionListener(e -> confirmBorrow());
    }

    private void scanReader() {
        String rb = txtReaderBarcode.getText().trim();
        if (rb.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập barcode thẻ độc giả");
            return;
        }
        var reader = readerDAO.findByBarcode(rb);
        if (reader == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy độc giả");
            lblReaderName.setText("Chưa quét độc giả");
            currentReaderId = -1;
            return;
        }
        lblReaderName.setText("Độc giả: " + reader.getName() + " (Mã: " + reader.getCode() + ")");
        currentReaderId = reader.getReaderId();
        loadCurrentBorrowed(reader.getReaderId());
    }

    private void loadCurrentBorrowed(int readerId) {
        currentModel.setRowCount(0);
        try (ResultSet rs = borrowDAO.getCurrentlyBorrowedResultSet(readerId)) {
            if (rs == null) return;
            while (rs.next()) {
                Object[] row = new Object[] {
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("book_barcode"),
                        rs.getDate("borrow_date"),
                        rs.getDate("due_date")
                };
                currentModel.addRow(row);
            }
            // close connection that holds the ResultSet
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmBorrow() {
        if (currentReaderId <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng quét thẻ độc giả trước");
            return;
        }
        String barcodesText = txtBookBarcodes.getText().trim();
        if (barcodesText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập barcode sách cần mượn");
            return;
        }
        String[] arr = barcodesText.split("[,;\\s]+");
        List<String> barcodes = new ArrayList<>();
        for (String s : arr) if (!s.isBlank()) barcodes.add(s.trim());

        try {
            var ticket = borrowDAO.createBorrow(currentReaderId, barcodes);
            JOptionPane.showMessageDialog(this, "Tạo phiếu mượn thành công: " + ticket.getTicketCode());
            // refresh current borrowed list
            loadCurrentBorrowed(currentReaderId);
            // in phiếu (hiển thị một dialog tóm tắt)
            showReceiptDialog(ticket.getTicketCode(), currentReaderId);
            txtBookBarcodes.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private void showReceiptDialog(String ticketCode, int readerId) {
        // Lấy lại các borrow_detail vừa tạo để hiển thị
        StringBuilder sb = new StringBuilder();
        sb.append("Mã phiếu: ").append(ticketCode).append("\n");
        sb.append("Độc giả ID: ").append(readerId).append("\n");
        sb.append("Danh sách sách (mượn):\n");
        try {
            String sql = "SELECT b.code, b.title, b.author, bd.book_barcode, bd.borrow_date, bd.due_date FROM borrow_detail bd JOIN book b ON bd.book_id=b.book_id JOIN borrow_ticket bt ON bd.ticket_id = bt.ticket_id WHERE bt.ticket_code = ?";
            try (var c = DBConnection.getConnection(); var ps = c.prepareStatement(sql)) {
                ps.setString(1, ticketCode);
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sb.append(rs.getString("code")).append(" | ")
                          .append(rs.getString("title")).append(" | ")
                          .append(rs.getString("author")).append(" | Barcode:")
                          .append(rs.getString("book_barcode")).append(" | Borrow:")
                          .append(rs.getDate("borrow_date")).append(" | Due:")
                          .append(rs.getDate("due_date")).append("\n");
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Phiếu mượn", JOptionPane.INFORMATION_MESSAGE);
    }
}
