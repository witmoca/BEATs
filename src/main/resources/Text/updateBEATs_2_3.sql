/* Updates the table from version 1.x to 2.x */
ALTER TABLE SongsInPlaylist ADD COLUMN Highlight Boolean
UPDATE SongsInPlaylist SET Highlight = 0