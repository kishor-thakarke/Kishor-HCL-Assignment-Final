package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.AmountTransferRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import net.minidev.json.JSONObject;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;
  
  @Mock
  private AccountsRepository accountsRepository;
  
  @Mock
  private NotificationService emailNotificationService;

  @Before
  public void setup() {
    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }
  
  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);
    
    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }
 
  }
  
  @Test(expected=InvalidAccountException.class)
  public void validateRequest_invalidFromAccount() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("100"));
  	 Whitebox.invokeMethod(accountsService, "validateRequest", transferRequest);
  	 Mockito.when(accountsRepository.getAccount(transferRequest.getAccountFromId())).thenReturn(null);
  	 
  }
  
  @Test(expected=InvalidAccountException.class)
  public void validateRequest_invalidToAccount() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("100"));
  	 Whitebox.invokeMethod(accountsService, "validateRequest", transferRequest);
  	 Mockito.when(accountsRepository.getAccount(transferRequest.getAccountToId())).thenReturn(null);
  	 
  }
  
  @Test 
  public void validateRequest_validAccount() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("200"));
	 Account accountFrom = new Account("123", new BigDecimal("1000"));
	 Account accountTo = new Account("456", new BigDecimal("1000"));
	 this.accountsService.createAccount(accountFrom);
	 this.accountsService.createAccount(accountTo);
	 Whitebox.invokeMethod(accountsService, "validateRequest", transferRequest);
  	 Mockito.when(accountsRepository.getAccount(transferRequest.getAccountFromId())).thenReturn(null);
  }
  
  @Test(expected=InsufficientBalanceException.class)
  public void validateRequest_invalidAmountToTransfer() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("2500"));
	 Account accountFrom = new Account("123", new BigDecimal("1000"));
	 Account accountTo = new Account("456", new BigDecimal("1000"));
	 this.accountsService.createAccount(accountFrom);
	 this.accountsService.createAccount(accountTo);
  	 Whitebox.invokeMethod(accountsService, "validateRequest", transferRequest);
  }
  
  @Test
  public void validateRequest_validAccountsAndAmountToTransfer() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("500"));
	 Account accountFrom = new Account("123", new BigDecimal("1000"));
	 Account accountTo = new Account("456", new BigDecimal("1000"));
	 this.accountsService.createAccount(accountFrom);
	 this.accountsService.createAccount(accountTo);
  	 Whitebox.invokeMethod(accountsService, "validateRequest", transferRequest);
  }
  
  @Test
  public void executeTransaction_validInput() throws Exception {
	 BigDecimal amountToTransfer = new BigDecimal(300);
	 Account accountFrom = new Account("123", new BigDecimal("1000"));
	 Account accountTo = new Account("456", new BigDecimal("1000"));
	 JSONObject response =  Whitebox.invokeMethod(accountsService, "executeTransaction", accountFrom, accountTo, amountToTransfer);
	 assertNotNull(response);
	 assertEquals("{\"message\":\"Funds has been transferred successfully\"}", response.toString());
  }
  
  @Test
  public void transferFunds_validInput() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("500"));
	 Account accountFrom = new Account("123", new BigDecimal("1000"));
	 Account accountTo = new Account("456", new BigDecimal("1000"));
	 this.accountsService.createAccount(accountFrom);
	 this.accountsService.createAccount(accountTo);

	 JSONObject response =  Whitebox.invokeMethod(accountsService, "transferFunds", transferRequest);
	 assertNotNull(response);
	 assertEquals("{\"message\":\"Funds has been transferred successfully\"}", response.toString());
  }
  
  @Test(expected=InvalidAccountException.class)
  public void transferFunds_invalidAccount() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("500"));
	 JSONObject response =  Whitebox.invokeMethod(accountsService, "transferFunds", transferRequest);
	 assertNotNull(response);
	 assertEquals("{\"message\":\"Funds has been transferred successfully\"}", response.toString());
  }
  
  @Test(expected=InsufficientBalanceException.class)
  public void transferFunds_invalidAmountToTransfer() throws Exception {
	 AmountTransferRequest transferRequest = new AmountTransferRequest("123", "456", new BigDecimal("1500"));
	 Account accountFrom = new Account("123", new BigDecimal("1000"));
	 Account accountTo = new Account("456", new BigDecimal("1000"));
	 this.accountsService.createAccount(accountFrom);
	 this.accountsService.createAccount(accountTo);
 
 	 Whitebox.invokeMethod(accountsService, "transferFunds", transferRequest);
  }
}
