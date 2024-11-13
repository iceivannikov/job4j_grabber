package ru.job4j;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int MAX_PAGES = 5;

    public static void main(String[] args) {
        int pageNumber = 1;
        try {
            for (int i = pageNumber; i < MAX_PAGES; i++) {
                String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
                Connection connection = Jsoup.connect(fullLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");

                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title a").first();
                    String vacancyName = Objects.requireNonNull(titleElement).text();
                    String link = String.format("%s%s", SOURCE_LINK, titleElement.attr("href"));
                    String description = retrieveDescription(link);
                    Element dateElement = row.select(".vacancy-card__date time").first();
                    String date = Objects.requireNonNull(dateElement).attr("datetime");
                    System.out.printf("%s %s %s%n %s%n", vacancyName, link, date, description);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Error connecting to the site or parsing data", e);
        }
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
}
