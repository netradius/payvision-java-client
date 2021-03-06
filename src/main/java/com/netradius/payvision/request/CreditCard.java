package com.netradius.payvision.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Holds the Credit Card details.
 *
 * @author Abhinav Nahar
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CreditCard extends PaymentType {

  private String ccnumber;

  @JsonProperty("ccexp")
  private String expirationDate; //Date format MMYY

  private String cvv;
}
