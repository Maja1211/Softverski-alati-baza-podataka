package rs.etf.sab;

import rs.ac.bg.etf.sab.operations.*;
import rs.etf.sab.student.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;

public class StudentMain {
    public static void main(String[] args) throws Exception {

        GeneralOperations generalOperations = new sm220572_GeneralOperations();
        GenresOperations genresOperations = new sm220572_GenresOperations();
        MoviesOperations moviesOperations = new sm220572_MoviesOperations();
        RatingsOperations ratingsOperations = new sm220572_RatingsOperations();
        TagsOperations tagsOperations = new sm220572_TagsOperations();
        UsersOperations usersOperations = new sm220572_UsersOperations();
        WatchlistsOperations watchlistsOperations = new sm220572_WatchlistsOperations();

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperations,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);

        TestRunner.runTests();
    }
}



