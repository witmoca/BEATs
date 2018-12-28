/* Base tables */
CREATE TABLE IF NOT EXISTS Artist(ArtistName TEXT PRIMARY KEY COLLATE NOCASE, Local INTEGER NOT NULL)
CREATE TABLE IF NOT EXISTS Song(SongId INTEGER PRIMARY KEY, Title TEXT COLLATE NOCASE, ArtistName REFERENCES Artist, UNIQUE (Title, ArtistName))
CREATE TABLE IF NOT EXISTS Playlist(PlaylistName TEXT PRIMARY KEY, TabOrder INTEGER NOT NULL UNIQUE)
CREATE TABLE IF NOT EXISTS Episode(EpisodeId INTEGER PRIMARY KEY, EpisodeDate INTEGER NOT NULL UNIQUE)
CREATE TABLE IF NOT EXISTS Section(SectionName TEXT PRIMARY KEY)
/* Relational Tables */
CREATE TABLE IF NOT EXISTS SongsInPlaylist(rowid INTEGER PRIMARY KEY, PlaylistName REFERENCES Playlist NOT NULL,Artist TEXT NOT NULL, Song TEXT NOT NULL, Comment TEXT)
CREATE TABLE IF NOT EXISTS CurrentQueue(SongOrder INTEGER PRIMARY KEY, SongId REFERENCES Song NOT NULL, Comment TEXT)
CREATE TABLE IF NOT EXISTS SongsInArchive(rowid INTEGER PRIMARY KEY, SongId REFERENCES Song NOT NULL, EpisodeId REFERENCES Episode NOT NULL, SectionName REFERENCES Section NOT NULL, Comment TEXT)
CREATE TABLE IF NOT EXISTS ccp(rowid INTEGER PRIMARY KEY, Artist TEXT NOT NULL, Song TEXT NOT NULL)