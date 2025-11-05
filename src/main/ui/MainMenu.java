package ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JFrame {
    public MainMenu() {
        setTitle("Quản lý Thư viện Quốc gia");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton btnBorrow = new JButton("Mượn Sách");
        JButton btnReturn = new JButton("Trả Sách");
        JButton btnStatBook = new JButton("Thống kê Sách");
        JButton btnStatReader = new JButton("Thống kê Độc Giả");

        JPanel panel = new JPanel();
        panel.add(btnBorrow);
        panel.add(btnReturn);
        panel.add(btnStatBook);
        panel.add(btnStatReader);

        add(panel);

        btnBorrow.addActionListener(e -> new BorrowBookPanel());
        btnReturn.addActionListener(e -> new ReturnBookPanel());
        btnStatBook.addActionListener(e -> new StatisticsBookPanel());
    }

    public static void main(String[] args) {
        new MainMenu().setVisible(true);
    }
}
