import java.sql.*;
import java.util.HashMap;
import java.util.Set;

public class BD {
    //    private Connection connection;
    private HashMap<ServerThread, Connection> connections = new HashMap<>();

    {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

//    public BD() {
//        try {
//            Class.forName("org.sqlite.JDBC");
//            try {
//                connection = DriverManager.getConnection("jdbs:sqllite:MyDB.db");
//                System.out.println(connection.getCatalog());
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }

//    public void connect(ServerThread thread) {
////        try {
////            Class.forName("org.sqlite.JDBC");
//            try {
//                if (connections.containsKey(thread)){
//                    connection = connections.get(thread);
//                }else{
//                    connection = DriverManager.getConnection("jdbc:sqlite:MyDB.db");
//                    connections.put(thread, connection);
//                }
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
////        } catch (ClassNotFoundException e) {
////            e.printStackTrace();
////        }
//    }

    public void disconnect(ServerThread thread) {
        Connection connection = null;
        if (connections.containsKey(thread)) {
            connection = connections.get(thread);
            try {
                connection.close();
                connections.remove(thread);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

//    public Connection getConnection() {
//        return connection;
//    }

    public boolean getUser(ServerThread thread, String log, String pass) {
        boolean userFind = false;
        Connection connection = getConnection(thread);
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id FROM Users WHERE USER = '" + log + "' AND PASS = '" + pass + "'");
                if (rs.next()) {
                    userFind = true;
                }
                rs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userFind;
    }

    private Connection getConnection(ServerThread thread) {
        Connection connection = null;
        try {
            if (connections.containsKey(thread)) {
                connection = connections.get(thread);
            } else {
                connection = DriverManager.getConnection("jdbc:sqlite:MyDB.db");
                connections.put(thread, connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public boolean addUser(ServerThread thread, String log, String pass) {

        boolean userCreated = false;
        if (!getUser(thread, log, pass)) {
            Connection connection = getConnection(thread);
            if (connection != null) {
                try {
//                    String text = "INSERT INTO Users (USER, PASS) values('" + log + "','" + pass + "')";
                    Statement statement = connection.createStatement();
                    statement.execute("INSERT INTO Users (USER, PASS) VALUES('" + log + "','" + pass + "')");
                    if (Network.makeDir(log)) {
                        userCreated = true;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return userCreated;

    }

    public String getFile(ServerThread thread) {
        return "";
    }

    public boolean addFile(ServerThread thread) {
        boolean userCreated = false;
        Connection connection = getConnection(thread);
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
//                userCreated = statement.execute("INSERT INTO users (login, pass) values('" + log + "','" + pass + "'");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return userCreated;
    }
}
