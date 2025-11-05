-- Tạo cơ sở dữ liệu
CREATE DATABASE IF NOT EXISTS library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_db;

-- Bảng độc giả
CREATE TABLE readers (
    reader_id INT AUTO_INCREMENT PRIMARY KEY,
    reader_code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    address VARCHAR(255),
    phone VARCHAR(20),
    barcode VARCHAR(50)
);

-- Bảng sách
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    book_code VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    publish_year INT,
    price DECIMAL(10,2),
    quantity INT DEFAULT 0,
    barcode VARCHAR(50),
    description TEXT
);

-- Bảng phiếu mượn
CREATE TABLE borrow_slips (
    slip_id INT AUTO_INCREMENT PRIMARY KEY,
    slip_code VARCHAR(20) UNIQUE NOT NULL,
    reader_id INT NOT NULL,
    borrow_date DATE DEFAULT (CURRENT_DATE),
    due_date DATE,
    return_date DATE,
    FOREIGN KEY (reader_id) REFERENCES readers(reader_id)
);

-- Bảng chi tiết mượn
CREATE TABLE borrow_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    slip_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE DEFAULT (CURRENT_DATE),
    due_date DATE,
    return_date DATE,
    fine DECIMAL(10,2) DEFAULT 0,
    status ENUM('BORROWED', 'RETURNED') DEFAULT 'BORROWED',
    FOREIGN KEY (slip_id) REFERENCES borrow_slips(slip_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- Trigger kiểm tra giới hạn mượn tối đa 5 sách / độc giả
DELIMITER //
CREATE TRIGGER check_borrow_limit
BEFORE INSERT ON borrow_details
FOR EACH ROW
BEGIN
    DECLARE total INT;
    SELECT COUNT(*) INTO total
    FROM borrow_details bd
    JOIN borrow_slips bs ON bd.slip_id = bs.slip_id
    WHERE bs.reader_id = (SELECT reader_id FROM borrow_slips WHERE slip_id = NEW.slip_id)
      AND bd.status = 'BORROWED';

    IF total >= 5 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Độc giả này đã mượn tối đa 5 cuốn!';
    END IF;
END;
//
DELIMITER ;

-- Bảng phạt
CREATE TABLE fines (
    fine_id INT AUTO_INCREMENT PRIMARY KEY,
    detail_id INT NOT NULL,
    fine_reason VARCHAR(255),
    fine_amount DECIMAL(10,2),
    FOREIGN KEY (detail_id) REFERENCES borrow_details(detail_id)
);

-- Dữ liệu mẫu
INSERT INTO readers (reader_code, full_name, birth_date, address, phone, barcode)
VALUES
('R001', 'Nguyễn Văn A', '1990-05-12', 'Hà Nội', '0901234567', 'BARCODE-R001'),
('R002', 'Trần Thị B', '1995-11-20', 'Đà Nẵng', '0912345678', 'BARCODE-R002');

INSERT INTO books (book_code, title, author, publish_year, price, quantity, barcode, description)
VALUES
('B001', 'Lập trình Java cơ bản', 'Nguyễn Văn C', 2020, 120000, 10, 'BARCODE-B001', 'Giáo trình học Java từ cơ bản đến nâng cao'),
('B002', 'Cơ sở dữ liệu', 'Trần Thị D', 2019, 95000, 5, 'BARCODE-B002', 'Sách về lý thuyết và thực hành cơ sở dữ liệu');
