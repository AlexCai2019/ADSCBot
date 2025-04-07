package org.adsc.adsc_bot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.adsc.adsc_bot.utilties.GuildPointsHandle;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class PointsCommand extends HasSubcommands
{
	public static final String POINTS = "points";

	public static final String VIEW = "view";
	public static final String BET = "bet";
	public static final String MINE = "mine";
	public static final String DAILY = "daily";
	public static final String RANK = "rank";

	public PointsCommand()
	{
		super(5);

		subcommands.put(VIEW, new ViewCommand());
		subcommands.put(BET, new BetCommand());
		subcommands.put(MINE, new MineCommand());
		subcommands.put(DAILY, new DailyCommand());
		subcommands.put(RANK, new RankCommand());
	}

	private int useTime = 0;
	@Override
	public void commandProcess(SlashCommandInteractionEvent event)
	{
		super.commandProcess(event);

		if (++useTime < 10) //指令使用了10次
			return;
		GuildPointsHandle.writeToDatabase(); //寫入資料庫
		useTime = 0;
	}

	private static class ViewCommand implements ICommand
	{
		@Override
		public void commandProcess(SlashCommandInteractionEvent event)
		{
			User user = event.getUser();
			if (user.isBot() || user.isSystem())
			{
				event.reply("你不能這樣做！").setEphemeral(true).queue();
				return;
			}

			GuildPointsHandle.PlayerData playerData = GuildPointsHandle.getPointsData(user.getIdLong());

			if (!event.getOption("detail", false, OptionMapping::getAsBoolean)) //不顯示細節
			{
				event.reply("你目前有 " + playerData.getPoints() + " 點").queue();
				return;
			}

			int won = playerData.getWon();
			int lost = playerData.getLost();
			int showHandWon = playerData.getShowHandWon();
			int showHandLost = playerData.getShowHandLost();
			event.reply("你目前有 " + playerData.getPoints() + " 點\n" +
					"- 賭了 " + (won + lost) + " 次\n" +
						" - 贏了 " + won + " 次 賺了 " + playerData.getEarned() + " 點\n" +
						" - 輸了 " + lost + " 次 賠了 " + playerData.getPaid() + " 點\n" +
						" - 梭哈了 " + (showHandWon + showHandLost) + " 次\n" +
							"  - 贏了 " + showHandWon + " 次 賺了 " +  playerData.getShowHandEarned() +" 點\n" +
							"  - 輸了 " + showHandLost + " 次 賠了 " + playerData.getShowHandPaid() + " 點").queue();
		}
	}

	private static class BetCommand implements ICommand
	{
		private static final Pattern BET_NUMBER_REGEX = Pattern.compile("\\d{1,18}"); //防止輸入超過Long.MAX_VALUE
		private static final Pattern BET_PERCENT_REGEX = Pattern.compile("\\d{1,4}%"); //防止輸入超過Short.MAX_VALUE

		@Override
		public void commandProcess(SlashCommandInteractionEvent event)
		{
			User user = event.getUser();
			if (user.isBot() || user.isSystem())
			{
				event.reply("你不能這樣做！").setEphemeral(true).queue();
				return;
			}

			GuildPointsHandle.PlayerData playerData = GuildPointsHandle.getPointsData(user.getIdLong());
			long nowHave = playerData.getPoints();

			String betString = event.getOption("bet", "", OptionMapping::getAsString);
			long bet;
			if (BET_NUMBER_REGEX.matcher(betString).matches()) //賭數字
				bet = Long.parseLong(betString);
			else if (BET_PERCENT_REGEX.matcher(betString).matches()) //賭%數
			{
				short percentage = Short.parseShort(betString.substring(0, betString.length() - 1));
				if (percentage > 100) //百分比格式錯誤 不能賭超過100%
				{
					event.reply("你不能賭超過100%！").setEphemeral(true).queue();
					return;
				}
				bet = nowHave * percentage / 100;
			}
			else if ("all".equalsIgnoreCase(betString))
				bet = nowHave;
			else if ("half".equalsIgnoreCase(betString))
				bet = nowHave >> 1;
			else if ("quarter".equalsIgnoreCase(betString))
				bet = nowHave >> 2;
			else //都不是
			{
				event.reply("用法錯誤！/points bet <正整數 或 百分比>").setEphemeral(true).queue();
				return;
			}

			if (bet == 0L) //不能賭0
			{
				event.reply("不能賭0！").setEphemeral(true).queue();
				return;
			}
			if (nowHave < bet) //如果現有的比要賭的還少
			{
				event.reply(String.format("你賭不起 %,d 點！", bet)).setEphemeral(true).queue();
				return;
			}

			boolean isWon = new Random().nextBoolean(); //輸贏
			boolean isShowHand = bet == nowHave; //梭哈

			String isWonString;
			String isShowHandString;
			if (isWon)
			{
				isWonString = "贏了！";
				isShowHandString = isShowHand ? "\nhttps://www.youtube.com/watch?v=pFi6f7k9DJ0" : "";
			}
			else
			{
				isWonString = "輸了…";
				isShowHandString = isShowHand ? "\n小賭怡情，大賭傷身。" : "";
			}

			playerData.addGame(bet, isWon, isShowHand); //紀錄下這局
			event.reply("你賭上 " + String.format("%,d", bet) + " 點後" + isWonString +
					"\n你現在有 " + String.format("%,d", playerData.getPoints()) + " 點。" + isShowHandString).queue();
		}
	}

	private static class MineCommand implements ICommand
	{
		private static final int COOLDOWN = 3 * 60;

		@Override
		public void commandProcess(SlashCommandInteractionEvent event)
		{
			/*
			鑽石 5%
			金礦 10%
			鐵礦 15%
			煤炭 25%
			石頭 30%
			空氣 10%
			神秘彩蛋 0.1%
			阿姆斯特朗炫風砲 0.01%
			暗夜 5% - 0.1% - 0.01% = 4.89%
			*/

			User user = event.getUser();
			if (user.isBot() || user.isSystem())
			{
				event.reply("你不能這樣做！").setEphemeral(true).queue();
				return;
			}

			long userID = user.getIdLong();
			GuildPointsHandle.PlayerData playerData = GuildPointsHandle.getPointsData(userID);
			GuildPointsHandle.MineData mineData = GuildPointsHandle.getMineData(userID);

			long now = System.currentTimeMillis() / 1000; //現在的秒數
			long lastMine = mineData.getCooldown(); //上次挖礦的秒數
			long difference = now - lastMine; //差異
			if (difference < COOLDOWN) //冷卻3分鐘
			{
				event.reply("你剛剛挖過礦了！\n請在 <t:" + (lastMine + COOLDOWN) + ":T> 後再試一次。").setEphemeral(true).queue();
				return;
			}
			mineData.setCooldown(now); //新的冷卻

			Random random = new Random();
			int roll = random.nextInt(100);

			//低機率
			if (roll >= 95) //95 ~ 99
			{
				int smallChance = random.nextInt(500);

				if (smallChance < 489)
				{
					event.reply("你挖到了 **暗夜**！\n你被偷走了 10 點！").queue();
					playerData.subPoints(10);
					mineData.getDarkNight().addValue();
				}
				else if (smallChance < 499)
				{
					event.reply("# 你挖到了 神秘彩蛋！\n## 獲得 1000 點！").queue();
					playerData.addPoints(1000);
					mineData.getEasterEgg().addValue();
				}
				else
				{
					event.reply("你挖到了 **阿姆斯特朗炫風砲**！\n請找暗夜，暗夜會告訴你。").queue();
					mineData.getCannon().addValue();
				}

				return;
			}

			String oreName;
			int give;
			GuildPointsHandle.OreStats oreStats;

			if (roll < 5) //0 ~ 4, 5%
			{
				oreName = "鑽石";
				give = 150 / 5;
				oreStats = mineData.getDiamond();
			}
			else if (roll < 15) //5 ~ 14, 10%
			{
				oreName = "金礦";
				give = 150 / 10;
				oreStats = mineData.getGold();
			}
			else if (roll < 30) //15 ~ 29, 15%
			{
				oreName = "鐵礦";
				give = 150 / 15;
				oreStats = mineData.getIron();
			}
			else if (roll < 55) //30 ~ 54, 25%
			{
				oreName = "煤炭";
				give = 150 / 25;
				oreStats = mineData.getCoal();
			}
			else if (roll < 85) //55 ~ 84, 30%
			{
				oreName = "石頭";
				give = 150 / 30;
				oreStats = mineData.getStone();
			}
			else //85 ~ 94, 10%
			{
				oreName = "空氣";
				give = 0;
				oreStats = mineData.getAir();
			}

			event.reply("你挖到了 " + oreName + "！\n獲得 " + give + " 點。").queue();
			playerData.addPoints(give); //給予點數
			oreStats.addValue();
		}
	}

	private static class DailyCommand implements ICommand
	{
		private static final int REWARD = 100; //簽到獎勵100點
		private static final int SECONDS_A_DAY = 60 * 60 * 24;

		@Override
		public void commandProcess(SlashCommandInteractionEvent event)
		{
			User user = event.getUser();
			if (user.isBot() || user.isSystem())
			{
				event.reply("你不能這樣做！").setEphemeral(true).queue();
				return;
			}

			GuildPointsHandle.PlayerData playerData = GuildPointsHandle.getPointsData(user.getIdLong());

			long now = System.currentTimeMillis() / 1000; //現在的秒數
			long lastClaim = playerData.getDaily(); //上次簽到的秒數
			long difference = now - lastClaim; //差異
			if (difference < SECONDS_A_DAY) //一天86400秒
			{
				event.reply("你今天已經簽到過了！\n請在 <t:" + (lastClaim + SECONDS_A_DAY) + "> 後再試一次。").setEphemeral(true).queue();
				return;
			}

			playerData.addPoints(REWARD);
			event.reply("簽到成功！獲得 " + REWARD + " 點。\n你現在有 " + playerData.getPoints() + " 點。").queue();
			playerData.setDaily(now);
		}
	}

	private static class RankCommand implements ICommand
	{
		@Override
		public void commandProcess(SlashCommandInteractionEvent event)
		{
			User user = event.getUser();
			if (user.isBot() || user.isSystem())
			{
				event.reply("你不能這樣做！").setEphemeral(true).queue();
				return;
			}

			//page從1開始 預設1
			event.reply(GuildPointsHandle.getRankString(user.getIdLong(), event.getOption("page", 1, OptionMapping::getAsInt))).queue();
		}
	}
}