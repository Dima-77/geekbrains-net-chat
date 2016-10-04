import java.sql.*;

/**
 * Created by Дима on 10.09.2016.
 */
public class SqlQuery {
    private static final String dbUrl = "jdbc:postgresql://localhost:5433/postgres";
    private static final String user = "postgres";
    private static final String password = "postgres";
    //private static final String SQL_INSERT = "INSERT INTO main VALUES ('?', '?', '?');";
    private static final String SQL_QUERY = "SELECT FROM main WHERE login = ?;";
    private static final String SQL_INSERT = "INSERT INTO main VALUES (?, ?, ?);";
    private static Connection conn;
    private static PreparedStatement qstm;
    private static PreparedStatement stmt;

    public SqlQuery() {
        connect();
        fillTableMain();
        disconnect();
    }

    public static void connect() {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            conn = DriverManager.getConnection(dbUrl, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            conn.close();
        } catch (Exception c) {
            System.out.println("Connection Error");
        }
    }

    public static void fillTableMain() {
        String password;
        String nickname;
        String login;
        Boolean noLgn = false;
        for (int i = 0; i < 20; i++) {
            password = "pass" + i;
            nickname = "nick" + i;
            login = "login" + i;
            try {
                qstm = conn.prepareStatement(SQL_QUERY);
                qstm.setString(1, login);
                ResultSet rs = qstm.executeQuery();
                noLgn = !rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (noLgn) {
                try {
                    stmt = conn.prepareStatement(SQL_INSERT);
                    stmt.setString(1, password);
                    stmt.setString(2, nickname);
                    stmt.setString(3, login);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("This login already exists");
            }
        }
    }
}
