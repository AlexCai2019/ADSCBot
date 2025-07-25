package org.adsc.adsc_bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.adsc.adsc_bot.commands.ClearMessageCommand;
import org.adsc.adsc_bot.commands.MuteCommand;
import org.adsc.adsc_bot.commands.PointsCommand;
import org.adsc.adsc_bot.events.*;
import org.adsc.adsc_bot.utilties.FileHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.dv8tion.jda.api.interactions.DiscordLocale.CHINESE_CHINA;
import static net.dv8tion.jda.api.interactions.DiscordLocale.CHINESE_TAIWAN;

public class ADSC
{
	private static final Logger logger = LoggerFactory.getLogger(ADSC.class);

	private static JDA jda;
	public static JDA getJDA()
	{
		return jda;
	}

	public static void main(String[] args) throws InterruptedException
	{
		String token = FileHandle.readConfig("token"); //讀取token
		if (token.isEmpty())
		{
			logger.error("沒有token！");
			return;
		}

		//Java Discord API
		logger.info("機器人上線！");
		jda = JDABuilder.createDefault(token) //啟動機器人
					.addEventListeners(
							new SessionEvent(),
							new ReactionEvent(),
							new ChangeNameEvent(),
							new MessageEvent(),
							new CommandEvent(),
							new ButtonEvent(),
							new ModalEvent())
					.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS) //機器人可讀取訊息和查看伺服器成員
					.setMemberCachePolicy(MemberCachePolicy.ALL)
					.build();

		jda.updateCommands()
			.addCommands(
				Commands.slash(PointsCommand.POINTS, "點數")
						.addSubcommands(
								new SubcommandData(PointsCommand.VIEW, "檢視你目前擁有的點數和記錄")
										.addOptions(
												new OptionData(OptionType.BOOLEAN, "detail", "Show detail", false, false)
														.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "細節")
														.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "顯示細節"),
												new OptionData(OptionType.STRING, "category", "Points or Mine", false, false)
														.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "類別")
														.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "點數或挖礦")
														.addChoices(
																new Command.Choice("Points", "points")
																		.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "點數"),
																new Command.Choice("Mine", "mine")
																		.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "挖礦"))),
								new SubcommandData(PointsCommand.BET, "Bet points")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "賭上點數")
										.addOptions(new OptionData(OptionType.STRING, "bet", "points to bet, could be integer or percent", true, false)
												.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "賭上的點數")
												.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "賭上的點數，可以是整數或百分比")),
								new SubcommandData(PointsCommand.MINE, "Mine randomly")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "隨機挖礦"),
								new SubcommandData(PointsCommand.DAILY, "Daily check to earn points")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "每日簽到，獲得點數"),
								new SubcommandData(PointsCommand.RANK, "View rank")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "檢視排名")
										.addOptions(new OptionData(OptionType.INTEGER, "page", "Page to show", false, false)
												.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "頁數")
												.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "顯示的頁數"))),
				Commands.slash(ClearMessageCommand.CLEAR_MESSAGE, "清除訊息")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
						.setContexts(InteractionContextType.GUILD)
						.addOptions(
								new OptionData(OptionType.INTEGER, "counts", "Messages counts that are going to be deleted", true, false)
										.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "數量")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "要刪除的訊息數量"),
								new OptionData(OptionType.USER, "target", "Target user", false, false)
										.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "目標")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "目標玩家"),
								new OptionData(OptionType.STRING, "target_id", "Target user ID", false, false)
										.setNameLocalization(DiscordLocale.CHINESE_TAIWAN, "目標id")
										.setDescriptionLocalization(DiscordLocale.CHINESE_TAIWAN, "目標玩家ID")),
				Commands.slash(MuteCommand.MUTE, "禁言")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
						.setContexts(InteractionContextType.GUILD)
						.addOptions(
								new OptionData(OptionType.USER, "target", "The member that you want to mute", true, false)
										.setNameLocalization(CHINESE_TAIWAN, "目標")
										.setNameLocalization(CHINESE_CHINA, "目标")
										.setDescriptionLocalization(CHINESE_TAIWAN, "你想禁言的成員")
										.setDescriptionLocalization(CHINESE_CHINA, "你想禁言的成员"),
								new OptionData(OptionType.NUMBER, "duration", "Duration that the member is going to be muted", true, false)
										.setNameLocalization(CHINESE_TAIWAN, "時間")
										.setNameLocalization(CHINESE_CHINA, "时长")
										.setDescriptionLocalization(CHINESE_TAIWAN, "成員將被禁言的時間")
										.setDescriptionLocalization(CHINESE_CHINA, "成员将被禁言的时长"),
								new OptionData(OptionType.STRING, "unit", "The time unit of the duration", true, false)
										.setNameLocalization(CHINESE_TAIWAN, "單位")
										.setNameLocalization(CHINESE_CHINA, "单位")
										.setDescriptionLocalization(CHINESE_TAIWAN, "時間的單位")
										.setDescriptionLocalization(CHINESE_CHINA, "时长的单位")
										.addChoices(
												new Command.Choice("Millisecond", "millisecond")
														.setNameLocalization(CHINESE_TAIWAN, "毫秒")
														.setNameLocalization(CHINESE_CHINA, "毫秒"),
												new Command.Choice("Second", "second")
														.setNameLocalization(CHINESE_TAIWAN, "秒")
														.setNameLocalization(CHINESE_CHINA, "秒"),
												new Command.Choice("Minute", "minute")
														.setNameLocalization(CHINESE_TAIWAN, "分鐘")
														.setNameLocalization(CHINESE_CHINA, "分钟"),
												new Command.Choice("Quarter", "quarter")
														.setNameLocalization(CHINESE_TAIWAN, "刻")
														.setNameLocalization(CHINESE_CHINA, "刻"),
												new Command.Choice("Hour", "hour")
														.setNameLocalization(CHINESE_TAIWAN, "小時")
														.setNameLocalization(CHINESE_CHINA, "小时"),
												new Command.Choice("Double Hour", "double_hour")
														.setNameLocalization(CHINESE_TAIWAN, "時辰")
														.setNameLocalization(CHINESE_CHINA, "时辰"),
												new Command.Choice("Day", "day")
														.setNameLocalization(CHINESE_TAIWAN, "天")
														.setNameLocalization(CHINESE_CHINA, "天"),
												new Command.Choice("Week", "week")
														.setNameLocalization(CHINESE_TAIWAN, "星期")
														.setNameLocalization(CHINESE_CHINA, "星期")),
								new OptionData(OptionType.STRING, "reason", "Reason of mute", false, false)
										.setNameLocalization(CHINESE_TAIWAN, "理由")
										.setNameLocalization(CHINESE_CHINA, "理由")
										.setDescriptionLocalization(CHINESE_TAIWAN, "禁言的理由")
										.setDescriptionLocalization(CHINESE_CHINA, "禁言的理由")),
				Commands.slash("shutdown", "關閉機器人")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
			.queue(); //添加指令

		jda.awaitReady();
	}
}