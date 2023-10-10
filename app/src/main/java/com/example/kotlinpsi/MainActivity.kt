package com.example.kotlinpsi

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.kotlinpsi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()
        //Log.d(TAG, "Boringtest: "+Boringtest())
        CryptoMessage("test","teat")
    }

    /**
     * A native method that is implemented by the 'kotlinpsi' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun Boringtest(): Int

    //与えられた文字列を暗号化(楕円曲線鍵暗号)
    external fun CryptoMessage(message:String,message_cl:String): Int

    companion object {
        // Used to load the 'kotlinpsi' library on application startup.
        init {
            System.loadLibrary("kotlinpsi")
        }
    }
}