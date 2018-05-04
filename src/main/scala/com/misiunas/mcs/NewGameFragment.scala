package com.misiunas.mcs

import java.util

import android.app.{Activity, ProgressDialog}
import android.content.{Context, Intent, SharedPreferences}
import android.os.{Bundle, Handler, Message}
import android.support.v4.app.{Fragment, FragmentActivity}
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget._
import com.misiunas.mcs.game.{GameActivity, StateCreator}
import com.misiunas.mcs.game.config.{GameDifficulty, GameMode, GameSize}
import android.widget.ArrayAdapter
import com.google.android.gms.ads.AdView
import com.google.android.gms.analytics.{HitBuilders, Tracker}
import com.misiunas.mcs.minor.AdHelper

/**
  * Created by kmisiunas on 2016-07-20.
  */
class NewGameFragment extends Fragment  {

  protected var tracker: Tracker = null

  def mapGameMode: Map[Int, GameMode] = Map(
    0 -> GameMode.Adventure,
    1 -> GameMode.Rescue,
    2 -> GameMode.Classic
  )

  def mapGameSize(mode: GameMode): Map[Int, GameSize] = mode match {
    case GameMode.Classic =>
      Map( 0 -> GameSize.small, 1 -> GameSize.medium, 2 -> GameSize.large, 3 -> GameSize.infinite)
    case _ =>
      Map( 0 -> GameSize.large, 1 -> GameSize.infinite)
  }

  def mapGameDifficulty: Map[Int, GameDifficulty] = Map(
    0 -> GameDifficulty.easy,
    1 -> GameDifficulty.medium,
    2 -> GameDifficulty.hard
  )


  private val PREFERENCES_MAP_SIZE: String = "mcs.private.size"
  private val PREFERENCES_DIFFICULTY: String = "mcs.private.difficulty"
  private val PREFERENCES_MODE: String = "mcs.private.mode"

  private var sizeSpinner: Spinner = null
  private var modeSpinner: Spinner = null
  private var diffSpinner: Spinner = null

  private var modeDescription: TextView = null

  private var pd: ProgressDialog = null

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
  }


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    tracker = this.getActivity.getApplication.asInstanceOf[SweeperCraftApp].getDefaultTracker
    val returnView: View = inflater.inflate(R.layout.newgame, container, false)
    sizeSpinner = returnView.findViewById(R.id.spinner_map_size).asInstanceOf[Spinner]
    modeSpinner = returnView.findViewById(R.id.spinner_game_mode).asInstanceOf[Spinner]
    diffSpinner = returnView.findViewById(R.id.spinner_difficulty).asInstanceOf[Spinner]
    modeDescription = returnView.findViewById(R.id.modeDescription).asInstanceOf[TextView]
    /** Handles continue button */
    val bt_start: Button = returnView.findViewById(R.id.button_start_game).asInstanceOf[Button]
    bt_start.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        startGame()
      }
    })
    // add listener to the spinner
    modeSpinner.setOnItemSelectedListener(new MyOnItemSelectedListener)
    // In case the there is auto start flag enabled
    if (NewGameFragment.autoStart) {
      startGame()
      NewGameFragment.autoStart = false
    }
    AdHelper.prepare(
      returnView.findViewById(R.id.adView).asInstanceOf[AdView],
      returnView.findViewById(R.id.linearLayoutAd),
      getActivity.getApplicationContext()  )
    return returnView
  }

  override def onPause() {
    super.onPause()
    saveSettings() // save settings for future use
  }

  override def onResume() {
    super.onResume()
    loadSettings() // loads previously used settings
  }

  private var timeStartMapCreation: Long = 0L

  private def startGame() {
    pd = showProgressDialog
    timeStartMapCreation = System.currentTimeMillis()
    val searchThread: CreateThread = new CreateThread(getActivity.getApplication)
    searchThread.start()
  }

  /**
    * thread is created for population, in order to display progress message
    */
  class CreateThread(var context: Context) extends Thread {
    override def run() {
      createGameState()
      handler.sendEmptyMessage(0)
    }

    val handler: Handler = new Handler() {
      override def handleMessage(msg: Message) {
        val myIntent: Intent = new Intent(context, classOf[GameActivity])
        pd.dismiss()
        // track time to create
        val mode: GameMode = mapGameMode( modeSpinner.getSelectedItemPosition )
        val size: GameSize = mapGameSize(mode)( sizeSpinner.getSelectedItemPosition )
        tracker.send(new HitBuilders.TimingBuilder()
          .setCategory("New Game")
          .setValue(System.currentTimeMillis() - timeStartMapCreation)
          .setVariable("Create Map Duration")
          .setLabel(size.toString)
          .build()
        )
        startActivity(myIntent)
        val activity: FragmentActivity = getActivity
        if (activity.isInstanceOf[NewGameActivity]) activity.finish()
      }
    }
  }

  def createGameState() {
    val mode: GameMode = mapGameMode( modeSpinner.getSelectedItemPosition )
    val size: GameSize = mapGameSize(mode)( sizeSpinner.getSelectedItemPosition )
    val difficulty: GameDifficulty = mapGameDifficulty( diffSpinner.getSelectedItemPosition )
    tracker.send(new HitBuilders.EventBuilder()
      .setCategory("New Game")
      .setAction("Create")
      .setLabel(""+mode+"; "+size+"; "+difficulty)
      .setValue(1)
      .build()
    )
    // create the game
    SaveState.save(new StateCreator(mode, size, difficulty).create, getActivity)
  }

  protected def showProgressDialog: ProgressDialog = {
    return ProgressDialog.show(getActivity, "", this.getString(R.string.loading), true)
  }

  /**
    * Loading fragment state
    */
  private def loadSettings() {
    val preferences = getActivity().getPreferences(Context.MODE_PRIVATE)
    val mode: Int = preferences.getInt(PREFERENCES_MODE, 0)
    modeSpinner.setSelection(mode)
    val size: Int = preferences.getInt(PREFERENCES_MAP_SIZE, 0)
    sizeSpinner.setSelection(size)
    val difficulty: Int = preferences.getInt(PREFERENCES_DIFFICULTY, 1)
    diffSpinner.setSelection(difficulty)
  }

  /**
    * Storing fragment state
    */
  private def saveSettings() {
    val editor: SharedPreferences.Editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit
    val size: Int = sizeSpinner.getSelectedItemPosition
    editor.putInt(PREFERENCES_MAP_SIZE, size).commit
    val mode: Int = modeSpinner.getSelectedItemPosition
    editor.putInt(PREFERENCES_MODE, mode).commit
    val diff: Int = diffSpinner.getSelectedItemPosition
    editor.putInt(PREFERENCES_DIFFICULTY, diff).commit
  }

  private class MyOnItemSelectedListener extends AdapterView.OnItemSelectedListener {
    def onItemSelected(parent: AdapterView[_], view: View, pos: Int, id: Long) {
      mapGameMode( modeSpinner.getSelectedItemPosition ) match {
        case GameMode.Adventure =>
          reduceSizeSpinner()
          modeDescription.setText(R.string.mode_adventure_exp)
        case GameMode.Rescue =>
          reduceSizeSpinner()
          modeDescription.setText(R.string.mode_rescue_exp)
        case GameMode.Classic =>
          extendSizeSpinner()
          modeDescription.setText(R.string.mode_classic_exp)

      }
    }

    override def onNothingSelected(adapterView: AdapterView[_]): Unit = {}
  }

  def extendSizeSpinner(): Unit = {
    val sizes = getResources().getStringArray(R.array.map_sizes).toVector.toArray
    val spinnerAdapter = new ArrayAdapter[String](this.getContext, android.R.layout.simple_spinner_item, sizes)
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    sizeSpinner.setAdapter(spinnerAdapter)
    spinnerAdapter.notifyDataSetChanged()
  }

  def reduceSizeSpinner(): Unit = {
    val sizes = getResources().getStringArray(R.array.map_sizes).toVector.drop(2).toArray
    val spinnerAdapter = new ArrayAdapter[String](this.getContext, android.R.layout.simple_spinner_item, sizes)
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    sizeSpinner.setAdapter(spinnerAdapter)
    spinnerAdapter.notifyDataSetChanged()
  }




}

object NewGameFragment {
  var autoStart: Boolean = false
}
