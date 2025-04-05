package org.adsc.adsc_bot.utilties;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public final class GuildPointsHandle
{
	private GuildPointsHandle()
	{
		throw new AssertionError();
	}

	private static final Map<Long, PlayerData> guildPointsMap = new HashMap<>();

	public static void initialize()
	{
		for (PlayerData data : DatabaseHandle.readPointsData())
			guildPointsMap.put(data.userID, data);
	}

	public static PlayerData getPointsData(long userID)
	{
		return guildPointsMap.computeIfAbsent(userID, k -> new PlayerData(userID));
	}

	public static void writeToDatabase()
	{
		DatabaseHandle.writePointsData(guildPointsMap.values());
	}

	@Getter
	public static class PlayerData
	{
		private final long userID;

		private long points = 0L;
		private int won = 0; //勝場
		private int lost = 0; //敗場
		private long earned = 0; //贏到的點數
		private long paid = 0; //輸掉的點數
		private int showHandWon = 0; //梭哈勝
		private int showHandLost = 0; //梭哈敗(破產)
		private long showHandEarned = 0; //贏到的點數(梭哈)
		private long showHandPaid = 0; //輸掉的點數(梭哈)

		@Setter
		private long daily; //每日簽到

		public PlayerData(long userID)
		{
			this.userID = userID;
		}

		public PlayerData(long userID, long points, int won, int lost, long earned, long paid, int showHandWon, int showHandLost, long showHandEarned, long showHandPaid, long daily)
		{
			this(userID);
			this.points = points;
			this.won = won;
			this.lost = lost;
			this.earned = earned;
			this.paid = paid;
			this.showHandWon = showHandWon;
			this.showHandLost = showHandLost;
			this.showHandEarned = showHandEarned;
			this.showHandPaid = showHandPaid;
			this.daily = daily;
		}

		public void addGame(long bet, boolean isWon, boolean isShowHand)
		{
			//記錄一場遊戲
			if (isWon)
			{
				points += bet;
				won++;
				earned += bet;
				if (isShowHand)
				{
					showHandWon++;
					showHandEarned += bet;
				}
			}
			else
			{
				points -= bet;
				lost++;
				paid += bet;
				if (isShowHand)
				{
					showHandLost++;
					showHandPaid += bet;
				}
			}
		}

		public void addPoints(long points)
		{
			this.points += points;
		}
	}
}