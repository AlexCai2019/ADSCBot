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
	private static final Map<Long, MineData> guildMineMap = new HashMap<>();

	public static void initialize()
	{
		for (PlayerData data : DatabaseHandle.readPointsData())
			guildPointsMap.put(data.userID, data); //初始化點數
		for (MineData data : DatabaseHandle.readMineData())
			guildMineMap.put(data.userID, data); //初始化挖礦紀錄
	}

	public static PlayerData getPointsData(long userID)
	{
		return guildPointsMap.computeIfAbsent(userID, k -> new PlayerData(userID));
	}

	public static MineData getMineData(long userID)
	{
		return guildMineMap.computeIfAbsent(userID, k -> new MineData(userID));
	}

	public static void writeToDatabase()
	{
		DatabaseHandle.writePointsData(guildPointsMap.values());
		DatabaseHandle.writeMineData(guildMineMap.values());
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
		private long daily = 0; //每日簽到

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

		public void subPoints(long points)
		{
			this.points -= points;
		}
	}

	@Getter
	public static class MineData
	{
		private final long userID;

		@Setter
		private long cooldown = 0; //冷卻

		private final OreStats diamond = new OreStats();
		private final OreStats gold = new OreStats();
		private final OreStats iron = new OreStats();
		private final OreStats coal = new OreStats();
		private final OreStats stone = new OreStats();
		private final OreStats air = new OreStats();
		private final OreStats easterEgg = new OreStats();
		private final OreStats cannon = new OreStats();
		private final OreStats darkNight = new OreStats();

		public MineData(long userID)
		{
			this.userID = userID;
		}

		public MineData(long userID, int diamond, int gold, int iron, int coal, int stone, int air, int easterEgg, int cannon, int darkNight, long cooldown)
		{
			this(userID);
			this.diamond.setValue(diamond);
			this.gold.setValue(gold);
			this.iron.setValue(iron);
			this.coal.setValue(coal);
			this.stone.setValue(stone);
			this.air.setValue(air);
			this.easterEgg.setValue(easterEgg);
			this.cannon.setValue(cannon);
			this.darkNight.setValue(darkNight);
			this.cooldown = cooldown;
		}
	}

	@Setter
	@Getter
	public static class OreStats
	{
		private int value;

		public void addValue()
		{
			value++;
		}
	}
}