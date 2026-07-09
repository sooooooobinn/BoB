package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MealService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String API_KEY = System.getenv("MEAL_API_KEY");

    private static final Color MAIN_PINK_COLOR = new Color(251, 202, 224);

    public static EmbedBuilder getTodayMeal(String command) {
        LocalTime nowTime = LocalTime.now();
        LocalDate targetDate = LocalDate.now();
        String targetMealType = "";
        String mealNameKor = "";

        if (command.equals("!밥.아침")) {
            targetMealType = "BREAKFAST";
            mealNameKor = "오늘 아침";
        } else if (command.equals("!밥.점심")) {
            targetMealType = "LUNCH";
            mealNameKor = "오늘 점심";
        } else if (command.equals("!밥.저녁")) {
            targetMealType = "DINNER";
            mealNameKor = "오늘 저녁";
        } else if (command.equals("!밥.내일아침")) {
            targetDate = targetDate.plusDays(1);
            targetMealType = "BREAKFAST";
            mealNameKor = "내일 아침";
        } else if (command.equals("!밥.내일점심")) {
            targetDate = targetDate.plusDays(1);
            targetMealType = "LUNCH";
            mealNameKor = "내일 점심";
        } else if (command.equals("!밥.내일저녁")) {
            targetDate = targetDate.plusDays(1);
            targetMealType = "DINNER";
            mealNameKor = "내일 저녁";
        } else {
            if (nowTime.isBefore(LocalTime.of(7, 40))) {
                targetMealType = "BREAKFAST";
                mealNameKor = "오늘 아침";
            } else if (nowTime.isBefore(LocalTime.of(13, 30))) {
                targetMealType = "LUNCH";
                mealNameKor = "오늘 점심";
            } else if (nowTime.isBefore(LocalTime.of(19, 30))) {
                targetMealType = "DINNER";
                mealNameKor = "오늘 저녁";
            } else {
                targetDate = targetDate.plusDays(1);
                targetMealType = "BREAKFAST";
                mealNameKor = "내일 아침";
            }
        }

        String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        EmbedBuilder embed = new EmbedBuilder();

        HttpUrl url = HttpUrl.parse("https://openapi.datagsm.kr/v1/neis/meals").newBuilder()
                .addQueryParameter("date", dateStr)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-API-KEY", API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                return embed.setTitle("❌ 오류 발생")
                        .setDescription("DG 급식 호출에 실패했습니다. status=" + response.code())
                        .setColor(MAIN_PINK_COLOR);
            }

            JsonNode jsonNode = mapper.readTree(responseBody);

            if (jsonNode.has("data") && jsonNode.get("data").has("meals")) {
                JsonNode mealsArray = jsonNode.get("data").get("meals");

                if (mealsArray.isEmpty()) {
                    return embed.setDescription("📅 [" + dateStr + "] 급식 정보가 없거나 휴일입니다. 🏖️")
                            .setColor(MAIN_PINK_COLOR);
                }

                boolean foundMeal = false;

                for (JsonNode meal : mealsArray) {
                    String type = meal.get("mealType").asText();

                    if (type.equalsIgnoreCase(targetMealType)) {
                        foundMeal = true;

                        String icon = "";
                        if (type.equalsIgnoreCase("BREAKFAST")) icon = "☀️";
                        else if (type.equalsIgnoreCase("LUNCH")) icon = "🍴";
                        else if (type.equalsIgnoreCase("DINNER")) icon = "🌙";

                        embed.setTitle(icon + " " + mealNameKor.replace("오늘 ", "").replace("내일 ", ""));
                        embed.setColor(MAIN_PINK_COLOR);

                        StringBuilder menuBuilder = new StringBuilder();
                        if (meal.has("mealMenu")) {
                            for (JsonNode menu : meal.get("mealMenu")) {
                                menuBuilder.append("• ").append(menu.asText()).append("\n");
                            }
                        }
                        embed.setDescription(menuBuilder.toString().trim());

                        if (meal.has("mealCalories")) {
                            embed.setFooter(meal.get("mealCalories").asText());
                        }
                        break;
                    }
                }

                if (!foundMeal) {
                    return embed.setDescription("📅 [" + dateStr + "] " + mealNameKor + " 메뉴가 아직 등록되지 않았습니다. 🏖️")
                            .setColor(MAIN_PINK_COLOR);
                }

                return embed;

            } else {
                return embed.setDescription("급식 데이터를 처리하는 중 오류가 발생했습니다.").setColor(MAIN_PINK_COLOR);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return embed.setDescription("DG 급식 서버에 연결할 수 없습니다. (네트워크 오류)").setColor(MAIN_PINK_COLOR);
        }
    }

    public static EmbedBuilder getHelpMessage() {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("🤖 Void-Bob봇 명령어 안내");
        embed.setColor(MAIN_PINK_COLOR);

        StringBuilder sb = new StringBuilder();
        sb.append("💡 **기본 자동 조회**\n");
        sb.append("• `!밥` : 현재 시간 기준 가장 가까운 다음 급식을 자동으로 보여줍니다.\n");
        sb.append("  *(07:40 전은 아침, 13:30 전은 점심, 19:30 전은 저녁, 그 이후는 내일 아침)*\n\n");

        sb.append("📅 **오늘 급식 지정 조회**\n");
        sb.append("• `!밥.아침` : 오늘의 아침 메뉴를 보여줍니다. ☀️\n");
        sb.append("• `!밥.점심` : 오늘의 점심 메뉴를 보여줍니다. 🍴\n");
        sb.append("• `!밥.저녁` : 오늘의 저녁 메뉴를 보여줍니다. 🌙\n\n");

        sb.append("⏩ **내일 급식 지정 조회**\n");
        sb.append("• `!밥.내일아침` : 내일의 아침 메뉴를 보여줍니다. ☀️\n");
        sb.append("• `!밥.내일점심` : 내일의 점심 메뉴를 보여줍니다. 🍴\n");
        sb.append("• `!밥.내일저녁` : 내일의 저녁 메뉴를 보여줍니다. 🌙");

        embed.setDescription(sb.toString());
        embed.setFooter("GSM 급식 알리미 v1.0");

        return embed;
    }
}