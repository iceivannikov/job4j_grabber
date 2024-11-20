package ru.job4j.grabber.storage;

import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {
    private final Connection connection;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        String sql = "INSERT into post (title, link, description, created) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON CONFLICT (link) DO UPDATE SET "
                   + "title = EXCLUDED.title, "
                   + "description = EXCLUDED.description, "
                   + "created = EXCLUDED.created";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM post")) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                posts.add(new Post(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("link"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                post = new Post(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("link"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    public static void main(String[] args) {
        Properties config = new Properties();
        try (InputStream is = AlertRabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            config.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file", e);
        }
        PsqlStore psqlStore = new PsqlStore(config);
        Post post = new Post("Java Developer", "https://example.com/vacancy/2",
                "Description for Java Developer", LocalDateTime.now());
        psqlStore.save(post);
        psqlStore.save(new Post("Kotlin Developer", "https://example.com/vacancy/1",
                "Description for Kotlin Developer", LocalDateTime.now()));
        psqlStore.save(new Post("Python Developer", "https://example.com/vacancy/3",
                "Description for Python Developer", LocalDateTime.now()));
        psqlStore.save(new Post("Duplicate Java Developer", "https://example.com/vacancy/1",
                "Duplicate Description", LocalDateTime.now()));
        psqlStore.save(new Post("Another Java Developer", "https://example.com/vacancy/4",
                "Another Java Developer Description", LocalDateTime.now()));

        List<Post> posts = psqlStore.getAll();
        System.out.println("method List<Post> getAll()");
        posts.forEach(System.out::println);

        Post post1 = psqlStore.findById(3);
        System.out.println("method Post findById(int id)");
        System.out.println(post1);
    }
}
