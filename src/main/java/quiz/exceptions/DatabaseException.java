package quiz.exceptions; // Asigură-te că este în pachetul util, așa cum este folosit în DBUtil și DAO-uri

import java.sql.SQLException;

public class DatabaseException extends SQLException { // Extinde SQLException pentru a menține tipul de excepție SQL
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}