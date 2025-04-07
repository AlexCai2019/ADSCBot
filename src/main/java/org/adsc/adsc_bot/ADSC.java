package org.adsc.adsc_bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
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
									new CommandEvent())
							.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS) //機器人可讀取訊息和查看伺服器成員
							.setMemberCachePolicy(MemberCachePolicy.ALL)
							.build();

		jda.updateCommands()
			.addCommands(
				Commands.slash(PointsCommand.POINTS, "點數")
						.addSubcommands(
								new SubcommandData(PointsCommand.VIEW, "檢視你目前擁有的點數和記錄")
										.addOption(OptionType.BOOLEAN, "detail", "顯示細節", false, false),
								new SubcommandData(PointsCommand.BET, "賭上點數")
										.addOption(OptionType.STRING, "bet", "賭上的點數，可以是數字或%數", true, false),
								new SubcommandData(PointsCommand.MINE, "隨機挖礦"),
								new SubcommandData(PointsCommand.DAILY, "每日簽到，獲得點數"),
								new SubcommandData(PointsCommand.RANK, "排名")
										.addOption(OptionType.INTEGER, "page", "頁數", false, false)),
				Commands.slash("shutdown", "關閉機器人")
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)))
			.queue(); //添加指令

		jda.awaitReady();
	}
}