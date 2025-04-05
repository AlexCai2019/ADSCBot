package org.adsc.adsc_bot.commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.adsc.adsc_bot.utilties.GuildPointsHandle;

import java.util.Random;
import java.util.regex.Pattern;

public class PointsCommand extends HasSubcommands
{
	public static final String POINTS = "points";

	public static final String VIEW = "view";
	public static final String BET = "bet";
	public static final String MINE = "mine";
	public static final String DAILY = "daily";

	public PointsCommand()
	{
		super(4);

		subcommands.put(VIEW, new ViewCommand());
		subcommands.put(BET, new BetCommand());
		subcommands.put(MINE, new MineCommand());
		subcommands.put(DAILY, new DailyCommand());
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
				event.reply("你不能這樣做！").queue();
				return;
			}

			GuildPointsHandle.PlayerData playerData = GuildPointsHandle.getPointsData(user.getIdLong());

			if (!event.getOption("detail", false, OptionMapping::getAsBoolean))
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

			event.reply("你賭上 " + String.format("%,d", bet) + " 點後" + isWonString +
					"\n你現在有 " + String.format("%,d", nowHave + bet) + " 點。" + isShowHandString).queue();
			playerData.addGame(bet, isWon, isShowHand); //紀錄下這局
		}
	}

	private static class MineCommand implements ICommand
	{
		@Override
		public void commandProcess(SlashCommandInteractionEvent event)
		{
			event.reply("挖礦功能正在趕工中！").setEphemeral(true).queue();
		}
	}

	private static class DailyCommand implements ICommand
	{
		private static final int REWARD = 100; //簽到獎勵100點

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
			if (difference < 86400) //一天86400秒
			{
				event.reply("你今天已經簽到過了！\n請在 <t:" + (lastClaim + 86400) + "> 後再試一次。").setEphemeral(true).queue();
				return;
			}

			event.reply("簽到成功！獲得 " + REWARD + " 點。\n你現在有 " + playerData.getPoints() + " 點。").queue();
			playerData.addPoints(REWARD);
			playerData.setDaily(now);
		}
	}
}