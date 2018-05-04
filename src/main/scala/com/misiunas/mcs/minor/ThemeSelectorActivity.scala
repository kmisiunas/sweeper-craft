package com.misiunas.mcs.minor

import android.app.Activity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.View.OnClickListener
import android.widget.TableRow
import com.misiunas.mcs.{R, Settings}

/**
  * Created by kmisiunas on 2016-07-30.
  */
class ThemeSelectorActivity extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    this.setContentView(R.layout.theme)

    // check if have premium version and activate buttons accordingly


    // Perform theme changing

    val context = this

    val row_minecraft = findViewById( R.id.theme_select_minecraft ).asInstanceOf[TableRow]
    row_minecraft.setOnClickListener( new OnClickListener() {
      override def onClick( view: View ): Unit = {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString("mcs.theme", "texture" )
        editor.commit()
        context.finish()
      }
    } )


    val row_classic = findViewById( R.id.theme_select_classic ).asInstanceOf[TableRow]
    row_classic.setOnClickListener( new OnClickListener() {
      override def onClick( view: View ): Unit = {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString("mcs.theme", "texture_classic" )
        editor.commit()
        context.finish()
      }
    } )

    /** Handles back button */
//    val b_back: Button = findViewById(R.id.button_reset_calibration).asInstanceOf[Button]
//    b_back.setOnClickListener(new View.OnClickListener() {
//      def onClick(view: View) {
//        finish()
//      }
//    })
  }

}
