import java.sql.SQLException;

public class Main {
    public void testData() {
        SQLConnector.createUser("User1", "Number1", "password1", "address1");
        SQLConnector.createUser("User2", "Number2", "password2", "address2");

        SQLConnector.createAccount(1, "RUB");
        SQLConnector.createAccount(2, "USD");
    }
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        SQLConnector.initDB();
        // testData();  // test accounts
        UI ui = new UI();
        ui.start();
        ui.close();
    }
}
