package org.adsc.adsc_bot.events;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class ButtonEvent extends ListenerAdapter
{
	public static final String CREATE_QUESTION_BUTTON = "create_question_button";

	@Override
	public void onButtonInteraction(@NonNull ButtonInteractionEvent event)
	{
		User user = event.getUser();
		if (user.isBot() || user.isSystem())
			return;

		if (CREATE_QUESTION_BUTTON.equals(event.getComponentId()))
		{
			TextInput minecraftID = TextInput.create(ModalEvent.MINECRAFT_ID, "Minecraft ID", TextInputStyle.SHORT).build();
			TextInput facingProblem = TextInput.create(ModalEvent.FACING_PROBLEM, "遇到的問題", TextInputStyle.PARAGRAPH).build();

			Modal modal = Modal.create(ModalEvent.CREATE_QUESTION_MODAL, "你需要幫助嗎？")
					.addComponents(ActionRow.of(minecraftID), ActionRow.of(facingProblem))
					.build();

			event.replyModal(modal).queue();
		}
	}
}