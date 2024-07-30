package com.bunamiranda.meuslivros

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaSplashBinding

class TelaSplash : AppCompatActivity() {

    private lateinit var binding: ActivityTelaSplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTelaSplashBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent =  Intent(this, TelaInicial::class.java)
            startActivity(intent)
            finish() // Finaliza a SplashActivity para que o usuário não possa voltar para ela
        }, 3000)
    }


}