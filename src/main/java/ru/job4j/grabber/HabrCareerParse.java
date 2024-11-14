package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int START_PAGE = 1;
    public static final int MAX_PAGES = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse(dateTimeParser);

        String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, START_PAGE, SUFFIX);
        List<Post> posts = parser.list(fullLink);
        posts.forEach(System.out::println);
    }

    private static String retrieveDescription(String link) {
        StringBuilder builder = new StringBuilder();
        try {
            Document document = Jsoup.connect(link).get();
            Element descriptionElement = document.select(".vacancy-description__text").first();
            if (descriptionElement != null) {
                Elements elements = descriptionElement.select("h3, p");
                for (Element element : elements) {
                    if (!element.text().isEmpty()) {
                        builder.append(element.text()).append("\n");
                    }
                }
            } else {
                System.out.println("Job description not found");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error connecting to page or parsing data", e);
        }
        return builder.toString();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        try {
            for (int i = START_PAGE; i < MAX_PAGES; i++) {
                Connection connection = Jsoup.connect(link);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");

                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title a").first();
                    String title = Objects.requireNonNull(titleElement).text();
                    String vacancyLink = String.format("%s%s", SOURCE_LINK, titleElement.attr("href"));
                    String description = retrieveDescription(vacancyLink);
                    Element dateElement = row.select(".vacancy-card__date time").first();
                    String date = Objects.requireNonNull(dateElement).attr("datetime");
                    posts.add(new Post(title, vacancyLink, description, dateTimeParser.parse(date)));
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Error connecting to the site or parsing data", e);
        }
        return posts;
    }
}
