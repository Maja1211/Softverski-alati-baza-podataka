package rs.etf.sab.student;
import rs.ac.bg.etf.sab.operations.WatchlistsOperations;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
public class sm220572_WatchlistsOperations implements WatchlistsOperations {
    private Connection getConnection() throws SQLException {
        return DB.getInstance().getConnection();
    }
    @Override
    public boolean addMovieToWatchlist(Integer korisnikId, Integer filmId) {
        if (korisnikId == null || filmId == null)
            return false;
        Connection connection = null;
        try {
            connection = getConnection();

            Integer listaId = null;
            String findList = "SELECT ID FROM Lista WHERE KorisnikID = ?";
            try (PreparedStatement ps = connection.prepareStatement(findList)) {
                ps.setInt(1, korisnikId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        listaId = rs.getInt(1);
                    }
                }
            }

            if (listaId == null) {
                String createList = "INSERT INTO Lista (Naziv, KorisnikID) VALUES (?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(createList, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Watchlist korisnika " + korisnikId);
                    ps.setInt(2, korisnikId);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            listaId = rs.getInt(1);
                        }
                    }
                }
            }

            String check = "SELECT COUNT(*) FROM ListaFilm WHERE ListaID = ? AND FilmID = ?";
            try (PreparedStatement ps = connection.prepareStatement(check)) {
                ps.setInt(1, listaId);
                ps.setInt(2, filmId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0)
                        return false;
                }
            }

            String insert = "INSERT INTO ListaFilm (ListaID, FilmID) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(insert)) {
                ps.setInt(1, listaId);
                ps.setInt(2, filmId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public boolean removeMovieFromWatchlist(Integer korisnikId, Integer filmId) {
        if (korisnikId == null || filmId == null)
            return false;
        Connection connection = null;
        try {
            connection = getConnection();

            Integer listaId = null;
            String findList = "SELECT ID FROM Lista WHERE KorisnikID = ?";
            try (PreparedStatement ps = connection.prepareStatement(findList)) {
                ps.setInt(1, korisnikId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        listaId = rs.getInt(1);
                    }
                }
            }
            if (listaId == null)
                return false;
            String delete = "DELETE FROM ListaFilm WHERE ListaID = ? AND FilmID = ?";
            try (PreparedStatement ps = connection.prepareStatement(delete)) {
                ps.setInt(1, listaId);
                ps.setInt(2, filmId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public boolean isMovieInWatchlist(Integer korisnikId, Integer filmId) {
        if (korisnikId == null || filmId == null)
            return false;
        String query = "SELECT COUNT(*) FROM ListaFilm lf JOIN Lista l ON lf.ListaID = l.ID " +
                "WHERE l.KorisnikID = ? AND lf.FilmID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, korisnikId);
                ps.setInt(2, filmId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    @Override
    public List<Integer> getMoviesInWatchlist(Integer korisnikId) {
        List<Integer> filmovi = new ArrayList<>();
        if (korisnikId == null)
            return filmovi;
        String query = "SELECT lf.FilmID FROM ListaFilm lf JOIN Lista l ON lf.ListaID = l.ID WHERE l.KorisnikID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, korisnikId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        filmovi.add(rs.getInt("FilmID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filmovi;
    }
    @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer filmId) {
        List<Integer> korisnici = new ArrayList<>();
        if (filmId == null)
            return korisnici;
        String query = "SELECT DISTINCT l.KorisnikID " +
                "FROM Lista l JOIN ListaFilm lf ON l.ID = lf.ListaID " +
                "WHERE lf.FilmID = ?";
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, filmId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        korisnici.add(rs.getInt("KorisnikID"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(sm220572_WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return korisnici;
    }
}