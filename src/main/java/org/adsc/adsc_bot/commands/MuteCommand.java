package org.adsc.adsc_bot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class MuteCommand implements ICommand
{
	public static final String MUTE = "mute";
	private static final Logger logger = LoggerFactory.getLogger(MuteCommand.class);

	@Override
	public void commandProcess(SlashCommandInteractionEvent event)
	{
		Member member = event.getMember(); //使用指令的成員
		if (member == null)
		{
			event.reply("請在伺服器中使用！").setEphemeral(true).queue();
			return;
		}

		Member target = event.getOption("target", OptionMapping::getAsMember); //要被禁言的目標
		if (target == null) //找不到要被禁言的成員
		{
			event.reply("找不到目標！").setEphemeral(true).queue();
			return;
		}

		if (target.hasPermission(Permission.MESSAGE_MANAGE)) //無法禁言身分組更高的人
		{
			event.reply("無法禁言：權限不足！").setEphemeral(true).queue();
			return;
		}
		if (target.isTimedOut()) //已經被禁言了
		{
			event.reply("該名使用者已經被禁言了！").setEphemeral(true).queue();
			return;
		}

		double duration = event.getOption("duration", 0.0, OptionMapping::getAsDouble);
		String unit = event.getOption("unit", "", OptionMapping::getAsString);

		//不用java.util.concurrent.TimeUnit 因為它不接受浮點數
		long durationMillis = Math.round(duration * switch (unit) //將單位轉成毫秒 1000毫秒等於1秒
		{
			case "second" -> 1000;
			case "minute" -> 1000 * 60;
			case "quarter" -> 1000 * 60 * 15;
			case "hour" -> 1000 * 60 * 60;
			case "double_hour" -> 1000 * 60 * 60 * 2;
			case "day" -> 1000 * 60 * 60 * 24;
			case "week" -> 1000 * 60 * 60 * 24 * 7;
			default -> 1; //millisecond
		}); //Math.round會處理溢位

		if (durationMillis <= 0) //不能負時間
		{
			event.reply("時間不能是負的！").setEphemeral(true).queue();
			return;
		}

		if (durationMillis > 1000L * 60 * 60 * 24 * Member.MAX_TIME_OUT_LENGTH) //不能禁言超過28天
		{
			event.reply("禁言時間不能超過" + Member.MAX_TIME_OUT_LENGTH + "天！").setEphemeral(true).queue();
			return;
		}

		long millis = durationMillis;
		StringBuilder replyBuilder = new StringBuilder(target.getAsMention()).append(" 被禁言了 ");
		if (millis >= 1000L * 60 * 60 * 24) //超過一天
		{
			replyBuilder.append(millis / (1000L * 60 * 60 * 24)).append(" 天 ");
			millis %= (1000L * 60 * 60 * 24);
		}
		if (millis >= 1000L * 60 * 60) //超過一小時
		{
			replyBuilder.append(millis / (1000L * 60 * 60)).append(" 小時 ");
			millis %= (1000L * 60 * 60);
		}
		if (millis >= 1000L * 60) //超過一分鐘
		{
			replyBuilder.append(millis / (1000L * 60)).append(" 分鐘 ");
			millis %= (1000L * 60);
		}
		if (millis >= 1000L) //超過一秒
		{
			replyBuilder.append(millis / 1000L).append(" 秒 ");
			millis %= 1000L;
		}
		if (millis >= 1) //超過一豪秒
			replyBuilder.append(millis).append(" 毫秒 ");

		String reason = event.getOption("reason", OptionMapping::getAsString);
		if (reason != null) //有理由
			replyBuilder.append("\n理由：").append(reason); //加上理由

		event.reply(replyBuilder.toString()).queue();

		target.timeoutFor(Duration.ofMillis(durationMillis)).reason(reason).queue(); //執行禁言
		logger.info("{}({}) mute {}({}) {}", member.getUser().getName(), member.getId(), target.getUser().getName(), target.getId(), replyBuilder);
	}
}