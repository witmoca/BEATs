package be.witmoca.BEATs;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

import be.witmoca.BEATs.model.SQLConnection;

class SqlTest {

	@Test
	void createTestDb() {
		SQLConnection Db = null;
		try {
			Db = new SQLConnection();
		} catch (SQLException e1) {
			fail("Db not created: " + e1.getLocalizedMessage());
		}
		// TODO: TEST CONTENT
		try (Statement testSet = Db.getDb().createStatement()) {
			testSet.executeUpdate("INSERT INTO Episode VALUES (1,0)");
			testSet.executeUpdate("INSERT INTO Episode VALUES (2,100)");
			testSet.executeUpdate("INSERT INTO Section VALUES ('SPC')");
			testSet.executeUpdate("INSERT INTO Section VALUES ('CL')");
			for (int i = 1; i <= 3000; i++)
				testSet.executeUpdate("INSERT INTO Artist VALUES ('a" + i + "',0)");
			for (int i = 1; i < 12000; i++) {
				testSet.executeUpdate("INSERT INTO Song VALUES (" + i + ",'s" + i + "','a" + ((i % 3000) + 1) + "')");
				testSet.executeUpdate("INSERT INTO SongsInArchive VALUES (" + i + ",1,'SPC','')");
			}
			testSet.executeUpdate("INSERT INTO Playlist VALUES ('Classic vandaag', 0)");
			testSet.executeUpdate("INSERT INTO Playlist VALUES ('Poll', 1)");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Classic vandaag', 'NA1', 'NS1', 'no comment')");
			testSet.executeUpdate(
					"INSERT INTO SongsInPlaylist VALUES ('Classic vandaag', 'NA2', 'NS2', 'local comment')");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Poll', 'PollArt1', 'PollSong1', 'local')");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Poll', 'PollArt2', 'PollSong2', 'comment')");

		} catch (SQLException e) {
			fail("Inserts failed: " + e.getLocalizedMessage());
		}
	}

}
