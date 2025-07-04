package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import org.adsc.adsc_bot.utilties.Constants;

public class ModalEvent extends ListenerAdapter
{
	public static final String CREATE_QUESTION_MODAL = "create_question_modal";
	public static final String MINECRAFT_ID = "minecraft_id";
	public static final String FACING_PROBLEM = "facing_problem";

	@Override
	public void onModalInteraction(@NonNull ModalInteractionEvent event)
	{
		User user = event.getUser();
		if (user.isBot() || user.isSystem())
			return;

		if (CREATE_QUESTION_MODAL.equals(event.getModalId()))
		{
			ModalMapping minecraftID = event.getValue(MINECRAFT_ID);
			ModalMapping facingProblem = event.getValue(FACING_PROBLEM);
			if (minecraftID == null || facingProblem == null)
			{
				event.reply("發生錯誤，請再試一次…").setEphemeral(true).queue();
				return;
			}

			event.deferReply().setEphemeral(true).queue();
			event.getChannel()
				.asThreadContainer()
				.createThreadChannel(user.getEffectiveName())
				.queue(channel ->
				{
					channel.sendMessage(user.getAsMention() + " 開啟了問題單，請靜待管理員 <@" + Constants.DARK_NIGHT_USER_ID + "> 和 <@" + Constants.AC_USER_ID + "> 協助處理。\n" +
							"Minecraft ID：`" + minecraftID.getAsString() + "`\n" +
							"遇到的問題：" + facingProblem.getAsString()).queue();

					channel.retrieveParentMessage().queue(message ->
					{
						if (message.getType() == MessageType.THREAD_CREATED)
							message.delete().queue();
					});

					event.getHook().sendMessage("已開啟討論串 " + channel.getAsMention()).queue();
				});
		}
	}
}