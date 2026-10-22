package movieticket.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import movieticket.model.Movie;
import movieticket.util.DatabaseConnection;
import movieticket.util.DbUtil;

/** CRUD operations on the movies table using JDBC. */
public class MovieDao implements CrudDao<Movie> {

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Movie map(ResultSet rs) throws SQLException {
        return new Movie(rs.getInt("id"), rs.getString("title"), rs.getString("genre"),
                rs.getString("language"), rs.getInt("duration_min"), rs.getDouble("rating"));
    }

    @Override
    public int insert(Movie movie) throws SQLException {
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            ps = connection().prepareStatement(
                    "INSERT INTO movies (title, genre, language, duration_min, rating) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDurationMinutes());
            ps.setDouble(5, movie.getRating());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } finally {
            DbUtil.close(keys, ps);
        }
    }

    @Override
    public Movie findById(int id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement("SELECT * FROM movies WHERE id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } finally {
            DbUtil.close(rs, ps);
        }
    }

    @Override
    public Vector<Movie> findAll() throws SQLException {
        Vector<Movie> movies = new Vector<Movie>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection().createStatement();
            rs = st.executeQuery("SELECT * FROM movies ORDER BY title");
            while (rs.next()) {
                movies.add(map(rs));
            }
        } finally {
            DbUtil.close(rs, st);
        }
        return movies;
    }

    @Override
    public boolean update(Movie movie) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement(
                    "UPDATE movies SET title=?, genre=?, language=?, duration_min=?, rating=? WHERE id=?");
            ps.setString(1, movie.getTitle());
            ps.setString(2, movie.getGenre());
            ps.setString(3, movie.getLanguage());
            ps.setInt(4, movie.getDurationMinutes());
            ps.setDouble(5, movie.getRating());
            ps.setInt(6, movie.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DbUtil.close(ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement("DELETE FROM movies WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            DbUtil.close(ps);
        }
    }
}
