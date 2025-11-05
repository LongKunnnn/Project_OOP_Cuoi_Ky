package ui;

import dao.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StatisticsBookPanel extends JPanel {
    private JTextField txtFrom;
    private JTextField txtTo;
    private JTable tbl;
    private DefaultTableModel model;

    public StatisticsBookPanel() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("From (yyyy-MM-dd):"));
        txtFrom = new JTextField(10);
        top.add(txtFrom);
        top.add(new JLabel("To (yyyy-MM-dd):"));
        txtTo = new JTextField(10);
        top.add(txtTo);
        JButton btn = new JButton("Thống kê");
        top.add(btn);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Mã","Tên","Ngày sinh","Địa chỉ","Tổng sách mượn"}, 0);
        tbl = new JTable(model);
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        btn.addActionListener(e -> runReport());
    }

    private void runReport() {
        model.setRowCount(0);
        String from = txtFrom.getText().trim();
        String to = txtTo.getText().trim();
        if (from.isEmpty() || to.isEmpty()) { JOptionPane.showMessageDialog(this, "Nhập from & to"); return; }

        String sql = "SELECT r.code, r.name, r.birth_date, r.address, SUM(cnt) as total FROM (SELECT bt.reader_id, COUNT(*) as cnt FROM borrow_detail bd JOIN borrow_ticket bt ON bd.ticket_id=bt.ticket_id WHERE bt.borrow_date BETWEEN ? AND ? GROUP BY bt.reader_id) x JOIN reader r ON r.reader_id = x.reader_id GROUP BY r.reader_id ORDER BY total DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getDate("birth_date"),
                            rs.getString("address"),
                            rs.getInt("total")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
