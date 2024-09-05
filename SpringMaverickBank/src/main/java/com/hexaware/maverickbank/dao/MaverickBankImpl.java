package com.hexaware.maverickbank.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.hexaware.maverickbank.model.Accounts;
import com.hexaware.maverickbank.model.UserDetails;

public class MaverickBankImpl implements IMaverickBank{

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	  
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {   
		this.jdbcTemplate = jdbcTemplate;
	}
	  
	@Override
	public List<Accounts> searchAccounts(String username) throws SQLException, ClassNotFoundException {
        String query = "SELECT * FROM accounts WHERE username = ?";
        
        List<Accounts> accountsList = jdbcTemplate.query(query, new Object[]{username}, new RowMapper<Accounts>() {
            @Override
            public Accounts mapRow(ResultSet rs, int rowNum) throws SQLException {
                Accounts account = new Accounts();
                account.setAccountNumber(rs.getString("account_number"));
                account.setAccountType(rs.getString("account_type"));
                account.setBalance(rs.getDouble("balance"));
                account.setBranchName(rs.getString("branch_name"));
                account.setIfscCode(rs.getString("ifsc_code"));
                account.setStatus(rs.getString("status"));
                return account;
            }
        });

        return accountsList;
	}
	@Override
	public String createAccount(String account_type, double balance, String username)
			throws SQLException, ClassNotFoundException {
        String accno = generateAccountNo();
        boolean usrchk = checkuser(username);

        if (usrchk) {
            Accounts account = new Accounts(accno, account_type, balance);

            String query = "INSERT INTO accounts (account_number, account_type, balance, branch_name, ifsc_code, status, username) VALUES (?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.update(query, account.getAccountNumber(), account.getAccountType(), account.getBalance(),
                    account.getBranchName(), account.getIfscCode(), account.getStatus(), username);

            return "Account Created with Account No " + accno;
        } else {
            return "User is not registered. Register and create account.";
        }
	}
	
	@Override
	public String deposit(String accountnumber, double amount) throws ClassNotFoundException, SQLException {
        String updateAccountQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String insertTransactionQuery = "INSERT INTO transactions (account_number, trans_amount, trans_date, trans_type) VALUES (?, ?, ?, 'deposit')";

        jdbcTemplate.update(updateAccountQuery, amount, accountnumber);

        jdbcTemplate.update(insertTransactionQuery, accountnumber, amount, new Timestamp(new Date().getTime()));
        
        return "Amount deposited...";
    }

	
	@Override
	public String withdraw(String accountnumber, double amount) throws ClassNotFoundException, SQLException {
		double currentBalance = getCurrentBalance(accountnumber);  
		if (amount <= 0 || currentBalance < amount) {
			   	return " Insufficient balance";
	   }

	 String updateAccountQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
     
	 String insertTransactionQuery = "INSERT INTO transactions (account_number, trans_amount, trans_date, trans_type) VALUES (?, ?, ?, 'withdrawal')";

		 jdbcTemplate.update(updateAccountQuery, amount, accountnumber);
		jdbcTemplate.update(insertTransactionQuery, accountnumber, amount, new Timestamp(new Date().getTime()));
		
		return "Amount withdrawn";
	}
	
		
	@Override
	public String moneytransfer(String phnum1, String phnum2, double amount)
				throws ClassNotFoundException, SQLException {
	        if (amount <= 0) {
	        	return "Amount must be positive";
	        }

	      
	     String senderAccountNumber = getAccountNumberByPhone(phnum1);
	     String receiverAccountNumber = getAccountNumberByPhone(phnum2);

	     double senderBalance = getCurrentBalance(senderAccountNumber);
	        if (senderBalance < amount) 
		      	return "Insufficient balance";
	            
	      String updateSenderQuery = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
	        jdbcTemplate.update(updateSenderQuery, amount, senderAccountNumber);

	        String updateReceiverQuery = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
	         jdbcTemplate.update(updateReceiverQuery, amount, receiverAccountNumber);


	       String insertTransactionQuery = "INSERT INTO transactions (account_number, trans_amount, trans_date, trans_type) VALUES (?, ?, ?, ?)";
	          jdbcTemplate.update(insertTransactionQuery, senderAccountNumber, amount, new Timestamp(new Date().getTime()), "transfer-out");
	        jdbcTemplate.update(insertTransactionQuery, receiverAccountNumber, amount, new Timestamp(new Date().getTime()), "transfer-in");

	     return "Transfer successful: " + amount + " transferred from " + senderAccountNumber + " to " + receiverAccountNumber+"via their phone number";
		}
	            

		
		@Override
		public String applyloan(String accountId, String loantype, double loanAmount) throws SQLException {
			String loanId = generateLoanId();

	       String insertLoanQuery = "INSERT INTO loans (loan_id, loan_type, loan_amount, status, installments,account_number) " +
		                                 "VALUES (?, ?, ?, ?, ?, ?)";

		   jdbcTemplate.update(insertLoanQuery, loanId, loantype, loanAmount, "Pending", 12,accountId);

	     return "Loan application successful with Loan ID: " + loanId;		
		        
		}
		
		
	    private String generateLoanId() throws SQLException {
	        String query = "SELECT CASE WHEN MAX(loan_id) IS NULL THEN 2000 ELSE MAX(loan_id) + 1 END AS newLoanId FROM loans";
	        return jdbcTemplate.queryForObject(query, String.class);
	    }
	    
	    private String getAccountNumberByPhone(String phnum) {
	        String query = "SELECT a.account_number FROM accounts a JOIN users u ON a.username = u.username WHERE u.contact_number = ? LIMIT 1";
	        return jdbcTemplate.queryForObject(query, new Object[]{phnum}, String.class);
	    }

		 private String generateAccountNo() throws ClassNotFoundException, SQLException {
		      String query = "SELECT CASE WHEN MAX(account_number) IS NULL THEN 1000 ELSE MAX(account_number) + 1 END AS accno FROM accounts";
		        
		            Integer accno = jdbcTemplate.queryForObject(query, Integer.class);
		            return accno != null ? accno.toString() : "1000";  
    
		 }
		 
		 private boolean checkuser(String username) throws ClassNotFoundException, SQLException {
		        String query = "SELECT username FROM users WHERE username = ?";
		            String result = jdbcTemplate.queryForObject(query, new Object[]{username}, String.class);
		            return result != null;      
		    }
		 
			
		 private double getCurrentBalance(String accountNumber) throws SQLException {
			    String query = "SELECT balance FROM accounts WHERE account_number = ?";

			     return jdbcTemplate.queryForObject(query, new Object[]{accountNumber}, Double.class);
			   
			}


		@Override
		public String registeruser(UserDetails user) throws ClassNotFoundException, SQLException {
			String query = "INSERT INTO users (firstname, lastname, gender, contact_number, address, city, state, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	        jdbcTemplate.update(query, user.getFirstname(), user.getLastname(), user.getGender(), user.getContactNumber(), 
	            user.getAddress(), user.getCity(), user.getState(), user.getUsername(), user.getPassword());
	        
	        return "Account Created...";			
		}

		@Override
		public String loginuser(String username, String password) throws ClassNotFoundException, SQLException {
	        String query = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
	        
	        int count = jdbcTemplate.queryForObject(query, new Object[]{username, password}, Integer.class);
	        
	        if (count > 0) {
	            return "Login successful";
	        }
	        return "Invalid Credentials. Try again.";
		}
		
		public  String getCode(String password) {
	        String encryptedpassword = null;  
	        try   
	        {  
	            MessageDigest m = MessageDigest.getInstance("MD5");  
	              
	            m.update(password.getBytes());  
	              
	            byte[] bytes = m.digest();  
	              
	            StringBuilder s = new StringBuilder();  
	            for(int i=0; i< bytes.length ;i++)  
	            {  
	                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));  
	            }  
	              
	            encryptedpassword = s.toString();  
	        }   
	        catch (NoSuchAlgorithmException e)   
	        {  
	            e.printStackTrace();  
	        }  
	          
	        return encryptedpassword;
		}
	}
