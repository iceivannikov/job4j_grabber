package ru.job4j.grabber.storage;

import ru.job4j.grabber.model.Post;

import java.sql.*;
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
                posts.add(createPost(resultSet));
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
                post = createPost(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    private Post createPost(ResultSet resultSet) {
        Post post;
        try {
            post = new Post(
                    resultSet.getInt("id"),
                    resultSet.getString("title"),
                    resultSet.getString("link"),
                    resultSet.getString("description"),
                    resultSet.getTimestamp("created").toLocalDateTime());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }
}
