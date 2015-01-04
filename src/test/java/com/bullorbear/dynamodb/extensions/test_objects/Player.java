package com.bullorbear.dynamodb.extensions.test_objects;

import java.util.List;
import java.util.Set;

import com.bullorbear.dynamodb.extensions.mapper.annotations.HashKey;

public class Player {

  @HashKey
  private String name;

  private Game favouriteGame;

  private List<Score> scores;

  private Set<Game> allGamesPlayed;

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
