package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AmountTransferRequest;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import static com.db.awmd.challenge.util.Constants.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Getter 
  private NotificationService emailNotificationService;
  
  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
    this.emailNotificationService = emailNotificationService;
  }
 
  /**
   * Creates new account of customer
   * @param account - account details of customer
   */
  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  /**
   * Returns account details of given account ID
   * @param accountId - account whose details need to be returned 
   * @return account details of given account ID
   */
  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  /**
   * Transfers money from one account to another. 
   * @param transferRequest - request containing money transfer details
   * @return JSON response with success or error message.
   */
  public JSONObject transferFunds(AmountTransferRequest transferRequest){
	  // Validate request and throw appropriate exception if request is invalid
	  log.debug("Validating transferRequest: {}", transferRequest);
	  validateRequest(transferRequest);
	  log.info("Request is validated successfully");

	  // Read request details
	  Account accFrom = accountsRepository.getAccount(transferRequest.getAccountFromId());
	  Account accTo = accountsRepository.getAccount(transferRequest.getAccountToId());
	  BigDecimal amountToTransfer = transferRequest.getAmountToTransfer();
	  log.debug("{} amount will be transferred from account {} to account {}", amountToTransfer, accFrom.getAccountId(), accTo.getAccountId());
	  
	  JSONObject response = executeTransaction(accFrom, accTo, amountToTransfer);
	  if(null != response) {
		  log.info("Funds has been transferred successfully");
	  }else {
		  log.info("Funds transfer failed");
	  }
	
	  return response;
  }
  
  /**
   * Executes amount transfer transaction from one account to another in thread safe manner.
   * @param accFrom - account from which amount should be debited
   * @param accTo  - account to which amount should be credited
   * @param amountToTransfer - amount to be transferred 
   * @return JSON response with success or error message.
   */
  private JSONObject executeTransaction(Account accFrom, Account accTo, BigDecimal amountToTransfer) {
	  // Obtain locks on both account objects. Obtain first lock on account with lower id.
	  int accFromId = Integer.parseInt(accFrom.getAccountId());
	  int accToId = Integer.parseInt(accFrom.getAccountId());
	  Object lockOnAccWithLowerId = accFromId < accToId ? accFrom.getLock() : accTo.getLock();
	  Object lockOnAccWithHigherId = accFromId < accToId ? accTo.getLock() : accFrom.getLock();
	  JSONObject response = null;
	  
	  try {
		  synchronized (lockOnAccWithLowerId) {
		     synchronized (lockOnAccWithHigherId) {
			 	  //Subtract amount from source account
				  BigDecimal accFromBalance = accFrom.getBalance();
				  log.debug("Balance of account {} before debit: {}",accFrom.getAccountId(), accFromBalance);
				  accFromBalance = accFromBalance.subtract(amountToTransfer);
				  accFrom.setBalance(accFromBalance);
				  log.info("Account {} has been debited with amount:{}. Updated balance: {}", accFromId, amountToTransfer, accFrom.getBalance());
				  
				  log.debug("Sending notfication to source acc holder");
				  // Send notification to source acc holder
				  emailNotificationService.notifyAboutTransfer(accFrom, "Your account has been"
				  		+ " debited with amount "+ amountToTransfer + ". Your updated balance is "+accFrom.getBalance());
		
				  //Add amount to target account
				  BigDecimal accToBalance = accTo.getBalance();
				  log.debug("Balance of account {} before credit: {}",accTo.getAccountId(), accToBalance);
				  accToBalance = accToBalance.add(amountToTransfer);
				  accTo.setBalance(accToBalance);
				  log.info("Account {} has been credited with amount:{}. Updated balance: {}", accToId, amountToTransfer, accTo.getBalance());
				  
				  log.debug("Sending notification to dest acc holder");
				  // Send notification to target acc holder
				  emailNotificationService.notifyAboutTransfer(accTo, "Your account has been credited with amount "+ amountToTransfer 
						  + ". Your updated balance is "+accTo.getBalance());
				 
				  log.debug("Generating JSON response");
				  // Generate JSON response with transaction status details
				  response =  new JSONObject();
	         	  response.put(MESSAGE, "Funds has been transferred successfully");
	         	  log.info("Response: {}", response);
	          }
		  }
	  }
	  catch(Exception ex) {
		log.error("Exception occurred while executing transaction.", ex);  
	  }
	  
	  return response;
  }
  
  /**
   * Validates request and throws exception if request is invalid 
   * @param transferRequest
   */
  private void validateRequest(AmountTransferRequest transferRequest){
	  Account accFrom = accountsRepository.getAccount(transferRequest.getAccountFromId());
	  Account accTo = accountsRepository.getAccount(transferRequest.getAccountToId());
	  
	  if(null == accFrom){
		  log.error("Account with number '{}' does not exist. Please provide valid account number.", transferRequest.getAccountFromId());
		  throw new InvalidAccountException("Account with number '"+  transferRequest.getAccountFromId() + "' does not exist. "
		  		+ "Please provide valid account number.");
	  }
	  
	  if(null == accTo){
		  log.error("Account with number '{}' does not exist. Please provide valid account number.", transferRequest.getAccountToId());
		  throw new InvalidAccountException("Account with number '"+  transferRequest.getAccountToId() + "' does not exist. "
			  		+ "Please provide valid account number.");
	  }
	  
	  BigDecimal amountToTransfer = transferRequest.getAmountToTransfer();
	  BigDecimal accFromBalance = accFrom.getBalance();
	  
	  if(accFromBalance.compareTo(amountToTransfer) == -1){
		  log.error("There is no sufficient balance to transfer.");
		  throw new InsufficientBalanceException("You don't have sufficient balance.");
	  }
  }
   
}
