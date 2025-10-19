package rs.etf.sab.student;
import rs.ac.bg.etf.sab.operations.GeneralOperations;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
public class sm220572_GeneralOperations implements GeneralOperations {

    @Override
    public void eraseAll() {
        String[] deleteQueries = {
                "DELETE FROM ListaFilm",
                "DELETE FROM Lista",
                "DELETE FROM Ocena",
                "DELETE FROM FilmTag",
                "DELETE FROM FilmZanr",
                "DELETE FROM Film",
                "DELETE FROM Tag",
                "DELETE FROM Zanr",
                "DELETE FROM Korisnik"
        };


        Connection connection = null;

        try {
            connection = DB.getInstance().getConnection();

            for (String query : deleteQueries) {
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.executeUpdate();
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(sm220572_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

}