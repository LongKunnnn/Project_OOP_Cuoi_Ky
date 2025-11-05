package ui;

import dao.BorrowDAO;
import dao.ReturnDAO;
import dao.ReaderDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReturnBookPanel extends JPanel {
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final ReturnDAO returnDAO = new ReturnDAO();

    private JTextField txtReaderBarcode;
    private JLabel lblReaderName;
    private JTable tblCurrent;
    private DefaultTableModel currentModel;
    private JButton btnReturn;
    private int currentReaderId = -1;

    public ReturnBookPanel() {
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

        currentModel = new DefaultTableModel(new String[]{"Chọn","ID","Mã","Tựa","Barcode","Ngày mượn","Hạn trả"}, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex==0) return Boolean.class;
                return Object.class;
            }
            @Override public boolean isCellEditable(int row, int column) {
                return column==0;
            }
        };
        tblCurrent = new JTable(currentModel);
        add(new JScrollPane(tblCurrent), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnReturn = new JButton("Xác nhận trả sách (hàng chọn)");
        bottom.add(btnReturn);
        add(bottom, BorderLayout.SOUTH);

        btnScan.addActionListener(e -> scanReader());
        btnReturn.addActionListener(e -> processReturn());
    }

    private void scanReader() {
        String rb = txtReaderBarcode.getText().trim();
        if (rb.isEmpty()) { JOptionPane.showMessageDialog(this,"Nhập barcode thẻ độc giả"); return; }
        var reader = readerDAO.findByBarcode(rb);
        if (reader == null) { JOptionPane.showMessageDialog(this,"Không tìm thấy độc giả"); lblReaderName.setText("Chưa quét"); currentReaderId=-1; return; }
        lblReaderName.setText("Độc giả: " + reader.getName());
        currentReaderId = reader.getReaderId();
        loadCurrentBorrowed(currentReaderId);
    }

    private void loadCurrentBorrowed(int readerId) {
        currentModel.setRowCount(0);
        try (ResultSet rs = borrowDAO.getCurrentlyBorrowedResultSet(readerId)) {
            if (rs==null) return;
            while (rs.next()) {
                Object[] row = new Object[] { false, rs.getInt("id"), rs.getString("code"), rs.getString("title"), rs.getString("book_barcode"), rs.getDate("borrow_date"), rs.getDate("due_date") };
                currentModel.addRow(row);
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void processReturn() {
        if (currentReaderId <= 0) { JOptionPane.showMessageDialog(this,"Quét độc giả trước"); return; }
        List<Integer> toReturn = new ArrayList<>();
        for (int i=0;i<currentModel.getRowCount();i++) {
            Boolean sel = (Boolean) currentModel.getValueAt(i,0);
            if (sel!=null && sel) {
                int id = (Integer) currentModel.getValueAt(i,1);
                toReturn.add(id);
            }
        }
        if (toReturn.isEmpty()) { JOptionPane.showMessageDialog(this,"Chọn ít nhất 1 sách để trả"); return; }

        try {
            double totalFine = returnDAO.returnBooks(toReturn);
            JOptionPane.showMessageDialog(this, "Trả sách thành công. Tổng tiền phạt: " + totalFine);
            loadCurrentBorrowed(currentReaderId);
            if (totalFine>0) showPenaltyDialog(totalFine);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi trả: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showPenaltyDialog(double totalFine) {
        JOptionPane.showMessageDialog(this, "Phiếu phạt: Tổng tiền phạt = " + totalFine);
    }
}
