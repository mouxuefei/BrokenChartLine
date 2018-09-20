package com.exmple.brokenchartline

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lineChart.setStartMonth(5)
        lineChart.setValueY(arrayListOf(30,60,90))

        btn.setOnClickListener {
            lineChart.setPointValues(arrayListOf(22,30,13,34,18,8),arrayListOf(34,39,20,25,12,27))
        }
    }
}
