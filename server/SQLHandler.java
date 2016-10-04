
import java.sql.*;

public class SQLHandler {
    private static final String dbUrl = "jdbc:postgresql://localhost:5433/postgres";
    private static final String user = "postgres";
    private static final String password = "postgres"; // наверно нехорошо делать пароль публичным, но подругому не получилось
    private static final String SQL_SELECT = "SELECT Nickname,password FROM main WHERE Login = ?;";
    private static Connection conn;
    private static PreparedStatement stmt;

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

    public static String getNickByLoginPassword(String login, String password) {
        String w = null;
        try {
            stmt = conn.prepareStatement(SQL_SELECT);
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (password.equals(rs.getString("password"))) {
                    w = rs.getString("Nickname");
                } else {
                    w = "Auth error: Password incorrect";
                }
            } else {
                w = "Auth error: No such login";
            }
        } catch (SQLException e) {
            System.out.println("SQL Query Error");
        }
        return w;
    }

    public static boolean setNick(String login, String oldnick, String nick) {
        try {
            stmt = conn.prepareStatement("SELECT * FROM main WHERE Login = ? AND Nickname = ?;",
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            stmt.setString(1, login);
            stmt.setString(2, oldnick);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Statement statement = conn.createStatement();
                statement.executeUpdate("UPDATE main\n" +
                        "    SET nickname  = '" + nick + "'\n" +
                        "    WHERE login = '" + login + "' AND nickname = '" + oldnick + "';");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void saveMsg(String login, String name, String msg) {
        long timeMillis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(timeMillis);
        try {
            stmt = conn.prepareStatement("INSERT INTO history (datetime, login, name, message)\n" +
                    "\tVALUES (?, ?, ?, ?);");
            stmt.setTimestamp(1, timestamp);
            stmt.setString(2, login);
            stmt.setString(3, name);
            stmt.setString(4, msg);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getHistory () {
        String history = null;
        try {
            stmt = conn.prepareStatement("SELECT Name,Message FROM history ORDER BY ord;");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history += (rs.getString("name") + ": " + rs.getString("message") + "\n");
            }
            if (history != null)
                history = history.substring(4); //Избавимся от null
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public static String getHistory (String name) {
        String history = null;
        try {
            stmt = conn.prepareStatement("SELECT Name,Message FROM history WHERE name = ? ORDER BY ord;");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                history += (rs.getString("name") + ": " + rs.getString("message") + "\n");
            }
            if (history != null)
                history = history.substring(4); //Избавимся от null
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
