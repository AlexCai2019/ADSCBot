package org.adsc.adsc_bot.utilties;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class DatabaseHandle
{
	private DatabaseHandle()
	{
		throw new AssertionError();
	}

	private static final String URL = FileHandle.readConfig("db.url");
	private static final String USER = FileHandle.readConfig("db.user");
	private static final String PASSWORD = FileHandle.readConfig("db.password");

	private static final String POINTS_TABLE_NAME = "points_data";

	public static List<GuildPointsHandle.PlayerData> readPointsData()
	{
		String sql =
				"""
				SELECT user_id, points, won, lost, earned, paid, show_hand_won, show_hand_lost, show_hand_earned, show_hand_paid, daily
				FROM\s""" + POINTS_TABLE_NAME;
		try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		     PreparedStatement statement = connection.prepareStatement(sql);
		     ResultSet result = statement.executeQuery())
		{
			List<GuildPointsHandle.PlayerData> playerData = new ArrayList<>();
			while (result.next())
				playerData.add(new GuildPointsHandle.PlayerData(
						result.getLong(1), //userID
						result.getLong(2), //points
 						result.getInt(3), //won
 						result.getInt(4), //lost
						result.getLong(5), //earned
						result.getLong(6), //paid
						result.getInt(7), //showHandWon
						result.getInt(8), //showHandLost
						result.getLong(9), //showHandEarned
						result.getLong(10), //showHandPaid
						result.getLong(11))); //daily
			return playerData;
		}
		catch (SQLException e)
		{
			return Collections.emptyList();
		}
	}

	public static void writePointsData(Collection<GuildPointsHandle.PlayerData> pointsValue)
	{
		String sql = """
				INSERT INTO %s (user_id, points, won, lost, earned, paid, show_hand_won, show_hand_lost, show_hand_earned, show_hand_paid, daily)
				VALUES (?,?,?,?,?,?,?,?,?,?,?)
				ON DUPLICATE KEY UPDATE points = ?, won = ?, lost = ?, earned = ?, paid = ?, show_hand_won = ?, show_hand_lost = ?, show_hand_earned = ?, show_hand_paid = ?, daily = ?;
				""".formatted(POINTS_TABLE_NAME); //將資料寫入資料庫 如果已存在就更新
		try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		     PreparedStatement statement = connection.prepareStatement(sql))
		{
			for (GuildPointsHandle.PlayerData data : pointsValue)
			{
				//每一個set 都對應了sql裡的問號
				statement.setLong(1, data.getUserID());
				statement.setLong(2, data.getPoints());
				statement.setInt(3, data.getWon());
				statement.setInt(4, data.getLost());
				statement.setLong(5, data.getEarned());
				statement.setLong(6, data.getPaid());
				statement.setInt(7, data.getShowHandWon());
				statement.setInt(8, data.getShowHandLost());
				statement.setLong(9, data.getShowHandEarned());
				statement.setLong(10, data.getShowHandPaid());
				statement.setLong(11, data.getDaily());

				statement.setLong(12, data.getPoints());
				statement.setInt(13, data.getWon());
				statement.setInt(14, data.getLost());
				statement.setLong(15, data.getEarned());
				statement.setLong(16, data.getPaid());
				statement.setInt(17, data.getShowHandWon());
				statement.setInt(18, data.getShowHandLost());
				statement.setLong(19, data.getShowHandEarned());
				statement.setLong(20, data.getShowHandPaid());
				statement.setLong(21, data.getDaily());

				statement.addBatch(); //加入批次
			}
			statement.executeBatch();
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}