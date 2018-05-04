package com.misiunas.mcs.minor

import java.util

import android.content.{Context, Intent, SharedPreferences}
import android.support.v4.app.Fragment
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup}
import com.google.android.gms.analytics.Tracker
import com.matthewtamlin.sliding_intro_screen_library.buttons.IntroButton
import com.matthewtamlin.sliding_intro_screen_library.buttons.IntroButton.Behaviour
import com.matthewtamlin.sliding_intro_screen_library.core.IntroActivity
import com.misiunas.mcs.{SweeperCraftApp, R}
import com.misiunas.mcs.game.config.GameMode
import com.misiunas.mcs.game.config.GameMode.{Adventure, Classic, Rescue}

/**
  * Created by kmisiunas on 2016-09-05.
  */
class TutorialActivity extends IntroActivity {

  protected var tracker: Tracker = null
  private var gameMode: String = ""

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    tracker = this.getApplication.asInstanceOf[SweeperCraftApp].getDefaultTracker
  }


  override def generatePages(bundle: Bundle): util.Collection[_ <: Fragment] = {
    val extras = getIntent().getExtras()
    val gameMode: String = if( extras != null && extras.containsKey("gameMode")){
      extras.getString("gameMode", "")
    } else {
      "all"
    }

    val pages: util.ArrayList[Fragment] = new util.ArrayList();

    pages.add(fragmentWithLayout(R.layout.tutorial_features))
    pages.add(fragmentWithLayout(R.layout.tutorial_classic))

    gameMode match {
      case "classic" => {}
      case "adventure" =>
        pages.add(fragmentWithLayout(R.layout.tutorial_adventure))
      case "rescue" =>
        pages.add(fragmentWithLayout(R.layout.tutorial_adventure))
        pages.add(fragmentWithLayout(R.layout.tutorial_rescue))
      case _ =>
        pages.add(fragmentWithLayout(R.layout.tutorial_adventure))
        pages.add(fragmentWithLayout(R.layout.tutorial_rescue))
        Log.d("Tutorial", "Universal match pattern init")
    }

    return pages;
  }

  override def generateFinalButtonBehaviour(): Behaviour = {
    val activity = this
    return new IntroButton.BehaviourAdapter {
      override def run(): Unit = activity.finish() // just close this activity
    };
  }

  def fragmentWithLayout(rootViewId: Int): Fragment = new FragmentWithLayout(rootViewId);

}

class FragmentWithLayout(rootViewId: Int) extends Fragment {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View =
    inflater.inflate(rootViewId, container, false)
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setRetainInstance(true)
  }
}

object TutorialActivity {

  def setShown(context: Context): Unit = {
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    editor.putBoolean("mcs.show_tutorial", true )
    editor.commit()
  }

  def wasItShow(context: Context): Boolean =
    PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mcs.show_tutorial", false)

  def show(context: Context, gm: GameMode): Unit =  {
    val intent: Intent = new Intent(context, classOf[TutorialActivity])
    intent.putExtra("gameMode", gm.toString);
    context.startActivity(intent)
  }

  def show(context: Context): Unit =  {
    val intent: Intent = new Intent(context, classOf[TutorialActivity])
    context.startActivity(intent)
  }

}
