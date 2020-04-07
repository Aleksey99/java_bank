import entities.Account;
import entities.Currency;
import entities.Operation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class UI {
    private static final String EXIT = "e";
    private static final String LOGOUT = "o";

    private User user;

    private void mainMenuOut() {
        System.out.println();
        System.out.println("Available actions");
        System.out.println("1 - Create user");
        System.out.println("2 - LogIn by phone number");
        System.out.println("3 - LogIn by username (login)");
        System.out.println("e - Exit");
    }

    private void loggedInMenuOut() {
        System.out.println();
        System.out.println("User: " + user.getLogin());
        System.out.println("Available actions");
        System.out.println("1 - Get user info");
        System.out.println("2 - Get accounts info");
        System.out.println("3 - Create account");
        System.out.println("4 - Replenish account");
        System.out.println("5 - Transfer funds");
        System.out.println("6 - Show operations history");
        System.out.println("o - Logout");
        System.out.println("e - Exit");
    }

    private boolean loggedInMenu() {
        String input = "";
        while (!input.equals(EXIT) && !input.equals(LOGOUT)) {
            loggedInMenuOut();
            input = readConsole("Enter");
            clearConsole();
            switch (input) {
                case "1": System.out.println(user.getInfo()); break;
                case "2": System.out.println(user.getAccountsInfo()); break;
                case "3": createAccount(); break;
                case "4": replenishAccount(); break;
                case "5": transferFunds(); break;
                case "6": operationsHistory(); break;
                case LOGOUT: System.out.println(user.logOut()); break;
                default: break;
            }
        }
        return input.equals(EXIT);
    }

    private String readCurrency() {
        String currencies = Arrays.toString(Currency.values()).replaceAll("^.|.$", "");
        System.out.println("Choose currency from existed: " + currencies + ".");
        String currencyStr = readConsole("Enter currency").toUpperCase();
        try {
            Currency.valueOf(currencyStr);  // check currency
            return currencyStr;
        }
        catch (Exception ex) {
            System.out.println("Unexpected currency!");
            return "";
        }
    }

    private void createAccount() {
        System.out.println();
        String currencyStr = readCurrency();
        if (!currencyStr.equals(""))
            System.out.println(SQLConnector.createAccount(user.getId(), currencyStr));
    }

    private void replenishAccount() {
        try {
            ArrayList<Account> accounts = user.getAccounts();
            System.out.println();
            System.out.println(user.getAccountsInfo(accounts));

            Account acc;
            if (accounts == null || accounts.size() == 0)
                return;
            else if (accounts.size() == 1)
                acc = accounts.get(0);
            else {
                int index = Integer.parseInt(readConsole("Enter number of account to replenish it")) - 1;
                if (index < 0 || index >= accounts.size()) {
                    System.out.println("Incorrect number of account!");
                    return;
                }
                acc = accounts.get(index);
            }

            String currency = readCurrency();
            if (currency.equals(""))
                return;

            BigDecimal sum = new BigDecimal(readConsole("Enter amount"));
            if (sum.compareTo(new BigDecimal("0")) <= 0) {
                System.out.println("Incorrect amount!");
                return;
            }

            sum = Account.convert(sum, Currency.valueOf(currency), acc.getAccCode());
            boolean result = SQLConnector.replenishAccount(acc.getUUID(), sum);
            if (result)
                System.out.println("Completed successfully!");
            else
                System.out.println("Failed!");
        }
        catch (Exception ex) {
            System.out.println("Incorrect input!");
        }
    }

    private void transferFunds() {
        try {
            ArrayList<Account> accounts = user.getAccounts();

            System.out.println();
            System.out.println(user.getAccountsInfo(accounts));

            Account acc;
            if (accounts == null || accounts.size() == 0)
                return;
            else if (accounts.size() == 1)
                acc = accounts.get(0);
            else {
                int index = Integer.parseInt(readConsole("Enter number of account to transfer funds")) - 1;
                if (index < 0 || index >= accounts.size()) {
                    System.out.println("Incorrect number of account!");
                    return;
                }
                acc = accounts.get(index);
            }

            String number = readConsole("Enter phone number to transfer");
            if (!SQLConnector.numberExists(number)) {
                System.out.println("Account with this phone number doesn't exist!");
                return;
            }

            BigDecimal sum = new BigDecimal(readConsole("Enter transfer amount"));
            if (sum.compareTo(new BigDecimal("0")) <= 0 || sum.compareTo(acc.getAmount()) > 0) {
                System.out.println("Incorrect transfer amount!");
                return;
            }

            System.out.println("Currency of transfer");
            String toCurrency = readCurrency();
            if (toCurrency.equals(""))
                return;

            System.out.println(SQLConnector.transferFunds(user, acc, number, sum, toCurrency));
        }
        catch (Exception ex) {
            System.out.println("Incorrect input!");
        }
    }

    private void operationsHistory() {
        System.out.println();

        ArrayList<Operation> operations = SQLConnector.getOperations(user.getAccounts());
        if (operations.size() == 0)
            System.out.println("No history!");
        for (Operation op : operations) {
            System.out.println();
            System.out.println(op.getInfo());
            System.out.println();
        }
    }

    private void clearConsole() {
        try  {
            String os = System.getProperty("os.name");
            if (os.contains("Windows"))
                Runtime.getRuntime().exec("cls");
            else
                Runtime.getRuntime().exec("clear");
        }
        catch (final Exception e) {
            System.out.println("------------------------------");
        }
    }

    private String readConsole(String pre) {
        Scanner sc = new Scanner(System.in);
        System.out.print(pre + ": ");
        String result;
        do
            result = sc.nextLine();
        while (result.equals(""));
        return result;
    }

    private void createUser() {
        System.out.println();

        String login = readConsole("Enter login");

        String number = readConsole("Enter number");

        String address = readConsole("Enter address");

        String password = readConsole("Enter password");

        System.out.println(user.createUser(login, number, password, address));
    }

    private boolean logIn(boolean byNumber) {
        System.out.println();

        String value;
        if (byNumber)
            value = readConsole("Enter phone number");
        else
            value = readConsole("Enter login");

        String password = readConsole("Enter password");
        System.out.println(user.logIn(value, password, byNumber));

        if (user.getLoggedIn())
            return loggedInMenu();
        return false;
    }

    public UI() {
        user = new User();
    }

    public void start() {
        String input = "";
        boolean exit = false;
        while (!input.equals(EXIT) && !exit) {
            mainMenuOut();
            input = readConsole("Enter");
            clearConsole();
            switch (input) {
                case "1": createUser(); break;
                case "2": {
                    exit = logIn(true);
                    break;
                }
                case "3": {
                    exit = logIn(false);
                    break;
                }
                default: break;
            }
        }
    }

    public void close() {
        user.logOut();
    }
}
