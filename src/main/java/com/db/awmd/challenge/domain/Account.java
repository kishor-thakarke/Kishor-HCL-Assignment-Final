package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Account {

  @NotNull
  @NotEmpty
  private final String accountId;
  
  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;
  
  private final Object lock = new Object(); 

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }
  
  @JsonIgnore
  public Object getLock(){
	  return lock;
  }
  
  
  
}
