//import config.ApplicationContext;

import java.io.IOException;
//import java.sql.*;

public class Main {
    public static void main(String[] args) throws IOException {
        MainApp.main(args);

//        final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
//        final String username= ApplicationContext.getPROPERTIES().getProperty("databse.socialnetwork.username");
//        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.pasword");
//


//        String sqlCommand = "UPDATE events SET creator=1";
//        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement()) {
//            statement.executeUpdate(sqlCommand);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//


//        try (Connection connection = DriverManager.getConnection(url,username,password);
//             PreparedStatement statement = connection.prepareStatement("SELECT * FROM participants");
//             ResultSet resultSet = statement.executeQuery()) {
//
//            while(resultSet.next())
//            {
//                Long event = resultSet.getLong("event_id");
//                Long user = resultSet.getLong("user_id");
//                try (Connection connection1 = DriverManager.getConnection(url, username, password);
//                     Statement statement1 = connection.createStatement()) {
//                     statement1.executeUpdate("UPDATE participants SET get_notifications=true, saw_notifications=false"
//                             + " WHERE event_id=" + event.toString() + " AND user_id=" + user.toString());
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }
}
