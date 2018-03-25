# Bank Account transactions assigment
This project provides basic functionality of creating bank accounts of customers, viewing their account and transferring funds 
from one account to another in a thread-safe manner.It allows to access these features through REST endpoints. 

**Below are the details of REST services:**

**1. Create new account**
   - Endpoint    : /v1/accounts
   - Description : Service to create new account. If given account doesn't exist, it creates new account else it returns with 'account
   already exists' error.
   - Request     :
      ```
      POST localhost:18080/v1/accounts
      {
	      "accountId":"123",
	      "balance":1000
      }
      ```
   - Response    :
      ```
      HTTP Status- 201 Created
      ```

**2. View existing account**
   - Endpoint    : /v1/accounts/{accountId}
   - Description : Service to view existing account. {accountId} is the ID of account to be viewed.
   - Request     :
      ```
      GET localhost:18080/v1/accounts/123
      ```
     - Response    :
   
    {
	      "accountId":"123",
	      "balance":1000
      }
   

**3. Tranfer money from one account to another**
   - Endpoint    : /v1/accounts/transfer
   - Description : Service to tranfer money from one account to another. If valid details are provided in request, it returns with
   success message else it returns with error message.
   - Request     :
      ```   
      PUT localhost:18080/v1/accounts/transfer
      {
      	"accountFromId":"456",
	      "accountToId":"123",
	      "amountToTransfer":500
      }
      ```      
   - Response if request is valid:
   ```
      HTTP 200
      {
        "message":"Funds has been transferred successfully"
      }
   ```
     
    - Response if request is invalid:
    ```
      HTTP 400
      {
         "error": "Account with number '456' does not exist. Please provide valid account number."
      }
      ```
   
   
   **Below are some examples of invalid requests for /v1/accounts/transfer and their respective responses:**
   
   **1. Invalid account**
   
      PUT localhost:18080/v1/accounts/transfer
      {
        "accountFromId":"456",
        "accountToId":"123",
        "amountToTransfer":1000
      }
      Response:
      HTTP 400
      {
          "error": "Account with number '456' does not exist. Please provide valid account number."
      }


   **2. Invalid amount to transfer: amountToTransfer > availableBalance**
   
      PUT localhost:18080/v1/accounts/transfer
      {
        "accountFromId":"456",
        "accountToId":"123",
        "amountToTransfer":2000
      }
      Response:
      HTTP 400
      {
          "error": "You don't have sufficient balance."
      } 


**Logging of application:**

This application has logging provided in DEBUG and INFO level. By default, LOG level is set to INFO. It can be changed to any other 
log level using below config in application.yml:

logging: 
  level: 	 
    org.springframework: DEBUG
    

**Below are the logs for one of the positive test:**

**1. Create first account:**

2018-03-25 14:56:41.326  INFO 17012 --- [io-18080-exec-1] c.d.a.challenge.web.AccountsController   : Creating account Account(accountId=123, balance=1000, lock=java.lang.Object@5a6f82ff)

**2. Create second account:**

2018-03-25 14:56:45.308  INFO 17012 --- [io-18080-exec-3] c.d.a.challenge.web.AccountsController   : Creating account Account(accountId=456, balance=1000, lock=java.lang.Object@3529435f)

**3. First request for transferring funds:**

2018-03-25 14:56:49.724  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.web.AccountsController   : Transferring money: AmountTransferRequest(accountFromId=456, accountToId=123, amountToTransfer=500)
2018-03-25 14:56:49.724  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.service.AccountsService  : Request is validated successfully
2018-03-25 14:56:49.724  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.service.AccountsService  : Account 456 has been debited with amount:500. Updated balance: 500
2018-03-25 14:56:49.725  INFO 17012 --- [io-18080-exec-5] c.d.a.c.s.EmailNotificationService       : Sending notification to owner of 456: Your account has been debited with amount 500. Your updated balance is 500
2018-03-25 14:56:49.725  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.service.AccountsService  : Account 456 has been credited with amount:500. Updated balance: 1500
2018-03-25 14:56:49.725  INFO 17012 --- [io-18080-exec-5] c.d.a.c.s.EmailNotificationService       : Sending notification to owner of 123: Your account has been credited with amount 500. Your updated balance is 1500
2018-03-25 14:56:49.728  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.service.AccountsService  : Funds has been transferred successfully
2018-03-25 14:56:49.729  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.web.AccountsController   : Response: {"message":"Funds has been transferred successfully"}
2018-03-25 14:56:49.798  INFO 17012 --- [io-18080-exec-5] c.d.a.challenge.web.AccountsController   : Transaction was successful.

**3. Second requst for transferring funds:**

2018-03-25 14:57:53.321  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.web.AccountsController   : Transferring money: AmountTransferRequest(accountFromId=456, accountToId=123, amountToTransfer=500)
2018-03-25 14:57:53.321  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.service.AccountsService  : Request is validated successfully
2018-03-25 14:57:53.321  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.service.AccountsService  : Account 456 has been debited with amount:500. Updated balance: 0
2018-03-25 14:57:53.321  INFO 17012 --- [io-18080-exec-7] c.d.a.c.s.EmailNotificationService       : Sending notification to owner of 456: Your account has been debited with amount 500. Your updated balance is 0
2018-03-25 14:57:53.322  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.service.AccountsService  : Account 456 has been credited with amount:500. Updated balance: 2000
2018-03-25 14:57:53.322  INFO 17012 --- [io-18080-exec-7] c.d.a.c.s.EmailNotificationService       : Sending notification to owner of 123: Your account has been credited with amount 500. Your updated balance is 2000
2018-03-25 14:57:53.322  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.service.AccountsService  : Funds has been transferred successfully
2018-03-25 14:57:53.322  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.web.AccountsController   : Response: {"message":"Funds has been transferred successfully"}
2018-03-25 14:57:53.322  INFO 17012 --- [io-18080-exec-7] c.d.a.challenge.web.AccountsController   : Transaction was successful.

**4. Third request for transferring funds:**

2018-03-25 14:58:26.463  INFO 17012 --- [o-18080-exec-10] c.d.a.challenge.web.AccountsController   : Transferring money: AmountTransferRequest(accountFromId=456, accountToId=123, amountToTransfer=500)
2018-03-25 14:58:26.464 ERROR 17012 --- [o-18080-exec-10] c.d.a.challenge.service.AccountsService  : There is no sufficient balance to transfer.

