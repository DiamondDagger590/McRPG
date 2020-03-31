package us.eunoians.mcrpg.party;

import lombok.Getter;

import java.util.Calendar;
import java.util.UUID;

public class TeleportRequest{
  
  /**
   * The player who sent the request
   */
  @Getter
  private UUID sender;
  
  /**
   * The player who received this request
   */
  @Getter
  private UUID recipient;
  
  /**
   * The time that this request was sent
   */
  @Getter
  private long timeSent;
  
  /**
   * This represents if it is a here or to request. If it is a /mcparty tp %player%, this should be true, otherwise it will be false
   */
  @Getter
  private boolean toRecipient;
  
  public TeleportRequest(UUID recipient, UUID sender, boolean toRecipient){
    this.recipient = recipient;
    this.sender = sender;
    this.toRecipient = toRecipient;
    this.timeSent = Calendar.getInstance().getTimeInMillis();
  }
}
