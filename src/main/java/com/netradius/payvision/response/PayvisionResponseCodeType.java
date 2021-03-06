package com.netradius.payvision.response;

/**
 * Enumerates the Payvision Response Code types.
 *
 * @author Abhinav Nahar
 * @author Erik Jensen
 */
public enum PayvisionResponseCodeType {

  TRANSACTION_APPROVED("100"),
  TRANSACTION_DECLINED_PROCESSOR("200"),
  DO_NOT_HONOR("201"),
  INSUFFICIENT_FUNDS("202"),
  OVER_LIMIT("203"),
  TRANSACTION_NOT_ALLOWED("204"),
  INCORRECT_PAYMENT_INFORMATION("220"),
  NO_SUCH_CARD_ISSUER("221"),
  NO_CARD_NUMBER_ON_FILE_WITH_ISSUER("222"),
  EXPIRED_CARD("223"),
  INVALID_EXPIRATION_DATE("224"),
  INVALID_CARD_SECURITY_CODE("225"),
  CALL_ISSUER_FOR_FURTHER_INFORMATION("240"),
  PICK_UP_CARD("250"),
  LOST_CARD("251"),
  STOLEN_CARD("252"),
  FRAUDULENT_CARD("253"),
  DECLINED_WITH_FURTHER_INSTRUCTIONS_AVAILABLE("260"),
  DECLINED_STOP_ALL_RECURRING_PAYMENT("261"),
  DECLINED_STOP_THIS_RECURRING_PROGRAM("262"),
  DECLINED_UPDATE_CARDHOLDER_DATA_AVAILABLE("263"),
  DECLINED_RETRY_IN_A_FEW_DAYS("264"),
  TRANSACTION_WAS_REJECTED_BY_GATEWAY("300"),
  TRANSACTION_ERROR_RETURNED_BY_PROCESSOR("400"),
  INVALID_MERCHANT_CONFIGURATION("410"),
  MERCHANT_ACCOUNT_IS_INACTIVE("411"),
  COMMUNICATION_ERROR("420"),
  COMMUNICATION_ERROR_WITH_ISSUER("421"),
  DUPLICATE_TRANSACTION_AT_PROCESSOR("430"),
  PROCESSOR_FORMAT_ERROR("440"),
  INVALID_TRANSACTION_INFORMATION("441"),
  PROCESSOR_FEATURE_NOT_AVAILABLE("460"),
  UNSUPPORTED_CARD_TYPE("461");

  private String value;

  PayvisionResponseCodeType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static PayvisionResponseCodeType getByValue(String value) {
    switch (value) {
      case "100":
        return TRANSACTION_APPROVED;
      case "200":
        return TRANSACTION_DECLINED_PROCESSOR;
      case "201":
        return DO_NOT_HONOR;
      case "202":
        return INSUFFICIENT_FUNDS;
      case "203":
        return OVER_LIMIT;
      case "204":
        return TRANSACTION_NOT_ALLOWED;
      case "220":
        return INCORRECT_PAYMENT_INFORMATION;
      case "221":
        return NO_SUCH_CARD_ISSUER;
      case "222":
        return NO_CARD_NUMBER_ON_FILE_WITH_ISSUER;
      case "223":
        return EXPIRED_CARD;
      case "224":
        return INVALID_EXPIRATION_DATE;
      case "225":
        return INVALID_CARD_SECURITY_CODE;
      case "240":
        return CALL_ISSUER_FOR_FURTHER_INFORMATION;
      case "250":
        return PICK_UP_CARD;
      case "251":
        return LOST_CARD;
      case "252":
        return STOLEN_CARD;
      case "253":
        return FRAUDULENT_CARD;
      case "260":
        return DECLINED_WITH_FURTHER_INSTRUCTIONS_AVAILABLE;
      case "261":
        return DECLINED_STOP_ALL_RECURRING_PAYMENT;
      case "262":
        return DECLINED_STOP_THIS_RECURRING_PROGRAM;
      case "263":
        return DECLINED_UPDATE_CARDHOLDER_DATA_AVAILABLE;
      case "264":
        return DECLINED_RETRY_IN_A_FEW_DAYS;
      case "300":
        return TRANSACTION_WAS_REJECTED_BY_GATEWAY;
      case "400":
        return TRANSACTION_ERROR_RETURNED_BY_PROCESSOR;
      case "410":
        return INVALID_MERCHANT_CONFIGURATION;
      case "411":
        return MERCHANT_ACCOUNT_IS_INACTIVE;
      case "420":
        return COMMUNICATION_ERROR;
      case "421":
        return COMMUNICATION_ERROR_WITH_ISSUER;
      case "430":
        return DUPLICATE_TRANSACTION_AT_PROCESSOR;
      case "440":
        return PROCESSOR_FORMAT_ERROR;
      case "441":
        return INVALID_TRANSACTION_INFORMATION;
      case "460":
        return PROCESSOR_FEATURE_NOT_AVAILABLE;
      case "461":
        return UNSUPPORTED_CARD_TYPE;
      default:
        return null;
    }
  }

}
