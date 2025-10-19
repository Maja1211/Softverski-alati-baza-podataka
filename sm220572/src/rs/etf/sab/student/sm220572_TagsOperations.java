package rs.etf.sab.student;
import rs.ac.bg.etf.sab.operations.TagsOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class sm220572_TagsOperations implements TagsOperations {
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
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    private Integer getTagId(String name) {
        String q = "SELECT ID FROM Tag WHERE Naziv = ?";
        Connection c = null;
        try {
            c = getConnection();
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("ID");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer addTag(Integer filmId, String tagName) {
        if (filmId == null || tagName == null || tagName.trim().isEmpty()) return null;
        tagName = tagName.trim();
        if (!existsById("Film", filmId)) return null;
        Connection conn = null;
        try {
            conn = getConnection();
            Integer tagId = getTagId(tagName);
            if (tagId == null) {
                String insTag = "INSERT INTO Tag (Naziv) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(insTag, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, tagName);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) tagId = rs.getInt(1);
                    }
                }
            }
            if (tagId == null) return null;
            String existsQ = "SELECT 1 FROM FilmTag WHERE FilmID = ? AND TagID = ?";
            try (PreparedStatement ps = conn.prepareStatement(existsQ)) {
                ps.setInt(1, filmId);
                ps.setInt(2, tagId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return null; // već postoji
                }
            }
            String insRel = "INSERT INTO FilmTag (FilmID, TagID) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insRel)) {
                ps.setInt(1, filmId);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }
            return filmId;
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer removeTag(Integer filmId, String nazivTaga) {
        if (filmId == null || nazivTaga == null || nazivTaga.trim().isEmpty()) return null;
        nazivTaga = nazivTaga.trim();
        if (!existsById("Film", filmId)) return null;
        Integer tagId = getTagId(nazivTaga);
        if (tagId == null) return null;
        Connection c = null;
        try {
            c = getConnection();
            String del = "DELETE FROM FilmTag WHERE FilmID = ? AND TagID = ?";
            try (PreparedStatement ps = c.prepareStatement(del)) {
                ps.setInt(1, filmId);
                ps.setInt(2, tagId);
                int rows = ps.executeUpdate();
                if (rows > 0) return filmId;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public int removeAllTagsForMovie(Integer filmId) {
        if (filmId == null) return 0;
        Connection c = null;
        try {
            c = getConnection();
            String q = "DELETE FROM FilmTag WHERE FilmID = ?";
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, filmId);
                return ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    @Override
    public boolean hasTag(Integer filmId, String nazivTaga) {
        if (filmId == null || nazivTaga == null || nazivTaga.trim().isEmpty()) return false;
        Connection c = null;
        try {
            c = getConnection();
            String q = "SELECT 1 FROM FilmTag ft JOIN Tag t ON ft.TagID = t.ID " +
                    "WHERE ft.FilmID = ? AND t.Naziv = ?";
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, filmId);
                ps.setString(2, nazivTaga.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public List<String> getTagsForMovie(Integer filmId) {
        List<String> tags = new ArrayList<>();
        if (filmId == null) return tags;
        Connection c = null;
        try {
            c = getConnection();
            String q = "SELECT t.Naziv FROM FilmTag ft JOIN Tag t ON ft.TagID = t.ID " +
                    "WHERE ft.FilmID = ? ORDER BY t.Naziv ASC";
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setInt(1, filmId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) tags.add(rs.getString("Naziv"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tags;
    }
    @Override
    public List<Integer> getMovieIdsByTag(String nazivTaga) {
        List<Integer> ids = new ArrayList<>();
        if (nazivTaga == null || nazivTaga.trim().isEmpty()) return ids;
        Connection c = null;
        try {
            c = getConnection();
            String q = "SELECT ft.FilmID FROM FilmTag ft JOIN Tag t ON ft.TagID = t.ID " +
                    "WHERE t.Naziv = ? ORDER BY ft.FilmID ASC";
            try (PreparedStatement ps = c.prepareStatement(q)) {
                ps.setString(1, nazivTaga.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) ids.add(rs.getInt("FilmID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    @Override
    public List<String> getAllTags() {
        List<String> tags = new ArrayList<>();
        Connection c = null;
        try {
            c = getConnection();
            String q = "SELECT DISTINCT Naziv FROM Tag ORDER BY Naziv ASC";
            try (PreparedStatement ps = c.prepareStatement(q);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tags.add(rs.getString("Naziv"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tags;
    }
}