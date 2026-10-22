package movieticket.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import movieticket.model.Show;
import movieticket.util.DatabaseConnection;
import movieticket.util.DbUtil;

/** CRUD operations on the shows table (joined with movies to get the title). */
public class ShowDao implements CrudDao<Show> {
    private static final String SELECT = "SELECT s.id, s.movie_id, m.title, s.screen, s.show_date, s.show_time, s.price "
            + "FROM shows s JOIN movies m ON m.id = s.movie_id ";

    private Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Show map(ResultSet rs) throws SQLException {
        return new Show(rs.getInt("id"), rs.getInt("movie_id"), rs.getString("title"), rs.getString("screen"),
                rs.getString("show_date"), rs.getString("show_time"), rs.getDouble("price"));
    }

    @Override
    public int insert(Show show) throws SQLException {
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            ps = connection().prepareStatement(
                    "INSERT INTO shows (movie_id, screen, show_date, show_time, price) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, show.getMovieId());
            ps.setString(2, show.getScreen());
            ps.setString(3, show.getShowDate());
            ps.setString(4, show.getShowTime());
            ps.setDouble(5, show.getBasePrice());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } finally {
            DbUtil.close(keys, ps);
        }
    }

    @Override
    public Show findById(int id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection().prepareStatement(SELECT + "WHERE s.id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? map(rs) : null;
        } finally {
            DbUtil.close(rs, ps);
        }
    }

    @Override
    public Vector<Show> findAll() throws SQLException {
        Vector<Show> shows = new Vector<Show>();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection().createStatement();
            rs = st.executeQuery(SELECT + "ORDER BY s.show_date, s.show_time");
            while (rs.next()) {
                shows.add(map(rs));
            }
        } finally {
            DbUtil.close(rs, st);
        }
        return shows;
    }

    @Override
    public boolean update(Show show) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement(
                    "UPDATE shows SET movie_id=?, screen=?, show_date=?, show_time=?, price=? WHERE id=?");
            ps.setInt(1, show.getMovieId());
            ps.setString(2, show.getScreen());
            ps.setString(3, show.getShowDate());
            ps.setString(4, show.getShowTime());
            ps.setDouble(5, show.getBasePrice());
            ps.setInt(6, show.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DbUtil.close(ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection().prepareStatement("DELETE FROM shows WHERE id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            DbUtil.close(ps);
        }
    }
}
