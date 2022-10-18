package ru.job4j.grabber;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("url"),
                cfg.getProperty("username"),
                cfg.getProperty("password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(String.format("%s%s",
                        "INSERT INTO post(name, link, text, created) VALUES (?, ?, ?, ?)",
                        " ON CONFLICT (link) DO NOTHING;"),
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement preparedStatement = cnn.prepareStatement("SELECT * FROM post;")) {
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    posts.add(makePost(result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * from post where id = ?;")) {
            statement.setInt(1, id);
            try (ResultSet itemSet = statement.executeQuery()) {
                if (itemSet.next()) {
                    post = makePost(itemSet);
                }
            }
        }  catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post makePost(ResultSet result) throws SQLException {
        return new Post(result.getInt(1), result.getString(2),
                result.getString(3), result.getString(4),
                result.getTimestamp(5).toLocalDateTime());
    }
}