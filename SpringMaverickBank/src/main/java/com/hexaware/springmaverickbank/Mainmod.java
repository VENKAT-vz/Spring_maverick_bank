package com.hexaware.springmaverickbank;
import java.util.Scanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hexaware.maverickbank.dao.MaverickBankImpl;
import com.hexaware.maverickbank.model.Accounts;
import com.hexaware.maverickbank.model.UserDetails;

import java.sql.SQLException;
import java.util.List;

public class Mainmod {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String username;
        Scanner sc = new Scanner(System.in);

        ApplicationContext ctx = new ClassPathXmlApplicationContext("jdbc.xml");
        MaverickBankImpl dao = (MaverickBankImpl) ctx.getBean("maverickBankImpl");

        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.println("Do you have an existing user account? (yes/no)");
            String response = sc.next();
            
            if (response.equalsIgnoreCase("yes")) {
                System.out.println("Enter username:");
                String user = sc.next();
                System.out.println("Enter password:");
                String password = sc.next();
                password = dao.getCode(password);  
                
                String loginResult = dao.loginuser(user, password);
                if (loginResult.equals("Login successful")) {
                    System.out.println("Login successful. You may proceed.");
                    loggedIn = true;
                } else {
                    System.out.println("Invalid credentials. Try again.");
                }
            } else {
                System.out.println("Registering a new user:");
                System.out.println("Enter first name:");
                String firstname = sc.next();
                System.out.println("Enter last name:");
                String lastname = sc.next();
                System.out.println("Enter gender (M/F):");
                String gender = sc.next();
                System.out.println("Enter contact number:");
                String contact = sc.next();
                System.out.println("Enter address:");
                String address = sc.next();
                System.out.println("Enter city:");
                String city = sc.next();
                System.out.println("Enter state:");
                String state = sc.next();
                System.out.println("Enter username:");
                String user = sc.next();
                System.out.println("Enter password:");
                String password = sc.next();
                password = dao.getCode(password);  

                UserDetails userDetails = new UserDetails(firstname, lastname, gender, contact, address, city, state, user, password);
                System.out.println(dao.registeruser(userDetails));
            }
        }

        while (true) {
            System.out.println("Click\n1. Create account \n2. Search Account \n3. Deposit \n4. Withdraw\n5. Money Transfer \n6. Apply loan \n7. Exit");
            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Enter account type:");
                    String acctype = sc.next();
                    System.out.println("Enter initial balance:");
                    double balance = sc.nextDouble();
                    System.out.println("Enter username:");
                    String user = sc.next();
                    System.out.println(dao.createAccount(acctype, balance, user));
                    break;
                case 2:
                    System.out.println("Enter username:");
                    username = sc.next();
                    List<Accounts> accounts = dao.searchAccounts(username);
                    if (accounts != null) {
                        for (Accounts acc : accounts) {
                            System.out.println(acc);
                        }
                    } else {
                        System.out.println("***Record not found***");
                    }
                    break;
                case 3:
                    System.out.println("Enter account number:");
                    String accnum = sc.next();
                    System.out.println("Enter deposit amount:");
                    double deposit = sc.nextDouble();
                    System.out.println(dao.deposit(accnum, deposit));
                    break;
                case 4:
                    System.out.println("Enter account number:");
                    String accnum1 = sc.next();
                    System.out.println("Enter withdraw amount:");
                    double withdraw = sc.nextDouble();
                    System.out.println(dao.withdraw(accnum1, withdraw));
                    break;
                case 5:
                    System.out.println("Enter your (Sender) phone number:");
                    String phonenum1 = sc.next();
                    System.out.println("Enter your (Receiver) phone number:");
                    String phonenum2 = sc.next();
                    System.out.println("Enter transfer amount:");
                    double transfer = sc.nextDouble();
                    System.out.println(dao.moneytransfer(phonenum1, phonenum2, transfer));
                    break;
                case 6:
                    System.out.println("Enter your account number:");
                    String accnum2 = sc.next();
                    System.out.println("Enter the loan type:");
                    String ltype = sc.next();
                    System.out.println("Enter the loan amount:");
                    double lamount = sc.nextDouble();
                    System.out.println(dao.applyloan(accnum2, ltype, lamount));
                    break;
                case 7:
                    System.out.println("Exiting...");
                    return;
            }
        }
    }
}
