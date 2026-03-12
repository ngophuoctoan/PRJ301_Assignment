-- ============================================================
-- Script: Add Foreign Keys - Patients <-> Users
-- Mục đích: Đảm bảo bảng Patients và Users được kết nối FK
-- Chạy trong SSMS với database AL_CLINIC / dental_clinic
-- ============================================================

USE [AL_CLINIC]; -- ⚠️ Đổi tên DB nếu cần
GO

-- ============================================================
-- 1. Patients.user_id → Users.user_id
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_Patients_User'
      AND parent_object_id = OBJECT_ID('dbo.Patients')
)
BEGIN
    ALTER TABLE [dbo].[Patients]
    ADD CONSTRAINT [FK_Patients_User]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id])
    ON DELETE CASCADE;

    PRINT '✅ FK_Patients_User đã được thêm thành công';
END
ELSE
BEGIN
    PRINT '⚠️  FK_Patients_User đã tồn tại, bỏ qua';
END
GO

-- ============================================================
-- 2. Doctors.user_id → Users.user_id (kiểm tra luôn)
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_Doctors_User'
      AND parent_object_id = OBJECT_ID('dbo.Doctors')
)
BEGIN
    ALTER TABLE [dbo].[Doctors]
    ADD CONSTRAINT [FK_Doctors_User]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id])
    ON DELETE CASCADE;

    PRINT '✅ FK_Doctors_User đã được thêm thành công';
END
ELSE
BEGIN
    PRINT '⚠️  FK_Doctors_User đã tồn tại, bỏ qua';
END
GO

-- ============================================================
-- 3. Staff.user_id → Users.user_id (kiểm tra luôn)
-- ============================================================
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_Staff_User'
      AND parent_object_id = OBJECT_ID('dbo.Staff')
)
BEGIN
    ALTER TABLE [dbo].[Staff]
    ADD CONSTRAINT [FK_Staff_User]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users] ([user_id])
    ON DELETE CASCADE;

    PRINT '✅ FK_Staff_User đã được thêm thành công';
END
ELSE
BEGIN
    PRINT '⚠️  FK_Staff_User đã tồn tại, bỏ qua';
END
GO

-- ============================================================
-- 4. VERIFY: Hiển thị tất cả FK hiện có trong DB
-- ============================================================
SELECT 
    fk.name                     AS [FK Name],
    OBJECT_NAME(fk.parent_object_id) AS [Table],
    COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS [Column],
    OBJECT_NAME(fk.referenced_object_id) AS [References Table],
    COL_NAME(fkc.referenced_object_id, fkc.referenced_column_id) AS [References Column]
FROM sys.foreign_keys fk
INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id
WHERE OBJECT_NAME(fk.referenced_object_id) = 'users'
   OR OBJECT_NAME(fk.parent_object_id) IN ('Patients', 'Doctors', 'Staff')
ORDER BY [Table];
GO

-- ============================================================
-- 5. KIỂM TRA: Bệnh nhân đã đăng ký nhưng chưa có bản ghi Patients
-- ============================================================
SELECT 
    u.user_id,
    u.email,
    u.role,
    u.created_at,
    CASE WHEN p.patient_id IS NULL THEN '❌ MISSING' ELSE '✅ OK' END AS [Patients Record]
FROM dbo.users u
LEFT JOIN dbo.Patients p ON u.user_id = p.user_id
WHERE u.role = 'PATIENT'
ORDER BY u.created_at DESC;
GO
