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
import org.adsc.adsc_bot.commands.PointsCommand;
import org.adsc.adsc_bot.events.*;
import org.adsc.adsc_bot.utilties.FileHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				Commands.slash("shutdown", "關閉機器人")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
			.queue(); //添加指令

		jda.awaitReady();
	}
}