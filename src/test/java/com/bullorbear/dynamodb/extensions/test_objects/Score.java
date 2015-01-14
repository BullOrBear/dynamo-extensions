package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.IndexRangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.RangeKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("scores")
public class Score extends DatastoreObject {

  private static final long serialVersionUID = -3560313819661029321L;

  @HashKey
  private String userId;

  @RangeKey
  private Date datePlayed;

  @IndexRangeKey(localSecondaryIndexName = "game_id-index")
  private String gameId;

  private String score;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getDatePlayed() {
    return datePlayed;
  }

  public void setDatePlayed(Date datePlayed) {
    this.datePlayed = datePlayed;
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }

}
