package com.example;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw();

        if (message.equals("!명령어")) {
            net.dv8tion.jda.api.EmbedBuilder helpEmbed = MealService.getHelpMessage();
            event.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();
        }
        else if (message.startsWith("!밥")) {
            net.dv8tion.jda.api.EmbedBuilder embedResult = MealService.getTodayMeal(message);
            event.getChannel().sendMessageEmbeds(embedResult.build()).queue();
        }
    }
}