package model;

public class ReturnDetail {
    private int id;
    private int borrowDetailId;
    private String returnDate;
    private double fine;

    // getters/setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBorrowDetailId() { return borrowDetailId; }
    public void setBorrowDetailId(int borrowDetailId) { this.borrowDetailId = borrowDetailId; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public double getFine() { return fine; }
    public void setFine(double fine) { this.fine = fine; }
}
