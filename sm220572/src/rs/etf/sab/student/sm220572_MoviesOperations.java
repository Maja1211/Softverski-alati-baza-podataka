package rs.etf.sab.student;
import rs.ac.bg.etf.sab.operations.MoviesOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class sm220572_MoviesOperations implements MoviesOperations {
    private Connection getConnection() throws SQLException {
        return DB.getInstance().getConnection();
    }
    private boolean existsById(String table, int id) {
        String query = "SELECT 1 FROM " + table + " WHERE ID = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(query)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    private boolean relationExists(int filmId, int genreId) {
        String q = "SELECT 1 FROM FilmZanr WHERE FilmID = ? AND ZanrID = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, filmId);
                ps.setInt(2, genreId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public Integer addMovie(String naslov, Integer idZanra, String reziser) {
        if (naslov == null || naslov.trim().isEmpty() ||
                reziser == null || reziser.trim().isEmpty()) return null;
        naslov = naslov.trim();
        reziser = reziser.trim();

        if (idZanra != null && !existsById("Zanr", idZanra)) return null;
        String insertFilm = "INSERT INTO Film (Naslov, Reziser) VALUES (?, ?)";
        String insertFilmZanr = "INSERT INTO FilmZanr (FilmID, ZanrID) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            Integer idFilma = null;
            try (PreparedStatement ps = conn.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, naslov);
                ps.setString(2, reziser);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idFilma = rs.getInt(1);
                }
            }
            if (idFilma != null && idZanra != null) {
                try (PreparedStatement psLink = conn.prepareStatement(insertFilmZanr)) {
                    psLink.setInt(1, idFilma);
                    psLink.setInt(2, idZanra);
                    psLink.executeUpdate();
                }
            }
            return idFilma;
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer updateMovieTitle(Integer idFilma, String noviNaslov) {
        if (idFilma == null || noviNaslov == null || noviNaslov.trim().isEmpty()) return null;
        if (!existsById("Film", idFilma)) return null;
        String query = "UPDATE Film SET Naslov = ? WHERE ID = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(query)) {
                ps.setString(1, noviNaslov.trim());
                ps.setInt(2, idFilma);
                if (ps.executeUpdate() > 0) return idFilma;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer updateMovieDirector(Integer idFilma, String noviReziser) {
        if (idFilma == null || noviReziser == null || noviReziser.trim().isEmpty()) return null;
        if (!existsById("Film", idFilma)) return null;
        String query = "UPDATE Film SET Reziser = ? WHERE ID = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(query)) {
                ps.setString(1, noviReziser.trim());
                ps.setInt(2, idFilma);
                if (ps.executeUpdate() > 0) return idFilma;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer addGenreToMovie(Integer idFilma, Integer idZanra) {
        if (idFilma == null || idZanra == null) return null;
        if (!existsById("Film", idFilma) || !existsById("Zanr", idZanra)) return null;
        if (relationExists(idFilma, idZanra)) return null;
        String insert = "INSERT INTO FilmZanr (FilmID, ZanrID) VALUES (?, ?)";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(insert)) {
                ps.setInt(1, idFilma);
                ps.setInt(2, idZanra);
                ps.executeUpdate();
                return idFilma;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer removeGenreFromMovie(Integer idFilma, Integer idZanra) {
        if (idFilma == null || idZanra == null) return null;
        if (!relationExists(idFilma, idZanra)) return null;
        String del = "DELETE FROM FilmZanr WHERE FilmID = ? AND ZanrID = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(del)) {
                ps.setInt(1, idFilma);
                ps.setInt(2, idZanra);
                ps.executeUpdate();
                return idFilma;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer removeMovie(Integer idFilma) {
        if (idFilma == null) return null;
        if (!existsById("Film", idFilma)) return null;
        String[] deletions = {
                "DELETE FROM Ocena WHERE FilmID = ?",
                "DELETE FROM FilmTag WHERE FilmID = ?",
                "DELETE FROM ListaFilm WHERE FilmID = ?",
                "DELETE FROM FilmZanr WHERE FilmID = ?",
                "DELETE FROM Film WHERE ID = ?"
        };
        Connection c = null;
        try {
            c = getConnection();
            for (String q : deletions) {
                try (PreparedStatement ps = c.prepareStatement(q)) {
                    ps.setInt(1, idFilma);
                    ps.executeUpdate();
                }
            }
            return idFilma;
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public List<Integer> getMovieIds(String naslov, String reziser) {
        List<Integer> ids = new ArrayList<>();
        String q = "SELECT ID FROM Film WHERE Naslov = ? AND Reziser = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setString(1, naslov);
                ps.setString(2, reziser);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(rs.getInt("ID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    @Override
    public List<Integer> getAllMovieIds() {
        List<Integer> ids = new ArrayList<>();
        String q = "SELECT ID FROM Film ORDER BY ID ASC";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("ID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    @Override
    public List<Integer> getMovieIdsByGenre(Integer idZanra) {
        List<Integer> ids = new ArrayList<>();
        String q = "SELECT FilmID FROM FilmZanr WHERE ZanrID = ? ORDER BY FilmID ASC";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, idZanra);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(rs.getInt("FilmID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    @Override
    public List<Integer> getGenreIdsForMovie(Integer idFilma) {
        List<Integer> ids = new ArrayList<>();
        String q = "SELECT ZanrID FROM FilmZanr WHERE FilmID = ? ORDER BY ZanrID ASC";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, idFilma);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(rs.getInt("ZanrID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    @Override
    public List<Integer> getMovieIdsByDirector(String reziser) {
        List<Integer> ids = new ArrayList<>();
        String q = "SELECT ID FROM Film WHERE Reziser = ? ORDER BY ID ASC";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setString(1, reziser);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(rs.getInt("ID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
}