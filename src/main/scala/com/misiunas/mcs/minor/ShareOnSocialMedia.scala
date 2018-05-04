package com.misiunas.mcs.minor

import android.content.{Context, Intent}
import com.misiunas.mcs.R
import com.misiunas.mcs.game.Operator
import com.misiunas.mcs.game.config.GameMode.Rescue

/**
  * Created by kmisiunas on 2016-09-07.
  */
object ShareOnSocialMedia {

  def share(context: Context, operator: Operator): Unit = {
    val msg = operator.mode match {
      case Rescue => context.getResources.getString(R.string.tweet_message_rescue)
      case _ => context.getResources.getString(R.string.tweet_message)
    }
    // parse [score] and [minutes]
    val sendMsg = msg
      .replace("[score]", operator.getScore().toString )
      .replace("[minutes]", (operator.getTime / 1000 / 60).toInt.toString )

    val intent = new Intent()
    intent.setAction(Intent.ACTION_SEND)
    intent.setType("text/plain")
    intent.putExtra(Intent.EXTRA_TEXT, sendMsg)
    context.startActivity(
      Intent.createChooser(intent, context.getResources.getString(R.string.tweet_share_title)
      ) )
  }


  def share(context: Context): Unit = {
    val sendMsg = context.getResources.getString(R.string.tweet_message_share)

    val intent = new Intent()
    intent.setAction(Intent.ACTION_SEND)
    intent.setType("text/plain")
    intent.putExtra(Intent.EXTRA_TEXT, sendMsg)
    context.startActivity(
      Intent.createChooser(intent, context.getResources.getString(R.string.tweet_share_title)
      ) )
  }

}
