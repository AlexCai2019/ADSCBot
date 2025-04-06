package org.adsc.adsc_bot.utilties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(DatabaseHandle.class);

	private static final String URL = FileHandle.readConfig("db.url");
	private static final String USER = FileHandle.readConfig("db.user");
	private static final String PASSWORD = FileHandle.readConfig("db.password");

	private static final String POINTS_TABLE_NAME = "points_data";
	private static final String MINE_TABLE_NAME = "mine_data";

	static List<GuildPointsHandle.PlayerData> readPointsData()
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
			logger.error("讀取資料庫時發生問題！", e);
			return Collections.emptyList();
		}
	}

	static List<GuildPointsHandle.MineData> readMineData()
	{
		String sql =
				"""
				SELECT user_id, diamond, gold, iron, coal, stone, air, easter_egg, cannon, dark_night, cooldown
				FROM\s""" + MINE_TABLE_NAME;
		try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		     PreparedStatement statement = connection.prepareStatement(sql);
		     ResultSet result = statement.executeQuery())
		{
			List<GuildPointsHandle.MineData> mineData = new ArrayList<>();
			while (result.next())
				mineData.add(new GuildPointsHandle.MineData(
						result.getLong(1), //userID
						result.getInt(2), //diamond
						result.getInt(3), //gold
						result.getInt(4), //iron
						result.getInt(5), //coal
						result.getInt(6), //stone
						result.getInt(7), //air
						result.getInt(8), //easter_egg
						result.getInt(9), //cannon
						result.getInt(10), //dark_night
						result.getLong(11))); //cooldown
			return mineData;
		}
		catch (SQLException e)
		{
			logger.error("讀取資料庫時發生問題！", e);
			return Collections.emptyList();
		}
	}

	static void writePointsData(Collection<GuildPointsHandle.PlayerData> pointsValue)
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
			statement.executeBatch(); //執行批次
		}
		catch (SQLException e)
		{
			logger.error("寫入資料庫時發生問題！", e);
		}
	}

	static void writeMineData(Collection<GuildPointsHandle.MineData> mineValue)
	{
		String sql = """
				INSERT INTO %s (user_id, diamond, gold, iron, coal, stone, air, easter_egg, cannon, dark_night, cooldown)
				VALUES (?,?,?,?,?,?,?,?,?,?,?)
				ON DUPLICATE KEY UPDATE diamond = ?, gold = ?, iron = ?, coal = ?, stone = ?, air = ?, easter_egg = ?, cannon = ?, dark_night = ?, cooldown = ?;
				""".formatted(MINE_TABLE_NAME); //將資料寫入資料庫 如果已存在就更新
		try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
		     PreparedStatement statement = connection.prepareStatement(sql))
		{
			for (GuildPointsHandle.MineData data : mineValue)
			{
				//每一個set 都對應了sql裡的問號
				statement.setLong(1, data.getUserID());
				statement.setInt(2, data.getDiamond().getValue());
				statement.setInt(3, data.getGold().getValue());
				statement.setInt(4, data.getIron().getValue());
				statement.setInt(5, data.getCoal().getValue());
				statement.setInt(6, data.getStone().getValue());
				statement.setInt(7, data.getAir().getValue());
				statement.setInt(8, data.getEasterEgg().getValue());
				statement.setInt(9, data.getCannon().getValue());
				statement.setInt(10, data.getDarkNight().getValue());
				statement.setLong(11, data.getCooldown());

				statement.setInt(12, data.getDiamond().getValue());
				statement.setInt(13, data.getGold().getValue());
				statement.setInt(14, data.getIron().getValue());
				statement.setInt(15, data.getCoal().getValue());
				statement.setInt(16, data.getStone().getValue());
				statement.setInt(17, data.getAir().getValue());
				statement.setInt(18, data.getEasterEgg().getValue());
				statement.setInt(19, data.getCannon().getValue());
				statement.setInt(20, data.getDarkNight().getValue());
				statement.setLong(21, data.getCooldown());

				statement.addBatch(); //加入批次
			}
			statement.executeBatch(); //執行批次
		}
		catch (SQLException e)
		{
			logger.error("寫入資料庫時發生問題！", e);
		}
	}
}