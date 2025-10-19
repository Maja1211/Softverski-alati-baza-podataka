package rs.etf.sab.student;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import rs.ac.bg.etf.sab.operations.RatingsOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class sm220572_RatingsOperations implements RatingsOperations {

    private Connection getConnection() throws SQLException {
        return DB.getInstance().getConnection();
    }

    @Override
    public boolean addRating(Integer userId, Integer movieId, Integer rating) {
        if (userId == null || movieId == null || rating == null) return false;
        if (rating < 1 || rating > 10) return false;

        final String check = "SELECT 1 FROM Ocena WHERE KorisnikID = ? AND FilmID = ?";
        final String insert = "INSERT INTO Ocena (KorisnikID, FilmID, Vrednost) VALUES (?, ?, ?)";

        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                ps.setInt(3, rating);
                ps.executeUpdate();
            }

            try (CallableStatement cs = conn.prepareCall("{CALL dbo.SP_REWARD_USER(?, ?)}")) {
                cs.setInt(1, userId);
                cs.setInt(2, movieId);
                cs.execute();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;

        } catch (SQLServerException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            if (e.getMessage() != null && e.getMessage().contains("Ne možete dati ekstremnu ocenu u ovom žanru!"))
                return false;
            return false;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public boolean updateRating(Integer userId, Integer movieId, Integer newRating) {
        if (userId == null || movieId == null || newRating == null) return false;
        if (newRating < 1 || newRating > 10) return false;

        final String update = "UPDATE Ocena SET Vrednost = ? WHERE KorisnikID = ? AND FilmID = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setInt(1, newRating);
                ps.setInt(2, userId);
                ps.setInt(3, movieId);
                int updated = ps.executeUpdate();

                conn.commit();
                conn.setAutoCommit(true);
                return updated > 0;
            }

        } catch (SQLServerException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return false;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public boolean removeRating(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return false;

        final String delete = "DELETE FROM Ocena WHERE KorisnikID = ? AND FilmID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(delete)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public Integer getRating(Integer userId, Integer movieId) {
        if (userId == null || movieId == null) return null;

        final String q = "SELECT Vrednost FROM Ocena WHERE KorisnikID = ? AND FilmID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {

            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("Vrednost");
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm220572_RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer userId) {
        List<Integer> list = new ArrayList<>();
        if (userId == null) return list;

        final String q = "SELECT FilmID FROM Ocena WHERE KorisnikID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("FilmID"));
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm220572_RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer movieId) {
        List<Integer> list = new ArrayList<>();
        if (movieId == null) return list;

        final String q = "SELECT KorisnikID FROM Ocena WHERE FilmID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {

            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("KorisnikID"));
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm220572_RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
}
