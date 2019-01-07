package com.ssl.generateviewmodel

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ssl.annotation.LiveData
import com.ssl.annotation.ViewModel

@LiveData
@ViewModel
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
