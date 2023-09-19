ALTER TABLE receipt_item ADD COLUMN receiptId INT(111);
ALTER TABLE receipt_item ADD COLUMN calculatedExpiration VARCHAR(255);
ALTER TABLE receipt_item ADD COLUMN item VARCHAR(255);
ALTER TABLE receipt_item ADD COLUMN quantity INT(255);
ALTER TABLE receipt_item ADD COLUMN itemBatch VARCHAR(255);
ALTER TABLE receipt_item ADD COLUMN expiration VARCHAR(255);