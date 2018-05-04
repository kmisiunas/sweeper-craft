package com.misiunas.mcs.highscore

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.misiunas.mcs.R
import com.misiunas.mcs.basegameutils.GameHelper
import com.misiunas.mcs.game.Operator
import com.misiunas.mcs.game.config.{GameDifficulty, GameMode}

/**
  * Created by kmisiunas on 2016-08-12.
  */
object Leaderboard {

  def add(context: Context, gameHelper: GameHelper, operator: Operator): Boolean = {
    // todo deal with offline and not signed in
    if (gameHelper.isSignedIn) {
      // todo check if tampered
      // find game id
      val id = (operator.mode, operator.difficulty) match {
        case (GameMode.Classic, GameDifficulty.Easy) => R.string.leaderboard_classic_easy
        case (GameMode.Classic, GameDifficulty.Medium) => R.string.leaderboard_classic_medium
        case (GameMode.Classic, GameDifficulty.Hard) => R.string.leaderboard_classic_hard
        case (GameMode.Adventure, GameDifficulty.Easy) => R.string.leaderboard_adventure_easy
        case (GameMode.Adventure, GameDifficulty.Medium) => R.string.leaderboard_adventure_medium
        case (GameMode.Adventure, GameDifficulty.Hard) => R.string.leaderboard_adventure_hard
        case (GameMode.Rescue, GameDifficulty.Easy) => R.string.leaderboard_rescue_easy
        case (GameMode.Rescue, GameDifficulty.Medium) => R.string.leaderboard_rescue_medium
        case (GameMode.Rescue, GameDifficulty.Hard) => R.string.leaderboard_rescue_hard
        case _ =>
          throw new IllegalArgumentException("Unexpected combo in Leaderboard.add:" + (operator.mode, operator.difficulty))
      }
      // submit score
      Games.Leaderboards.submitScore(gameHelper.getApiClient, context.getResources.getString(id), operator.getScore())
      return true
    } else {
      return false
    }
  }

}
