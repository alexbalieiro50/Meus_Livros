package com.bunamiranda.meuslivros

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaInicialBinding

class TelaInicial : AppCompatActivity() {

    private lateinit var binding: ActivityTelaInicialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTelaInicialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar o clique no botão "Entrar"
        binding.btnEntrar.setOnClickListener {
            val intent = Intent(this,TelaLogin::class.java)
            startActivity(intent)
        }

        // Configurar o clique no botão "Criar uma conta"
        binding.btnCriarConta.setOnClickListener {
            val intent = Intent(this, TelaCadastroUsuario::class.java)
            startActivity(intent)
        }

    }
}