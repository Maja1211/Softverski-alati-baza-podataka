package rs.etf.sab.student;
import rs.ac.bg.etf.sab.operations.GenresOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class sm220572_GenresOperations implements GenresOperations {
    private Connection getConnection() throws SQLException {
        return DB.getInstance().getConnection();
    }
    @Override
    public Integer addGenre(String naziv) {
        if (naziv == null || naziv.trim().isEmpty())
            return null;
        if (doesGenreExist(naziv.trim()))
            return null;
        String insertQuery = "INSERT INTO Zanr (Naziv) VALUES (?)";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, naziv.trim());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next())
                        return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer updateGenre(Integer id, String noviNaziv) {
        if (id == null || noviNaziv == null || noviNaziv.trim().isEmpty())
            return null;
        if (!existsById(id))
            return null;
        if (doesGenreExist(noviNaziv.trim()))
            return null;
        String updateQuery = "UPDATE Zanr SET Naziv = ? WHERE ID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
                ps.setString(1, noviNaziv.trim());
                ps.setInt(2, id);
                int rows = ps.executeUpdate();
                if (rows > 0) return id;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public Integer removeGenre(Integer id) {
        if (id == null) return null;
        if (!existsById(id)) return null;
        String deleteRelations = "DELETE FROM FilmZanr WHERE ZanrID = ?";
        String deleteGenre = "DELETE FROM Zanr WHERE ID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps1 = connection.prepareStatement(deleteRelations)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = connection.prepareStatement(deleteGenre)) {
                ps2.setInt(1, id);
                int rows = ps2.executeUpdate();
                if (rows > 0) return id;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public boolean doesGenreExist(String naziv) {
        if (naziv == null || naziv.trim().isEmpty())
            return false;
        String query = "SELECT 1 FROM Zanr WHERE Naziv = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, naziv.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public Integer getGenreId(String naziv) {
        if (naziv == null || naziv.trim().isEmpty())
            return null;
        String query = "SELECT ID FROM Zanr WHERE Naziv = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, naziv.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return rs.getInt("ID");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public List<Integer> getAllGenreIds() {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT ID FROM Zanr ORDER BY ID ASC";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    ids.add(rs.getInt("ID"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }
    private boolean existsById(int id) {
        String query = "SELECT 1 FROM Zanr WHERE ID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}