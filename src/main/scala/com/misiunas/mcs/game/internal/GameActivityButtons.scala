package com.misiunas.mcs.game.internal

import android.content.{Context, Intent}
import android.graphics.PorterDuff
import android.os.{Build, Bundle, Vibrator}
import android.preference.PreferenceManager
import android.view.{Menu, MenuInflater, MenuItem, View}
import android.widget.{Button, ImageButton, Toast}
import com.misiunas.mcs._
import com.misiunas.mcs.game.{GameActivity, Tool}
import com.misiunas.mcs.minor.ShareOnSocialMedia

abstract class GameActivityButtons extends com.misiunas.mcs.game.internal.GameActivityServices {

  protected var tool: Tool = Tool.Protect
  protected var mineButton: Button = null
  protected var flagButton: Button = null
  protected var exploreButton: Button = null
  private var buttonSelectedColor: Int = 0
  private var buttonUnselectedColor: Int = 0
  private var vibrator: Vibrator = null

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    vibrator = getSystemService(Context.VIBRATOR_SERVICE).asInstanceOf[Vibrator]
    mineButton = findViewById(R.id.button_mine).asInstanceOf[Button]
    mineButton.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        tool = Tool.Mine
        updateButtonsGraphics
      }
    })
    flagButton = findViewById(R.id.button_flag).asInstanceOf[Button]
    flagButton.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        tool = Tool.Flag
        updateButtonsGraphics
        considerShowingAd()
      }
    })
    exploreButton = findViewById(R.id.button_explore).asInstanceOf[Button]
    exploreButton.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        tool = Tool.Protect
        updateButtonsGraphics
        considerShowingAd()
      }
    })
    buttonSelectedColor = getResources.getColor(R.color.button_selected)
    buttonUnselectedColor = getResources.getColor(R.color.button_unselected)
    val b_share: ImageButton = findViewById(R.id.button_share).asInstanceOf[ImageButton]
    val context = this
    b_share.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        ShareOnSocialMedia.share(context, operator)
      }
    })
    val b_back: ImageButton = findViewById(R.id.button_back).asInstanceOf[ImageButton]
    b_back.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        finish()
      }
    })
    val b_restart: ImageButton = findViewById(R.id.button_restart).asInstanceOf[ImageButton]
    b_restart.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        NewGameFragment.autoStart = true
        startActivity(new Intent(view.getContext , classOf[NewGameActivity]))
        finish()
      }
    })
    updateButtonsGraphics
  }

  def updateButtonsGraphics(): Unit = {
    val activity = this
    activity.runOnUiThread(new Runnable() {
      def run() = updateButtonsGraphicsInternal()
    })
  }

  private def updateButtonsGraphicsInternal(): Unit = {
    mineButton.setEnabled(tool ne Tool.Mine)
    if (tool eq Tool.Mine) mineButton.getBackground.setColorFilter(buttonSelectedColor, PorterDuff.Mode.MULTIPLY)
    else mineButton.getBackground.setColorFilter(buttonUnselectedColor, PorterDuff.Mode.MULTIPLY)
    flagButton.setEnabled(tool ne Tool.Flag)
    if (tool eq Tool.Flag) flagButton.getBackground.setColorFilter(buttonSelectedColor, PorterDuff.Mode.MULTIPLY)
    else flagButton.getBackground.setColorFilter(buttonUnselectedColor, PorterDuff.Mode.MULTIPLY)
    exploreButton.setEnabled(tool ne Tool.Protect)
    if (tool eq Tool.Protect) exploreButton.getBackground.setColorFilter(buttonSelectedColor, PorterDuff.Mode.MULTIPLY)
    else exploreButton.getBackground.setColorFilter(buttonUnselectedColor, PorterDuff.Mode.MULTIPLY)
    if (operator.hasExplosives) {
      mineButton.setCompoundDrawablesWithIntrinsicBounds(tntDrawable, null, null, null)
    }
    else if (operator.hasSword) {
      mineButton.setCompoundDrawablesWithIntrinsicBounds(swordDrawable, null, null, null)
    }
    else {
      mineButton.setCompoundDrawablesWithIntrinsicBounds(axeDrawable, null, null, null)
    }
  }

  protected def vibrate {
    if (Settings.vibrate(this)) {
      try {
        vibrator.vibrate( Settings.vibrateDuration(this) )
      } catch {
        case e: Exception => { }
      }
    }
  }

  protected def vibrateLong {
    if (Settings.vibrate(this)) {
      try {
        vibrator.vibrate( 6*Settings.vibrateDuration(this) )
      } catch {
        case e: Exception => { }
      }
    }
  }

}