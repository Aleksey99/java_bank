import entities.Account;
import entities.Currency;
import entities.Operation;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class SQLConnector {
    private static String JDBC_DRIVER = "org.sqlite.JDBC";
    private static String DATABASE_URL = "jdbc:sqlite:sql/database.db";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    private static void logIn(String field, String value, String password, User user) {
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = getConnection();
            Statement statement = con.createStatement();

            String query = "select * from User where %s='%s' and password='%s' limit 1";
            ResultSet rs = statement.executeQuery(String.format(query, field, value, password));
            if (rs.next()) {
                String log = rs.getString("login");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                Integer id = rs.getInt("id");
                user.setUser(log, phone, address, id);
            }

            rs.close();
            statement.close();
            con.close();
        }
        catch (Exception ex) { }
    }

    private static boolean hasAccount(Integer id, String currency) {
        try {
            String query = "select 1 from Account where client_id=%s and accCode='%s' limit 1";
            return executeHasAny(String.format(query, id, currency));
        }
        catch (Exception ex) {
            return true;
        }
    }

    public static void execute(String query) throws ClassNotFoundException, SQLException {
	    Class.forName(JDBC_DRIVER);
	    Connection con = getConnection();
	    Statement statement = con.createStatement();
	    statement.execute(query);
	    statement.close();
	    con.close();
    }

    public static boolean executeHasAny(String query) throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        Connection con = getConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(query);

        boolean result = false;
        if (rs.next())
            result = true;

        rs.close();
        statement.close();
        con.close();

        return result;
    }

    public static void initDB() throws ClassNotFoundException, SQLException {
        String UserTable =
                "create table if not exists User " +
                        "(id integer primary key autoincrement, " +
                        "login text not null, " +
                        "password text not null, " +
                        "address text, " +
                        "phone text not null);";
        String AccountTable =
                "create table if not exists Account " +
                        "(id text not null, " +
                        "client_id integer, " +
                        "amount real default 0, " +
                        "accCode text not null, " +
                        "foreign key (client_id) " +
                            "references User(id) on delete cascade);";
        String OperationTable =
                "create table if not exists Operation " +
                        "(id integer primary key autoincrement, " +
                        "date text not null," +
                        "accCode text not null," +
                        "accountFrom text not null," +
                        "accountTo text not null," +
                        "operationAmount real not null," +
                        "amountBefore real not null," +
                        "amountAfter real not null)";
        execute(UserTable);
        execute(AccountTable);
        execute(OperationTable);
    }

    public static void clearDB() throws SQLException, ClassNotFoundException {
        for (String table : new String[]{"User", "Account", "Operation", "sqlite_sequence"}) {
            execute("delete from " + table);
        }
    }

    public static void dropDB() throws SQLException, ClassNotFoundException {
        for (String table : new String[]{"User", "Account", "Operation"}) {
            execute("drop table if exists " + table);
        }
        execute("delete from sqlite_sequence");
    }

    public static boolean numberExists(String number) {
        try {
            return executeHasAny("select 1 from User where phone='" + number + "' limit 1");
        }
        catch (Exception ex) {
            return true;
        }
    }

    public static boolean loginExists(String login) {
        try {
            return executeHasAny("select 1 from User where login='" + login + "' limit 1");
        }
        catch (Exception ex) {
            return true;
        }
    }

    public static boolean createUser(String login, String number, String password, String address) {
        try {
            String format = "insert into User(login, password, address, phone) values ('%s', '%s', '%s', '%s')";
            execute(String.format(format, login, password, address, number));
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static void logInByNumber(String number, String password, User user) {
        logIn("phone", number, password, user);
    }

    public static void logInByLogin(String login, String password, User user) {
        logIn("login", login, password, user);
    }

    public static ArrayList<Account> getAccounts(Integer id) {
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = getConnection();
            Statement statement = con.createStatement();

            String query = "select * from Account where client_id=%s";
            ResultSet rs = statement.executeQuery(String.format(query, id));
            ArrayList<Account> accounts = new ArrayList<Account>();
            while (rs.next()) {
                String uuid = rs.getString("id");
                BigDecimal amount = new BigDecimal(rs.getString("amount"));
                Currency currency = Currency.valueOf(rs.getString("accCode"));
                Account acc = new Account(uuid, amount, currency);
                accounts.add(acc);
            }

            rs.close();
            statement.close();
            con.close();
            return accounts;
        }
        catch (Exception ex) {
            return new ArrayList<Account>();
        }
    }

    public static String createAccount(Integer id, String currencyStr) {
        if (hasAccount(id, currencyStr))
            return "Account with the same currency already exists!";

        try {
            String query = "insert into Account(id, client_id, accCode) values ('%s', '%s', '%s')";
            execute(String.format(query, UUID.randomUUID(), id, currencyStr));
            return "Account created!";
        }
        catch (Exception ex) {
            return "Failed to create account!";
        }
    }

    public static boolean replenishAccount(String uuid, BigDecimal sum) {
        try {
            String query = "update Account set amount=amount+cast('%s' as real) where id='%s'";
            execute(String.format(query, sum, uuid));
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

    public static String transferFunds(User user, Account acc, String number, BigDecimal sum, String toCurrency) {
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = getConnection();
            Statement statement = con.createStatement();

            String query = "select id from User where phone='%s' limit 1";
            ResultSet rs = statement.executeQuery(String.format(query, number));
            Integer id = -1;
            if (rs.next()) {
                id = rs.getInt("id");
            }
            rs.close();

            query = "select id, amount from Account where client_id=%s and accCode='%s' limit 1";
            rs = statement.executeQuery(String.format(query, id, toCurrency));
            String result;
            String toUUID;
            if (rs.next()) {
                toUUID = rs.getString("id");
                rs.close();

                if (toUUID.equals(acc.getUUID()))
                    result = "Attempt to transfer from one account to the same!";
                else {
                    BigDecimal newAmount = acc.getAmount().subtract(sum);
                    query = "update Account set amount=amount-cast('%s' as real) where id='%s'";
                    statement.execute(String.format(query, sum.toString(), acc.getUUID()));

                    BigDecimal addSum = Account.convert(sum, acc.getAccCode(), Currency.valueOf(toCurrency));
                    query = "update Account set amount=amount+cast('%s' as real) where id='%s'";
                    statement.execute(String.format(query, addSum.toString(), toUUID));

                    Operation op = new Operation(acc.getAccCode(), acc.getUUID(), toUUID, sum, acc.getAmount(), newAmount);
                    query = "insert into Operation(date, accCode, accountFrom, accountTo, " +
                            "operationAmount, amountBefore, amountAfter) values (%s)";
                    execute(String.format(query, op.toStrList()));

                    result = "Transferred successfully!";
                }
            }
            else {
                result = "User has no account with this currency!";
            }

            rs.close();
            statement.close();
            con.close();
            return result;
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "ERROR! Something went wrong!";
        }
    }

    public static ArrayList<Operation> getOperations(ArrayList<Account> accounts) {
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = getConnection();
            Statement statement = con.createStatement();
            ArrayList<Operation> operations = new ArrayList<>();

            ArrayList<String> uuids = new ArrayList<>();
            for (Account acc : accounts) {
                uuids.add("'" + acc.getUUID() + "'");
            }
            String query = "select * from Operation where accountFrom in (%s)";
            ResultSet rs = statement.executeQuery(String.format(query, String.join(", ", uuids)));

            while (rs.next()) {
                Operation op =
                        new Operation(rs.getString("date"), rs.getString("accCode"),
                                      rs.getString("accountFrom"), rs.getString("accountTo"),
                                      rs.getString("operationAmount"),
                                      rs.getString("amountBefore"), rs.getString("amountAfter"));
                operations.add(op);
            }

            rs.close();
            statement.close();
            con.close();
            return operations;
        }
        catch (Exception ex) {
            return new ArrayList<Operation>();
        }
    }
}
