package rs.etf.sab.student;
import rs.ac.bg.etf.sab.operations.UsersOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class sm220572_UsersOperations implements UsersOperations {
    private Connection getConnection() throws SQLException {
        return DB.getInstance().getConnection();
    }
    @Override
    public Integer addUser(String korisnickoIme) {
        if (korisnickoIme == null || korisnickoIme.trim().isEmpty())
            return null;
        String insertQuery = "INSERT INTO Korisnik (KorisnickoIme, BrojNagrada) OUTPUT INSERTED.ID VALUES (?, 0)";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setString(1, korisnickoIme.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            String msg = ex.getMessage();
            if (msg != null && (msg.contains("UNIQUE") || msg.contains("duplicate"))) {
                return null;
            }
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer updateUser(Integer id, String novoIme) {
        if (id == null || novoIme == null || novoIme.trim().isEmpty())
            return null;
        String updateQuery = "UPDATE Korisnik SET KorisnickoIme = ? WHERE ID = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setString(1, novoIme.trim());
                ps.setInt(2, id);
                int affected = ps.executeUpdate();
                if (affected == 0) return null;
                return id;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer removeUser(Integer id) {
        if (id == null)
            return null;
        String deleteQuery = "DELETE FROM Korisnik WHERE ID = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
                ps.setInt(1, id);
                int affected = ps.executeUpdate();
                if (affected == 0) return null;
                return id;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public boolean doesUserExist(String korisnickoIme) {
        if (korisnickoIme == null)
            return false;
        String query = "SELECT 1 FROM Korisnik WHERE KorisnickoIme = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, korisnickoIme.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public Integer getUserId(String korisnickoIme) {
        if (korisnickoIme == null)
            return null;
        String query = "SELECT ID FROM Korisnik WHERE KorisnickoIme = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, korisnickoIme.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return rs.getInt("ID");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public List<Integer> getAllUserIds() {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT ID FROM Korisnik";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ids.add(rs.getInt("ID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    @Override
    public Integer getRewards(Integer idKorisnika) {
        if (idKorisnika == null)
            return 0;
        String query = "SELECT BrojNagrada FROM Korisnik WHERE ID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, idKorisnika);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("BrojNagrada");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer korisnikId) {
        List<Integer> preporuke = new ArrayList<>();
        if (korisnikId == null) return preporuke;
        String query =
                "SELECT f.ID " +
                        "FROM Film f " +
                        "JOIN FilmZanr fz ON f.ID = fz.FilmID " +
                        "JOIN ( " +
                        "   SELECT fz.ZanrID " +
                        "   FROM Ocena o " +
                        "   JOIN FilmZanr fz ON o.FilmID = fz.FilmID " +
                        "   WHERE o.KorisnikID = ? " +
                        "   GROUP BY fz.ZanrID " +
                        "   HAVING AVG(CAST(o.Vrednost AS DECIMAL(10,3))) >= 8.000 " +
                        ") fav ON fz.ZanrID = fav.ZanrID " +
                        "LEFT JOIN ( " +
                        "   SELECT lf.FilmID " +
                        "   FROM ListaFilm lf " +
                        "   JOIN Lista l ON l.ID = lf.ListaID " +
                        "   WHERE l.KorisnikID = ? " +
                        ") wl ON wl.FilmID = f.ID " +
                        "LEFT JOIN Ocena o_user ON f.ID = o_user.FilmID AND o_user.KorisnikID = ? " +
                        "LEFT JOIN Ocena o_all ON f.ID = o_all.FilmID " +
                        "WHERE o_user.FilmID IS NULL AND wl.FilmID IS NULL " +
                        "GROUP BY f.ID " +
                        "HAVING (COUNT(o_all.Vrednost) >= 4 AND AVG(CAST(o_all.Vrednost AS DECIMAL(10,3))) >= 7.500) " +
                        "    OR (COUNT(o_all.Vrednost) < 4 AND AVG(CAST(o_all.Vrednost AS DECIMAL(10,3))) >= 9.000) " +
                        "ORDER BY AVG(CAST(o_all.Vrednost AS DECIMAL(10,3))) DESC, f.ID ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, korisnikId);
            ps.setInt(2, korisnikId);
            ps.setInt(3, korisnikId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    preporuke.add(rs.getInt("ID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return preporuke;
    }
    @Override
    public List<String> getThematicSpecializations(Integer korisnikId) {
        List<String> specijalizacije = new ArrayList<>();
        if (korisnikId == null) return specijalizacije;
        String query = "SELECT t.Naziv " +
                "FROM Tag t " +
                "JOIN FilmTag ft ON t.ID = ft.TagID " +
                "JOIN Ocena o ON ft.FilmID = o.FilmID " +
                "WHERE o.KorisnikID = ? AND o.Vrednost >= 8 " +
                "GROUP BY t.ID, t.Naziv " +
                "HAVING COUNT(DISTINCT o.FilmID) >= 2 " +
                "ORDER BY t.Naziv ASC";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, korisnikId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        specijalizacije.add(rs.getString("Naziv"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return specijalizacije;
    }
    @Override
    public String getUserDescription(Integer korisnikId) {
        if (korisnikId == null) return "undefined";
        String countOcena = "SELECT COUNT(*) FROM Ocena WHERE KorisnikID = ?";
        String countTags = "SELECT COUNT(DISTINCT ft.TagID) " +
                "FROM Ocena o JOIN FilmTag ft ON o.FilmID = ft.FilmID " +
                "WHERE o.KorisnikID = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement psOcena = conn.prepareStatement(countOcena);
                 PreparedStatement psTags = conn.prepareStatement(countTags)) {
                psOcena.setInt(1, korisnikId);
                int brojOcena = 0;
                try (ResultSet rsOcena = psOcena.executeQuery()) {
                    if (rsOcena.next())
                        brojOcena = rsOcena.getInt(1);
                }
                if (brojOcena < 10)
                    return "undefined";
                psTags.setInt(1, korisnikId);
                int brojTags = 0;
                try (ResultSet rsTags = psTags.executeQuery()) {
                    if (rsTags.next())
                        brojTags = rsTags.getInt(1);
                }
                return brojTags >= 10 ? "curious" : "focused";
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "undefined";
    }
}