package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BorrowTicket {
    private int ticketId;
    private String ticketCode;
    private int readerId;
    private String borrowDate; // yyyy-MM-dd
    private List<BorrowDetail> details = new ArrayList<>();

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public int getReaderId() { return readerId; }
    public void setReaderId(int readerId) { this.readerId = readerId; }
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public List<BorrowDetail> getDetails() { return details; }
    public void addDetail(BorrowDetail d) { details.add(d); }
}
