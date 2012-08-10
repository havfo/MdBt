package net.fosstveit.mdbt.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.fosstveit.mdbt.utils.MdBtQuad;

public class DataBase {

	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";
	private String dbName = "derbyDB";

	private Connection conn;
	private PreparedStatement psInsert;
	private Statement s;
	private ResultSet rs;

	public DataBase() {
		setup();
	}

	private void setup() {
		try {
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(protocol + dbName
					+ ";create=true");
			conn.setAutoCommit(true);
			s = conn.createStatement();
			// s.execute("DROP TABLE words");
			// s.execute("CREATE TABLE words (id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), word varchar(256))");
			// s.execute("DROP TABLE quads");
			// s.execute("CREATE TABLE quads (id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), word1 varchar(256), word2 varchar(256), word3 varchar(256), word4 varchar(256), canstart int, canend int)");
			// s.execute("DROP TABLE nextword");
			// s.execute("CREATE TABLE nextword (id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), word varchar(256), quad int)");
			// s.execute("DROP TABLE previousword");
			// s.execute("CREATE TABLE previousword (id INT not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), word varchar(256), quad int)");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasWord(String word) {
		try {
			rs = s.executeQuery("SELECT * FROM words WHERE word='"
					+ word.replaceAll("'", "''"));
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void addWord(String word) {
		try {
			psInsert = conn
					.prepareStatement("insert into words (word) values (?)");

			psInsert.setString(1, word);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasQuad(MdBtQuad quad) {
		try {
			int canstart = 0;
			int canend = 0;

			if (quad.canStart()) {
				canstart++;
			}

			if (quad.canEnd()) {
				canend++;
			}

			rs = s.executeQuery("SELECT * FROM quads WHERE word1='"
					+ quad.getToken(0).replaceAll("'", "''") + "' AND word2='"
					+ quad.getToken(1).replaceAll("'", "''") + "' AND word3='"
					+ quad.getToken(2).replaceAll("'", "''") + "' AND word4='"
					+ quad.getToken(3).replaceAll("'", "''")
					+ "' AND canstart=" + canstart + " AND canend=" + canend);
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void addQuad(MdBtQuad quad) {
		try {
			psInsert = conn
					.prepareStatement("INSERT INTO quads (word1, word2, word3, word4, canstart, canend) VALUES (?, ?, ?, ?, ?, ?)");

			int canstart = 0;
			int canend = 0;

			if (quad.canStart()) {
				canstart++;
			}

			if (quad.canEnd()) {
				canend++;
			}

			psInsert.setString(1, quad.getToken(0));
			psInsert.setString(2, quad.getToken(1));
			psInsert.setString(3, quad.getToken(2));
			psInsert.setString(4, quad.getToken(3));
			psInsert.setInt(5, canstart);
			psInsert.setInt(6, canend);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// public static void main(String[] args) {
	//
	//
	// sentence = sentence.trim();
	// String[] parts = sentence.split("(?!^)\\b");
	//
	// if (parts.length >= 4) {
	// for (int i = 0; i < parts.length - 3; i++) {
	// rs = s.executeQuery("SELECT * from words where word='"
	// + parts[i].replaceAll("'", "''")
	// + "' OR word='"
	// + parts[i + 1].replaceAll("'", "''")
	// + "' OR word='"
	// + parts[i + 2].replaceAll("'", "''")
	// + "' OR word='"
	// + parts[i + 3].replaceAll("'", "''") + "'");
	//
	// int[] wordId = new int[4];
	// int ind = 0;
	// while (rs.next()) {
	// wordId[ind] = rs.getInt("id");
	// }
	//
	// rs = s.executeQuery("SELECT id from quads where word1="
	// + wordId[0]
	// + " AND word2="
	// + wordId[1]
	// + " AND word3="
	// + wordId[2]
	// + " AND word4="
	// + wordId[3]);
	//
	// rs.next();
	// int quadid = rs.getInt("id");
	//
	// if (i > 0) {
	// rs = s.executeQuery("SELECT * from words WHERE word='"
	// + parts[i - 1].replaceAll("'", "''")
	// + "'");
	// rs.next();
	// int previousToken = rs.getInt("id");
	//
	// rs = s.executeQuery("SELECT * from previousword WHERE quad="
	// + quadid + " and word=" + previousToken);
	//
	// if (!rs.next()) {
	// psInsert = conn
	// .prepareStatement("insert into previousword (word, quad) values (?, ?)");
	//
	// psInsert.setInt(1, previousToken);
	// psInsert.setInt(2, quadid);
	// psInsert.executeUpdate();
	// }
	// }
	//
	// if (i < parts.length - 4) {
	// rs = s.executeQuery("SELECT * from words WHERE word='"
	// + parts[i + 4].replaceAll("'", "''")
	// + "'");
	// rs.next();
	//
	// int nexttoken = rs.getInt("id");
	//
	// rs = s.executeQuery("SELECT * from nextword WHERE quad="
	// + quadid + " and word=" + nexttoken);
	//
	// if (!rs.next()) {
	// psInsert = conn
	// .prepareStatement("insert into nextword (word, quad) values (?, ?)");
	//
	// psInsert.setInt(1, nexttoken);
	// psInsert.setInt(2, quadid);
	// psInsert.executeUpdate();
	// }
	// }
	//
	// }
	// }
	// }
	//
	// fir.close();
	// bis.close();
	//
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// // psInsert = conn
	// // .prepareStatement("insert into location values (?, ?)");
	// //
	// // psInsert.setInt(1, 1956);
	// // psInsert.setString(2, "Webster St.");
	// // psInsert.executeUpdate();
	// //
	// // psInsert.setInt(1, 1910);
	// // psInsert.setString(2, "Union St.");
	// // psInsert.executeUpdate();
	// //
	// // psUpdate = conn
	// // .prepareStatement("update location set num=?, addr=? where num=?");
	// //
	// // psUpdate.setInt(1, 180);
	// // psUpdate.setString(2, "Grand Ave.");
	// // psUpdate.setInt(3, 1956);
	// // psUpdate.executeUpdate();
	// //
	// // psUpdate.setInt(1, 300);
	// // psUpdate.setString(2, "Lakeshore Ave.");
	// // psUpdate.setInt(3, 180);
	// // psUpdate.executeUpdate();
	//
	// /*
	// * We select the rows and verify the results.
	// */
	// // rs =
	// // s.executeQuery("SELECT num, addr FROM location ORDER BY num");
	//
	// // delete the table
	// // s.execute("drop table location");
	//
	// DriverManager.getConnection("jdbc:derby:;shutdown=true");
	// } catch (Exception e) {
	// e.printStackTrace();
	// }

	public static void main(String[] args) {
		new DataBase();
	}
}