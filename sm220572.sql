CREATE DATABASE FilmoviDB;
GO

USE FilmoviDB;
GO

CREATE TABLE Korisnik (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    KorisnickoIme VARCHAR(100) UNIQUE NOT NULL,
    BrojNagrada INT DEFAULT 0
);

CREATE TABLE Film (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Naslov VARCHAR(100) NOT NULL,
    Reziser VARCHAR(100) NOT NULL
);

CREATE TABLE Zanr (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Naziv VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE Tag (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Naziv VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE FilmZanr (
    FilmID INT NOT NULL,
    ZanrID INT NOT NULL,
    PRIMARY KEY (FilmID, ZanrID),
    FOREIGN KEY (FilmID) REFERENCES Film(ID) ON UPDATE CASCADE ON DELETE NO ACTION,
    FOREIGN KEY (ZanrID) REFERENCES Zanr(ID) ON UPDATE CASCADE ON DELETE NO ACTION
);

CREATE TABLE FilmTag (
    FilmID INT NOT NULL,
    TagID INT NOT NULL,
    PRIMARY KEY (FilmID, TagID),
    FOREIGN KEY (FilmID) REFERENCES Film(ID) ON UPDATE CASCADE ON DELETE NO ACTION,
    FOREIGN KEY (TagID) REFERENCES Tag(ID) ON UPDATE CASCADE ON DELETE NO ACTION
);

CREATE TABLE Lista (
    ID INT IDENTITY(1,1) PRIMARY KEY,
    Naziv VARCHAR(100) NOT NULL,
    KorisnikID INT NOT NULL,
    FOREIGN KEY (KorisnikID) REFERENCES Korisnik(ID) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE ListaFilm (
    ListaID INT NOT NULL,
    FilmID INT NOT NULL,
    PRIMARY KEY (ListaID, FilmID),
    FOREIGN KEY (ListaID) REFERENCES Lista(ID) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (FilmID) REFERENCES Film(ID) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Ocena (
    KorisnikID INT NOT NULL,
    FilmID INT NOT NULL,
    Vrednost INT NOT NULL CHECK (Vrednost BETWEEN 1 AND 10),
    PRIMARY KEY (KorisnikID, FilmID),
    FOREIGN KEY (KorisnikID) REFERENCES Korisnik(ID) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (FilmID) REFERENCES Film(ID) ON UPDATE CASCADE ON DELETE CASCADE
);
GO




CREATE TRIGGER [dbo].[TR_BLOCK_EXTREME_]
ON [dbo].[Ocena]
AFTER INSERT, UPDATE
AS
BEGIN
    IF EXISTS (SELECT 1 FROM inserted WHERE Vrednost IN (1, 10))
    BEGIN
        DECLARE @KorisnikID INT, @FilmID INT, @Vrednost INT;
        DECLARE inserted_cursor CURSOR FOR SELECT KorisnikID, FilmID, Vrednost FROM inserted WHERE Vrednost IN (1,10);
        OPEN inserted_cursor;
        FETCH NEXT FROM inserted_cursor INTO @KorisnikID, @FilmID, @Vrednost;
        WHILE @@FETCH_STATUS = 0
        BEGIN
            DECLARE @ZanrID INT;
            DECLARE genre_cur CURSOR FOR SELECT ZanrID FROM FilmZanr WHERE FilmID = @FilmID;
            OPEN genre_cur;
            FETCH NEXT FROM genre_cur INTO @ZanrID;
            WHILE @@FETCH_STATUS = 0
            BEGIN
                DECLARE @Ekstremne INT = (SELECT COUNT(*) FROM Ocena o
                                          JOIN FilmZanr fz ON o.FilmID = fz.FilmID
                                          WHERE o.KorisnikID = @KorisnikID AND fz.ZanrID = @ZanrID
                                          AND o.Vrednost IN (1, 10));
                DECLARE @Neutralne INT = (SELECT COUNT(*) FROM Ocena o
                                          JOIN FilmZanr fz ON o.FilmID = fz.FilmID
                                          WHERE o.KorisnikID = @KorisnikID AND fz.ZanrID = @ZanrID
                                          AND o.Vrednost IN (6, 7, 8));
                IF @Ekstremne > 4 AND @Neutralne <= 3
                BEGIN
                    RAISERROR ('Ne možete dati ekstremnu ocenu u ovom žanru!', 16, 1);
                    ROLLBACK TRANSACTION;
                    RETURN;
                END
                FETCH NEXT FROM genre_cur INTO @ZanrID;
            END
            CLOSE genre_cur;
            DEALLOCATE genre_cur;
            FETCH NEXT FROM inserted_cursor INTO @KorisnikID, @FilmID, @Vrednost;
        END
        CLOSE inserted_cursor;
        DEALLOCATE inserted_cursor;
    END
END;


CREATE PROCEDURE [dbo].[SP_REWARD_USER]
    @IdKorisnik INT,
    @IdFilm INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Ocena INT;
    SELECT @Ocena = Vrednost
    FROM Ocena
    WHERE KorisnikID = @IdKorisnik AND FilmID = @IdFilm;
    IF @Ocena IS NULL 
        RETURN;

    DECLARE @UkupnoOcenjenih INT;
    SELECT @UkupnoOcenjenih = COUNT(*)
    FROM Ocena
    WHERE KorisnikID = @IdKorisnik;
    IF @UkupnoOcenjenih < 10 
        RETURN;

    DECLARE @IsFavorite BIT = 0;
    DECLARE @ZanrID INT;
    DECLARE zanr_cursor CURSOR FOR 
        SELECT ZanrID FROM FilmZanr WHERE FilmID = @IdFilm;
    OPEN zanr_cursor;
    FETCH NEXT FROM zanr_cursor INTO @ZanrID;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        DECLARE @ProsekZanr DECIMAL(10,3);
        SELECT @ProsekZanr = AVG(CAST(o.Vrednost AS DECIMAL(10,3)))
        FROM Ocena o
        JOIN FilmZanr fz ON o.FilmID = fz.FilmID
        WHERE o.KorisnikID = @IdKorisnik
          AND fz.ZanrID = @ZanrID
          AND o.FilmID <> @IdFilm;

        IF @ProsekZanr IS NOT NULL AND @ProsekZanr >= 8
        BEGIN
            SET @IsFavorite = 1;
            BREAK;
        END

        FETCH NEXT FROM zanr_cursor INTO @ZanrID;
    END
    CLOSE zanr_cursor;
    DEALLOCATE zanr_cursor;

    IF @IsFavorite = 0 
        RETURN;

    DECLARE @ProsekFilm DECIMAL(10,3);
    SELECT @ProsekFilm = AVG(CAST(Vrednost AS DECIMAL(10,3)))
    FROM Ocena
    WHERE FilmID = @IdFilm AND KorisnikID <> @IdKorisnik;

    IF @ProsekFilm IS NULL OR @ProsekFilm >= 6 
        RETURN;

    UPDATE Korisnik
    SET BrojNagrada = ISNULL(BrojNagrada, 0) + 1
    WHERE ID = @IdKorisnik;
END;
