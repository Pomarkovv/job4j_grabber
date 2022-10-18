package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com/vacancies/java_developer?page=";

    private static final int PAGES_COUNT = 5;

    public final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".style-ugc");
        return rows.text();
    }

    private Post getPost(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select(".vacancy-card__date").first();
        String vacancyName = titleElement.text();
        Element date = dateElement.child(0);
        LocalDateTime vacancyDate = dateTimeParser.parse(date.attr("datetime"));
        String vacLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description = null;
        try {
            description = retrieveDescription(vacLink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Post(vacancyName, vacLink, description, vacancyDate);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> vacancies = new ArrayList<>();
        for (int i = 1; i <= PAGES_COUNT; i++) {
            String pageLink = String.format("%s%d", link, i);
            try {
                Connection connection = Jsoup.connect(pageLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
               for (Element row : rows) {
                   vacancies.add(getPost(row));
               }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return vacancies;
    }
}