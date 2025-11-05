package model;

public class BorrowDetail {
    private int id;
    private int ticketId;
    private int bookId;
    private String bookBarcode;
    private String borrowDate;
    private String dueDate;
    private String returnDate;
    private double fineAmount;
    private String status; // BORROWED or RETURNED

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getBookBarcode() { return bookBarcode; }
    public void setBookBarcode(String bookBarcode) { this.bookBarcode = bookBarcode; }
    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
