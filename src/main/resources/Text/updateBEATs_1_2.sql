/* Updates the table from version 1.x to 2.x */
ALTER TABLE Artist ADD COLUMN Origin TEXT NOT NULL  DEFAULT '' COLLATE NOCASE
UPDATE artist SET Origin='BE' WHERE Local=1
ALTER TABLE Artist DROP COLUMN Local