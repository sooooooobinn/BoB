package com.example;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) {
        String token = System.getenv("DISCORD_TOKEN");

        if (token == null || token.isEmpty()) {
            System.err.println("에러: 환경변수 'DISCORD_TOKEN'이 설정되지 않았습니다!");
            return;
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new BotListener())
                .build();

        System.out.println("급식 봇이 성공적으로 켜졌습니다!");
    }
}