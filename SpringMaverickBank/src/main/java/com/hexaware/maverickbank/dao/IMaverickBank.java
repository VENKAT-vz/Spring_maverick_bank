package com.hexaware.maverickbank.dao;

import java.sql.SQLException;
import java.util.List;

import com.hexaware.maverickbank.model.Accounts;
import com.hexaware.maverickbank.model.UserDetails;


public interface IMaverickBank {
    List<Accounts> searchAccounts(String username) throws SQLException, ClassNotFoundException;
	String createAccount(String account_type, double balance, String username)
			throws SQLException, ClassNotFoundException;
	 String deposit(String accountnumber, double amount) throws ClassNotFoundException, SQLException;
     String withdraw(String accountnumber, double amount) throws ClassNotFoundException, SQLException;
	 String moneytransfer(String phnum1, String phnum2, double amount) throws ClassNotFoundException, SQLException;
	 String applyloan(String accountId,String loantype,double loanAmount) throws SQLException;
	 
	String registeruser(UserDetails user) throws ClassNotFoundException, SQLException;
	String loginuser(String username,String password) throws ClassNotFoundException, SQLException;
}
