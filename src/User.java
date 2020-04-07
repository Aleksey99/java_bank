import entities.Account;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;

public class User {
    private String login;
    private String number;
    private String address;
    private Integer id;
    private boolean loggedIn;
    private ArrayList<Account> accounts;

    private void init() {
        login = null;
        number = null;
        address = null;
        id = -1;
        loggedIn = false;
        accounts = null;
    }

    private String getHash(String str) {
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes(StandardCharsets.UTF_8));
            return (new BigInteger(1, crypt.digest())).toString(16);
        }
        catch (Exception ex) {
            return "";
        }
    }

    private int userExists(String login, String number) {
        boolean num = SQLConnector.numberExists(number);
        boolean log = SQLConnector.loginExists(login);
        if (num && log)
            return 1;
        if (num)
            return 2;
        if (log)
            return 3;
        return 0;
    }

    public User() {
        init();
    }

    public boolean getLoggedIn() {
        return loggedIn;
    }

    public String getLogin() {
        if (loggedIn)
            return login;
        else
            return "Need to log in!";
    }

    public Integer getId() {
        return id;
    }

    public String getInfo() {
        if (loggedIn)
            return String.format("login: %s\nphone number: %s\naddress: %s", login, number, address);
        else
            return "Need to log in!";
    }

    public String getAccountsInfo() {
        accounts = SQLConnector.getAccounts(id);
        return getAccountsInfo(accounts);
    }

    public String getAccountsInfo(ArrayList<Account> accs) {
        accounts = accs;

        int count = accounts.size();
        if (count == 0)
            return "You have no accounts!";
        StringBuilder str = new StringBuilder();
        str.append("You have " + count + " account(s):\n");
        int num = 1;
        for (Account acc : accounts) {
            str.append("\nAccount #" + num + "\n");
            str.append(acc.getInfo());
            str.append("\n");
            num++;
        }
        return str.toString();
    }

    public ArrayList<Account> getAccounts() {
        return SQLConnector.getAccounts(id);
    }

    public String createUser(String login, String number, String pswd, String address) {
        int info = userExists(login, number);
        switch (info) {
            case 0: {
                if (SQLConnector.createUser(login, number, getHash(pswd), address))
                    return "Created!";
                return "User was not created!";
            }
            case 1: return "User with same login and phone number already exists!";
            case 2: return "User with same phone number already exists!";
            case 3: return "User with same login already exists!";
            default: return "Unexpected";
        }
    }

    public void setUser(String login, String number, String address, Integer id) {
        this.loggedIn = true;
        this.login = login;
        this.number = number;
        this.address = address;
        this.id = id;
    }

    public String logIn(String input, String pswd, boolean isNumber) {
        if (isNumber)
            SQLConnector.logInByNumber(input, getHash(pswd), this);
        else
            SQLConnector.logInByLogin(input, getHash(pswd), this);

        if (!loggedIn)
            return "Incorrect login or password!";
        else
            return "Logged in!";
    }

    public String logOut() {
        if (loggedIn) {
            init();
            return "Logged out!";
        }
        else {
            return "Already logged out!";
        }
    }
}
