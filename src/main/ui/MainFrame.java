package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Hệ thống Quản lý Thư viện Quốc gia");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Mượn sách", new BorrowBookPanel());
        tabs.add("Trả sách", new ReturnBookPanel());
        tabs.add("Thống kê sách", new ReportBookPanel());
        tabs.add("Thống kê độc giả", new StatisticsBookPanel());

        add(tabs, BorderLayout.CENTER);
    }
}
