package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.List;
import java.util.Set;

import com.bullorbear.dynamodb.extensions.datastore.DatastoreObject;
import com.bullorbear.dynamodb.extensions.mapper.annotations.AutoGenerateId;
import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;
import com.bullorbear.dynamodb.extensions.mapper.annotations.Table;

@Table("player")
public class Player extends DatastoreObject {

  private static final long serialVersionUID = 2082956582761298174L;

  @HashKey
  @AutoGenerateId
  private String userId;

  private String name;

  private Game favouriteGame;

  private List<Score> scores;

  private Set<Game> allGamesPlayed;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Game getFavouriteGame() {
    return favouriteGame;
  }

  public void setFavouriteGame(Game favouriteGame) {
    this.favouriteGame = favouriteGame;
  }

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }

  public Set<Game> getAllGamesPlayed() {
    return allGamesPlayed;
  }

  public void setAllGamesPlayed(Set<Game> allGamesPlayed) {
    this.allGamesPlayed = allGamesPlayed;
  }

}
