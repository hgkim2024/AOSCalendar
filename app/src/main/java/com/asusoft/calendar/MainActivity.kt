package com.asusoft.calendar

import android.os.Bundle
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: - 실제 기기에서 적용되는지 테스트해보기
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        setContentView(R.layout.activity_main)

        val layout = findViewById<ConstraintLayout>(R.id.root_layout)
        val btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener {
//            val layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//            )
//
//            layout.layoutParams = layoutParams
            val ani = ScaleAnimation(
                    0.0F,
                    2.0F,
                    0F,
                    2.0F
            )
            ani.duration = 4000
            layout.startAnimation(ani)
        }
    }
}