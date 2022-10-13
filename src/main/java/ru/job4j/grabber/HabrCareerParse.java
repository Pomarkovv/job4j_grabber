package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        HabrCareerDateTimeParser dateParse = new HabrCareerDateTimeParser();
        List<String> saved = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(String.format("%s?page=%d", PAGE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first();
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String date = dateElement.text();
                System.out.printf("%s %s %s%n", vacancyName, link, date);
            });
        }
    }
}