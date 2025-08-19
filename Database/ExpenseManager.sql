-- =====================================================
-- SCRIPT TẠO DATABASE QUẢN LÝ CHI TIÊU CÁ NHÂN (ĐÃ SỬA LỖI)
-- Hệ quản trị: SQL Server
-- Ngày tạo: 2025
-- =====================================================

-- 1. TẠO DATABASE
USE master;
GO

-- Xóa database nếu đã tồn tại
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'ExpenseManager')
BEGIN
    ALTER DATABASE ExpenseManager SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ExpenseManager;
END
GO

-- Tạo database mới (sử dụng đường dẫn mặc định)
CREATE DATABASE ExpenseManager;
GO

-- Sử dụng database vừa tạo
USE ExpenseManager;
GO

-- =====================================================
-- 2. TẠO CÁC BẢNG
-- =====================================================

-- Bảng Users (Người dùng)
CREATE TABLE Users (
    UserID INT PRIMARY KEY IDENTITY(1,1),
    Username NVARCHAR(50) UNIQUE NOT NULL,
    Password NVARCHAR(255) NOT NULL,
    FullName NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100),
    Phone NVARCHAR(20),
    CreatedDate DATETIME DEFAULT GETDATE(),
    LastLogin DATETIME,
    IsActive BIT DEFAULT 1
);

-- Bảng Categories (Danh mục thu chi)
CREATE TABLE Categories (
    CategoryID INT PRIMARY KEY IDENTITY(1,1),
    CategoryName NVARCHAR(100) NOT NULL,
    CategoryType NVARCHAR(20) CHECK (CategoryType IN ('INCOME', 'EXPENSE')) NOT NULL,
    Description NVARCHAR(255),
    IconName NVARCHAR(50),
    Color NVARCHAR(7), -- Màu hex (#FFFFFF)
    UserID INT NOT NULL,
    IsDefault BIT DEFAULT 0,
    CreatedDate DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);

-- Bảng Transactions (Giao dịch)
CREATE TABLE Transactions (
    TransactionID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    CategoryID INT NOT NULL,
    Amount DECIMAL(15,2) NOT NULL CHECK (Amount > 0),
    TransactionType NVARCHAR(20) CHECK (TransactionType IN ('INCOME', 'EXPENSE')) NOT NULL,
    Description NVARCHAR(255),
    TransactionDate DATE NOT NULL,
    Location NVARCHAR(100),
    Notes NVARCHAR(500),
    CreatedDate DATETIME DEFAULT GETDATE(),
    ModifiedDate DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);

-- Bảng Budget (Ngân sách)
CREATE TABLE Budget (
    BudgetID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    CategoryID INT NOT NULL,
    BudgetAmount DECIMAL(15,2) NOT NULL CHECK (BudgetAmount > 0),
    Month INT CHECK (Month BETWEEN 1 AND 12) NOT NULL,
    Year INT CHECK (Year >= 2020) NOT NULL,
    AlertThreshold DECIMAL(5,2) DEFAULT 80.0, -- Cảnh báo khi đạt % ngân sách
    CreatedDate DATETIME DEFAULT GETDATE(),
    ModifiedDate DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    UNIQUE (UserID, CategoryID, Month, Year)
);

-- Bảng Savings (Mục tiêu tiết kiệm)
CREATE TABLE Savings (
    SavingID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    SavingName NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255),
    TargetAmount DECIMAL(15,2) NOT NULL CHECK (TargetAmount > 0),
    CurrentAmount DECIMAL(15,2) DEFAULT 0 CHECK (CurrentAmount >= 0),
    TargetDate DATE,
    Priority INT DEFAULT 1 CHECK (Priority BETWEEN 1 AND 5), -- 1=Thấp, 5=Cao
    IsCompleted BIT DEFAULT 0,
    CreatedDate DATETIME DEFAULT GETDATE(),
    CompletedDate DATETIME,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
);

-- Bảng SavingTransactions (Giao dịch tiết kiệm)
CREATE TABLE SavingTransactions (
    SavingTransactionID INT PRIMARY KEY IDENTITY(1,1),
    SavingID INT NOT NULL,
    Amount DECIMAL(15,2) NOT NULL,
    TransactionType NVARCHAR(20) CHECK (TransactionType IN ('DEPOSIT', 'WITHDRAW')) NOT NULL,
    Description NVARCHAR(255),
    TransactionDate DATE NOT NULL,
    CreatedDate DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (SavingID) REFERENCES Savings(SavingID) ON DELETE CASCADE
);

-- Bảng RecurringTransactions (Giao dịch định kỳ)
CREATE TABLE RecurringTransactions (
    RecurringID INT PRIMARY KEY IDENTITY(1,1),
    UserID INT NOT NULL,
    CategoryID INT NOT NULL,
    Amount DECIMAL(15,2) NOT NULL,
    TransactionType NVARCHAR(20) CHECK (TransactionType IN ('INCOME', 'EXPENSE')) NOT NULL,
    Description NVARCHAR(255),
    Frequency NVARCHAR(20) CHECK (Frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')) NOT NULL,
    StartDate DATE NOT NULL,
    EndDate DATE,
    LastExecuted DATE,
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);

-- =====================================================
-- 3. TẠO INDEX ĐỂ TỐI ƯU HIỆU SUẤT
-- =====================================================

-- Index cho bảng Transactions
CREATE INDEX IX_Transactions_UserID_Date ON Transactions(UserID, TransactionDate DESC);
CREATE INDEX IX_Transactions_CategoryID ON Transactions(CategoryID);
CREATE INDEX IX_Transactions_Type_Date ON Transactions(TransactionType, TransactionDate);

-- Index cho bảng Budget
CREATE INDEX IX_Budget_UserID_Month_Year ON Budget(UserID, Month, Year);

-- Index cho bảng Categories
CREATE INDEX IX_Categories_UserID_Type ON Categories(UserID, CategoryType);

-- =====================================================
-- 4. TẠO CÁC VIEW HỮU ÍCH
-- =====================================================
GO

-- View tổng hợp giao dịch với tên danh mục
CREATE VIEW vw_TransactionDetails AS
SELECT 
    t.TransactionID,
    t.UserID,
    t.Amount,
    t.TransactionType,
    t.Description,
    t.TransactionDate,
    t.Location,
    t.CreatedDate,
    c.CategoryName,
    c.Color as CategoryColor,
    u.FullName as UserName
FROM Transactions t
INNER JOIN Categories c ON t.CategoryID = c.CategoryID
INNER JOIN Users u ON t.UserID = u.UserID;
GO

-- View tổng hợp theo tháng
CREATE VIEW vw_MonthlyReport AS
SELECT 
    UserID,
    YEAR(TransactionDate) as Year,
    MONTH(TransactionDate) as Month,
    TransactionType,
    CategoryID,
    SUM(Amount) as TotalAmount,
    COUNT(*) as TransactionCount
FROM Transactions
GROUP BY UserID, YEAR(TransactionDate), MONTH(TransactionDate), TransactionType, CategoryID;
GO

-- View số dư hiện tại
CREATE VIEW vw_CurrentBalance AS
SELECT 
    UserID,
    SUM(CASE WHEN TransactionType = 'INCOME' THEN Amount ELSE -Amount END) as CurrentBalance,
    SUM(CASE WHEN TransactionType = 'INCOME' THEN Amount ELSE 0 END) as TotalIncome,
    SUM(CASE WHEN TransactionType = 'EXPENSE' THEN Amount ELSE 0 END) as TotalExpense
FROM Transactions
GROUP BY UserID;
GO

-- =====================================================
-- 5. TẠO CÁC STORED PROCEDURE
-- =====================================================

-- Procedure lấy báo cáo theo khoảng thời gian
CREATE PROCEDURE sp_GetTransactionReport
    @UserID INT,
    @StartDate DATE,
    @EndDate DATE,
    @TransactionType NVARCHAR(20) = NULL
AS
BEGIN
    SELECT 
        t.TransactionID,
        t.Amount,
        t.TransactionType,
        t.Description,
        t.TransactionDate,
        t.Location,
        c.CategoryName,
        c.Color
    FROM Transactions t
    INNER JOIN Categories c ON t.CategoryID = c.CategoryID
    WHERE t.UserID = @UserID 
        AND t.TransactionDate BETWEEN @StartDate AND @EndDate
        AND (@TransactionType IS NULL OR t.TransactionType = @TransactionType)
    ORDER BY t.TransactionDate DESC, t.CreatedDate DESC;
END;
GO

-- Procedure kiểm tra vượt ngân sách
CREATE PROCEDURE sp_CheckBudgetAlert
    @UserID INT,
    @Month INT,
    @Year INT
AS
BEGIN
    SELECT 
        b.BudgetID,
        b.BudgetAmount,
        c.CategoryName,
        ISNULL(spent.TotalSpent, 0) as CurrentSpent,
        CASE 
            WHEN ISNULL(spent.TotalSpent, 0) > b.BudgetAmount THEN 'EXCEEDED'
            WHEN ISNULL(spent.TotalSpent, 0) > (b.BudgetAmount * b.AlertThreshold / 100) THEN 'WARNING'
            ELSE 'OK'
        END as Status,
        ((ISNULL(spent.TotalSpent, 0) / b.BudgetAmount) * 100) as UsagePercentage
    FROM Budget b
    INNER JOIN Categories c ON b.CategoryID = c.CategoryID
    LEFT JOIN (
        SELECT 
            CategoryID,
            SUM(Amount) as TotalSpent
        FROM Transactions 
        WHERE UserID = @UserID 
            AND MONTH(TransactionDate) = @Month 
            AND YEAR(TransactionDate) = @Year
            AND TransactionType = 'EXPENSE'
        GROUP BY CategoryID
    ) spent ON b.CategoryID = spent.CategoryID
    WHERE b.UserID = @UserID 
        AND b.Month = @Month 
        AND b.Year = @Year;
END;
GO

-- Procedure cập nhật tiến độ tiết kiệm
CREATE PROCEDURE sp_UpdateSavingProgress
    @SavingID INT,
    @Amount DECIMAL(15,2),
    @TransactionType NVARCHAR(20),
    @Description NVARCHAR(255)
AS
BEGIN
    BEGIN TRANSACTION;
    
    -- Thêm giao dịch tiết kiệm
    INSERT INTO SavingTransactions (SavingID, Amount, TransactionType, Description, TransactionDate)
    VALUES (@SavingID, @Amount, @TransactionType, @Description, GETDATE());
    
    -- Cập nhật số tiền hiện tại
    UPDATE Savings 
    SET CurrentAmount = CurrentAmount + 
        CASE WHEN @TransactionType = 'DEPOSIT' THEN @Amount ELSE -@Amount END
    WHERE SavingID = @SavingID;
    
    -- Kiểm tra nếu đạt mục tiêu
    UPDATE Savings 
    SET IsCompleted = 1, CompletedDate = GETDATE()
    WHERE SavingID = @SavingID AND CurrentAmount >= TargetAmount AND IsCompleted = 0;
    
    COMMIT TRANSACTION;
END;
GO

-- =====================================================
-- 6. TẠO CÁC FUNCTION HỮU ÍCH
-- =====================================================

-- Function tính tổng thu nhập theo tháng
CREATE FUNCTION fn_GetMonthlyIncome(@UserID INT, @Month INT, @Year INT)
RETURNS DECIMAL(15,2)
AS
BEGIN
    DECLARE @TotalIncome DECIMAL(15,2);
    
    SELECT @TotalIncome = ISNULL(SUM(Amount), 0)
    FROM Transactions 
    WHERE UserID = @UserID 
        AND MONTH(TransactionDate) = @Month 
        AND YEAR(TransactionDate) = @Year 
        AND TransactionType = 'INCOME';
    
    RETURN @TotalIncome;
END;
GO

-- Function tính tổng chi tiêu theo tháng
CREATE FUNCTION fn_GetMonthlyExpense(@UserID INT, @Month INT, @Year INT)
RETURNS DECIMAL(15,2)
AS
BEGIN
    DECLARE @TotalExpense DECIMAL(15,2);
    
    SELECT @TotalExpense = ISNULL(SUM(Amount), 0)
    FROM Transactions 
    WHERE UserID = @UserID 
        AND MONTH(TransactionDate) = @Month 
        AND YEAR(TransactionDate) = @Year 
        AND TransactionType = 'EXPENSE';
    
    RETURN @TotalExpense;
END;
GO

-- =====================================================
-- 7. TẠO TRIGGER
-- =====================================================

-- Trigger cập nhật ModifiedDate khi sửa Transaction
CREATE TRIGGER tr_UpdateTransactionModified 
ON Transactions
AFTER UPDATE
AS
BEGIN
    UPDATE Transactions 
    SET ModifiedDate = GETDATE()
    FROM Transactions t
    INNER JOIN inserted i ON t.TransactionID = i.TransactionID;
END;
GO

-- Trigger cập nhật ModifiedDate khi sửa Budget
CREATE TRIGGER tr_UpdateBudgetModified 
ON Budget
AFTER UPDATE
AS
BEGIN
    UPDATE Budget 
    SET ModifiedDate = GETDATE()
    FROM Budget b
    INNER JOIN inserted i ON b.BudgetID = i.BudgetID;
END;
GO

-- =====================================================
-- 8. THÊM DỮ LIỆU MẪU
-- =====================================================

-- Thêm user mẫu (password đã được hash)
INSERT INTO Users (Username, Password, FullName, Email, Phone) VALUES 
('admin', 'admin123', N'Nguyễn Văn Admin', 'admin@email.com', '0123456789'),
('user1', 'user123', N'Trần Thị User', 'user1@email.com', '0987654321');

-- Lấy UserID để sử dụng
DECLARE @AdminID INT = (SELECT UserID FROM Users WHERE Username = 'admin');
DECLARE @User1ID INT = (SELECT UserID FROM Users WHERE Username = 'user1');

-- Thêm danh mục mặc định cho admin
INSERT INTO Categories (CategoryName, CategoryType, Description, IconName, Color, UserID, IsDefault) VALUES 
-- Danh mục thu nhập
(N'Lương chính', 'INCOME', N'Lương từ công việc chính', 'salary.png', '#4CAF50', @AdminID, 1),
(N'Lương phụ', 'INCOME', N'Thu nhập từ công việc phụ', 'part_time.png', '#8BC34A', @AdminID, 1),
(N'Đầu tư', 'INCOME', N'Lợi nhuận từ đầu tư', 'investment.png', '#FFC107', @AdminID, 1),
(N'Khác', 'INCOME', N'Thu nhập khác', 'other.png', '#FF9800', @AdminID, 1),

-- Danh mục chi tiêu
(N'Ăn uống', 'EXPENSE', N'Chi phí ăn uống hàng ngày', 'food.png', '#F44336', @AdminID, 1),
(N'Xăng xe', 'EXPENSE', N'Chi phí xăng xe, di chuyển', 'transport.png', '#2196F3', @AdminID, 1),
(N'Mua sắm', 'EXPENSE', N'Mua quần áo, đồ dùng', 'shopping.png', '#E91E63', @AdminID, 1),
(N'Giải trí', 'EXPENSE', N'Xem phim, du lịch, vui chơi', 'entertainment.png', '#9C27B0', @AdminID, 1),
(N'Hóa đơn', 'EXPENSE', N'Điện, nước, internet, điện thoại', 'bills.png', '#FF5722', @AdminID, 1),
(N'Y tế', 'EXPENSE', N'Khám bệnh, mua thuốc', 'health.png', '#00BCD4', @AdminID, 1),
(N'Giáo dục', 'EXPENSE', N'Học phí, sách vở', 'education.png', '#795548', @AdminID, 1),
(N'Tiết kiệm', 'EXPENSE', N'Gửi tiết kiệm, đầu tư', 'savings.png', '#607D8B', @AdminID, 1);

-- Copy danh mục cho user1
INSERT INTO Categories (CategoryName, CategoryType, Description, IconName, Color, UserID, IsDefault)
SELECT CategoryName, CategoryType, Description, IconName, Color, @User1ID, IsDefault
FROM Categories WHERE UserID = @AdminID;

-- Thêm giao dịch mẫu cho admin (3 tháng gần đây)
DECLARE @CategoryIncome1 INT = (SELECT CategoryID FROM Categories WHERE CategoryName = N'Lương chính' AND UserID = @AdminID);
DECLARE @CategoryFood INT = (SELECT CategoryID FROM Categories WHERE CategoryName = N'Ăn uống' AND UserID = @AdminID);
DECLARE @CategoryTransport INT = (SELECT CategoryID FROM Categories WHERE CategoryName = N'Xăng xe' AND UserID = @AdminID);
DECLARE @CategoryEntertainment INT = (SELECT CategoryID FROM Categories WHERE CategoryName = N'Giải trí' AND UserID = @AdminID);
DECLARE @CategoryBills INT = (SELECT CategoryID FROM Categories WHERE CategoryName = N'Hóa đơn' AND UserID = @AdminID);

-- Giao dịch tháng 8/2025
INSERT INTO Transactions (UserID, CategoryID, Amount, TransactionType, Description, TransactionDate, Location) VALUES 
-- Thu nhập
(@AdminID, @CategoryIncome1, 15000000, 'INCOME', N'Lương tháng 8', '2025-08-01', N'Công ty ABC'),

-- Chi tiêu
(@AdminID, @CategoryFood, 150000, 'EXPENSE', N'Ăn trưa', '2025-08-02', N'Quán cơm Bình dân'),
(@AdminID, @CategoryTransport, 200000, 'EXPENSE', N'Đổ xăng xe máy', '2025-08-03', N'Cửa hàng xăng dầu'),
(@AdminID, @CategoryFood, 80000, 'EXPENSE', N'Ăn sáng', '2025-08-04', N'Quán phở'),
(@AdminID, @CategoryBills, 500000, 'EXPENSE', N'Tiền điện tháng 7', '2025-08-05', N'Công ty điện lực'),
(@AdminID, @CategoryEntertainment, 300000, 'EXPENSE', N'Xem phim', '2025-08-06', N'CGV Vincom'),
(@AdminID, @CategoryFood, 120000, 'EXPENSE', N'Ăn tối', '2025-08-07', N'Nhà hàng Hương Việt'),
(@AdminID, @CategoryTransport, 50000, 'EXPENSE', N'Grab xe', '2025-08-08', N'Grab'),
(@AdminID, @CategoryFood, 95000, 'EXPENSE', N'Cà phê với bạn', '2025-08-09', N'Highlands Coffee'),
(@AdminID, @CategoryBills, 300000, 'EXPENSE', N'Tiền internet', '2025-08-10', N'FPT Telecom');

-- Giao dịch tháng 7/2025
INSERT INTO Transactions (UserID, CategoryID, Amount, TransactionType, Description, TransactionDate, Location) VALUES
(@AdminID, @CategoryIncome1, 15000000, 'INCOME', N'Lương tháng 7', '2025-07-01', N'Công ty ABC'),
(@AdminID, @CategoryFood, 2800000, 'EXPENSE', N'Chi tiêu ăn uống tháng 7', '2025-07-15', N'Tổng hợp'),
(@AdminID, @CategoryTransport, 800000, 'EXPENSE', N'Chi phí đi lại tháng 7', '2025-07-15', N'Tổng hợp'),
(@AdminID, @CategoryEntertainment, 1200000, 'EXPENSE', N'Giải trí tháng 7', '2025-07-15', N'Tổng hợp'),
(@AdminID, @CategoryBills, 1500000, 'EXPENSE', N'Hóa đơn tháng 7', '2025-07-15', N'Tổng hợp');

-- Giao dịch tháng 6/2025
INSERT INTO Transactions (UserID, CategoryID, Amount, TransactionType, Description, TransactionDate, Location) VALUES
(@AdminID, @CategoryIncome1, 15000000, 'INCOME', N'Lương tháng 6', '2025-06-01', N'Công ty ABC'),
(@AdminID, @CategoryFood, 2500000, 'EXPENSE', N'Chi tiêu ăn uống tháng 6', '2025-06-15', N'Tổng hợp'),
(@AdminID, @CategoryTransport, 750000, 'EXPENSE', N'Chi phí đi lại tháng 6', '2025-06-15', N'Tổng hợp'),
(@AdminID, @CategoryEntertainment, 1000000, 'EXPENSE', N'Giải trí tháng 6', '2025-06-15', N'Tổng hợp'),
(@AdminID, @CategoryBills, 1400000, 'EXPENSE', N'Hóa đơn tháng 6', '2025-06-15', N'Tổng hợp');

-- Thêm ngân sách mẫu cho tháng 8/2025
INSERT INTO Budget (UserID, CategoryID, BudgetAmount, Month, Year, AlertThreshold) VALUES 
(@AdminID, @CategoryFood, 3000000, 8, 2025, 80.0),
(@AdminID, @CategoryTransport, 1000000, 8, 2025, 85.0),
(@AdminID, @CategoryEntertainment, 1500000, 8, 2025, 75.0),
(@AdminID, @CategoryBills, 2000000, 8, 2025, 90.0);

-- Thêm mục tiêu tiết kiệm mẫu
INSERT INTO Savings (UserID, SavingName, Description, TargetAmount, CurrentAmount, TargetDate, Priority) VALUES 
(@AdminID, N'Mua xe máy mới', N'Tiết kiệm để mua Honda Vision', 50000000, 15000000, '2025-12-31', 4),
(@AdminID, N'Du lịch Đà Lạt', N'Chuyến du lịch gia đình', 10000000, 3000000, '2025-10-15', 3),
(@AdminID, N'Quỹ khẩn cấp', N'Dự phòng cho các tình huống khẩn cấp', 30000000, 8000000, '2026-06-30', 5);

-- Thêm giao dịch tiết kiệm mẫu
DECLARE @SavingXeMay INT = (SELECT SavingID FROM Savings WHERE SavingName = N'Mua xe máy mới' AND UserID = @AdminID);
DECLARE @SavingDuLich INT = (SELECT SavingID FROM Savings WHERE SavingName = N'Du lịch Đà Lạt' AND UserID = @AdminID);

INSERT INTO SavingTransactions (SavingID, Amount, TransactionType, Description, TransactionDate) VALUES 
(@SavingXeMay, 5000000, 'DEPOSIT', N'Gửi tiết kiệm đầu tháng', '2025-08-01'),
(@SavingXeMay, 3000000, 'DEPOSIT', N'Tiền thưởng', '2025-07-15'),
(@SavingDuLich, 1000000, 'DEPOSIT', N'Tiết kiệm du lịch', '2025-08-01'),
(@SavingDuLich, 500000, 'DEPOSIT', N'Tiết kiệm hàng tuần', '2025-08-08');

-- Thêm giao dịch định kỳ mẫu
INSERT INTO RecurringTransactions (UserID, CategoryID, Amount, TransactionType, Description, Frequency, StartDate, EndDate) VALUES 
(@AdminID, @CategoryIncome1, 15000000, 'INCOME', N'Lương hàng tháng', 'MONTHLY', '2025-01-01', '2025-12-31'),
(@AdminID, @CategoryBills, 500000, 'EXPENSE', N'Tiền điện hàng tháng', 'MONTHLY', '2025-01-01', '2025-12-31'),
(@AdminID, @CategoryBills, 300000, 'EXPENSE', N'Tiền internet hàng tháng', 'MONTHLY', '2025-01-01', '2025-12-31');

PRINT 'Database ExpenseManager đã được tạo thành công!';
PRINT 'Dữ liệu mẫu đã được thêm vào.';
PRINT 'Sẵn sàng để phát triển ứng dụng Java.';

-- =====================================================
-- 9. CÁC QUERY KIỂM TRA DỮ LIỆU
-- =====================================================

-- Kiểm tra users
SELECT * FROM Users;

-- Kiểm tra categories  
SELECT * FROM Categories ORDER BY UserID, CategoryType, CategoryName;

-- Kiểm tra transactions
SELECT * FROM vw_TransactionDetails ORDER BY TransactionDate DESC;

-- Kiểm tra số dư hiện tại
SELECT 
    u.FullName,
    cb.CurrentBalance,
    cb.TotalIncome,
    cb.TotalExpense,
    FORMAT(cb.CurrentBalance, 'N0') + ' VNĐ' as FormattedBalance
FROM vw_CurrentBalance cb
INNER JOIN Users u ON cb.UserID = u.UserID;

-- Test procedure kiểm tra ngân sách
EXEC sp_CheckBudgetAlert 1, 8, 2025;

-- Kiểm tra tiến độ tiết kiệm
SELECT 
    SavingName,
    TargetAmount,
    CurrentAmount,
    (CurrentAmount * 100.0 / TargetAmount) as CompletionPercentage,
    TargetDate,
    DATEDIFF(DAY, GETDATE(), TargetDate) as DaysRemaining
FROM Savings 
WHERE UserID = 1;