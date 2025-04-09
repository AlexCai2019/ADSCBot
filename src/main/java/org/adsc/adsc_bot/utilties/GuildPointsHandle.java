package org.adsc.adsc_bot.utilties;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.adsc.adsc_bot.ADSC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuildPointsHandle
{
	private GuildPointsHandle()
	{
		throw new AssertionError();
	}

	private static boolean changed = true; //清單是否更改過 用於決定/points rank時是否重新排序
	private static final Map<Long, PlayerData> guildPointsMap = new HashMap<>();
	private static final Map<Long, MineData> guildMineMap = new HashMap<>();

	private static final List<PlayerData> sortedPointsList = new ArrayList<>();

	public static void initialize()
	{
		JDA jda = ADSC.getJDA();
		for (PlayerData data : DatabaseHandle.readPointsData())
		{
			jda.retrieveUserById(data.userID).queue(user -> data.name = user.getEffectiveName());
			guildPointsMap.put(data.userID, data); //初始化點數
		}
		sortedPointsList.addAll(guildPointsMap.values());

		for (MineData data : DatabaseHandle.readMineData())
			guildMineMap.put(data.userID, data); //初始化挖礦紀錄
	}

	public static PlayerData getPointsData(long userID)
	{
		PlayerData pointsData = guildPointsMap.get(userID);
		if (pointsData != null)
			return pointsData;

		//如果不存在該玩家
		changed = true;
		PlayerData newData = new PlayerData(userID);
		ADSC.getJDA().retrieveUserById(userID).queue(user -> newData.name = user.getEffectiveName());
		guildPointsMap.put(userID, newData);
		sortedPointsList.add(newData);
		return newData;
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

	private static String lastReply; //上一次回覆過的字串
	private static int lastPage = -1; //上一次查看的頁面
	private static long lastUser = -1L; //上一次使用指令的使用者
	private static int maxPage; //目前有幾頁

	public static String getRankString(long userID, int inputPage)
	{
		int page;
		//假設總共有27位使用者 (27 - 1) / 10 + 1 = 3 總共有3頁
		maxPage = (sortedPointsList.size() - 1) / 10 + 1;
		if (inputPage > maxPage) //超出範圍
			page = maxPage; //同上例子 就改成顯示第3頁
		else if (inputPage < 0) //-1 = 最後一頁, -2 = 倒數第二頁 負太多就變第一頁
			page = (-inputPage < maxPage) ? maxPage + inputPage + 1 : 1;
		else
			page = inputPage;

		boolean sameUser = userID == lastUser;
		lastUser = userID;
		boolean samePage = page == lastPage;
		lastPage = page; //換過頁了

		if (!changed) //距離上一次排序 沒有任何變動
		{
			if (!samePage || !sameUser) //有換頁 或 不是同一位使用者
				lastReply = replyString(userID, page); //重新建立字串
			return lastReply; //省略排序
		}

		//排序
		sortedPointsList.sort((user1, user2) -> Long.compare(user2.getPoints(), user1.getPoints())); //方塊較多的在前面 方塊較少的在後面

		changed = false; //已經排序過了
		return lastReply = replyString(userID, page);
	}

	private static String replyString(long userID, int page)
	{
		//page 從1開始
		int startElement = (page - 1) * 10; //開始的那個元素
		int endElement = Math.min(startElement + 10, sortedPointsList.size()); //結束的那個元素 不可比list總長還長

		List<PlayerData> ranking = sortedPointsList.subList(startElement, endElement); //要查看的那一頁
		PlayerData myData = getPointsData(userID);

		StringBuilder rankBuilder = new StringBuilder("```ansi\n點數排名\n--------------------\n你是第 \u001B[36m")
				.append(listBinarySearch(myData.points)).append("\u001B[0m 名，擁有 \u001B[36m")
				.append(String.format("%,d", myData.points)).append("\u001B[0m 點。\n\n");

		for (int i = 0, add = page * 10 - 9, rankingSize = ranking.size(); i < rankingSize; i++) //add = (page - 1) * 10 + 1
		{
			PlayerData rank = ranking.get(i);
			rankBuilder.append("[\u001B[36m")
					.append(String.format("%03d", add + i))
					.append("\u001B[0m]\t")
					.append(rank.name)
					.append(": \u001B[36m")
					.append(String.format("%,d", rank.points))
					.append("\u001B[0m\n");
		}

		return rankBuilder.append("\n--------------------\n")
				.append(page)
				.append(" / ")
				.append(maxPage)
				.append("\n```")
				.toString();
	}

	private static int listBinarySearch(long points)
	{
		//找出這點數的位置
		long midValue;
		for (int low = 0, middle, high = sortedPointsList.size() - 1; low <= high;)
		{
			middle = (low + high) >>> 1;
			midValue = sortedPointsList.get(middle).getPoints();

			if (midValue < points)
				high = middle - 1;
			else if (midValue > points)
				low = middle + 1;
			else
				return middle + 1;
		}
		return 0;
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

		@Setter
		private String name;

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
				addPoints(bet);
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
				subPoints(bet);
				lost++;
				paid += bet;
				if (isShowHand)
				{
					showHandLost++;
					showHandPaid += bet;
				}
			}
		}

		public void addPoints(long add)
		{
			setPoints(points + add);
		}

		public void subPoints(long sub)
		{
			setPoints(points - sub);
		}

		private static final long ROLE_CHANGE_GAP = 100000;
		public void setPoints(long newValue)
		{
			changed = true; //有更動

			long oldValue = points; //舊數值
			points = newValue;

			boolean less = newValue < ROLE_CHANGE_GAP;
			if ((oldValue < ROLE_CHANGE_GAP) == less) //沒有跨過門檻
				return;

			Guild adsc = ADSC.getJDA().getGuildById(Constants.SERVER_ID);
			if (adsc == null)
				return;

			Role godOfGamblersRole = adsc.getRoleById(Constants.GOD_OF_GAMBLERS_ROLE_ID); //賭神身分組
			if (godOfGamblersRole == null) //找不到賭神身分組
				return;

			adsc.retrieveMemberById(userID).queue(member -> //根據userID 從群組中找到這名成員
			{
				boolean hasRole = member.getRoles().contains(godOfGamblersRole);
				if (!less && !hasRole) //大於等於ROLE_CHANGE_GAP 且沒有身分組
					adsc.addRoleToMember(member, godOfGamblersRole).queue(); //給予賭神身分組
				else if (less && hasRole) //小於ROLE_CHANGE_GAP 且有身分組
					adsc.removeRoleFromMember(member, godOfGamblersRole).queue(); //剝奪賭神身分組
			});
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

		private final OreStats[] allStats =
		{
			diamond, gold, iron, coal, stone,
			air, easterEgg, cannon, darkNight
		};

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

		public int getTotalMine()
		{
			int totalMine = 0;
			for (OreStats stats : allStats)
				totalMine += stats.value;
			return totalMine;
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