package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.Date;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.IndexHashKey;
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

  @IndexRangeKey(localSecondaryIndexNames = "game_id-index")
  @IndexHashKey(globalSecondaryIndexNames = "game_score-index")
  private String gameId;

  @IndexRangeKey(globalSecondaryIndexNames = "game_score-index")
  private String score;

  public Score() {
  }

  public Score(String userId, Date datePlayed, String gameId, String score) {
    super();
    this.userId = userId;
    this.datePlayed = datePlayed;
    this.gameId = gameId;
    this.score = score;
  }

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
