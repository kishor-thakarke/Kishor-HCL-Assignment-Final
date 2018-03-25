package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.*;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.util.Constants;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
	this.accountsService = accountsService;
  }
  
  /**
   * Creates account with given details in request.
   * @param account - account to be created
   * @return If account is created successfully, returns HTTP 201 status,
   * 		if account details are invalid, returns error response with HTTP 400 status.
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
	log.info("Creating account {}", account);
	try {
		this.accountsService.createAccount(account);
	} catch (DuplicateAccountIdException daie) {
    	return new ResponseEntity<>(generateErrorResponse(daie.getMessage()), HttpStatus.BAD_REQUEST);
	}
	return new ResponseEntity<>(HttpStatus.CREATED);
  }			
  
  /**
   * Performs fund transfer from one account to another.
   * @param transfer is a input request
   * @return JSON response with success message in case of successful funds transfer, 
   * 		otherwise returns error message 
   */
  @PutMapping(path="/transfer",consumes = MediaType.APPLICATION_JSON_VALUE,
		  produces = MediaType.APPLICATION_JSON_VALUE	)
  public ResponseEntity<Object> transferMoney(@RequestBody @Valid AmountTransferRequest transfer) {
    log.info("Transferring money: {}", transfer);
    JSONObject response = null;
    
    try {
    	response = this.accountsService.transferFunds(transfer);
    	if(null != response){
    		log.info("Response: {}", response);
    		log.info("Transaction was successful.");
    	    return new ResponseEntity<>(response, HttpStatus.OK);
    	}
    	
    	log.error("Funds transfer failed.");
        return new ResponseEntity<>(Constants.ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);

    }catch (InvalidAccountException | InsufficientBalanceException ex) {
    	return new ResponseEntity<>(generateErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    } 
  }

  /**
   * Retrieves account details of the given accountId
   * @param accountId - id of the account
   * @return account details of the given accountId
   */
  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }
  
  /**
   * Generates error response in JSON format.
   * @param message - error message to be returned in the response 
   * @return error response in JSON format
   */
  private JSONObject generateErrorResponse(String message) {
	JSONObject errorResponse = new JSONObject();
	errorResponse.put(Constants.ERROR, message);
	return errorResponse;
  }

}
