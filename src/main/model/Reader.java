package model;

public class Reader {
    private int readerId;
    private String code;
    private String name;
    private String birthDate; // lưu chuỗi yyyy-MM-dd để đơn giản
    private String address;
    private String phone;
    private String barcode;

    // getters & setters
    public int getReaderId() { return readerId; }
    public void setReaderId(int readerId) { this.readerId = readerId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
}
