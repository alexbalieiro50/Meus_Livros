package com.bunamiranda.meuslivros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaLogin : AppCompatActivity() {

    private lateinit var binding: ActivityTelaLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTelaLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.btnEntrarLogin.setOnClickListener {
            val email = binding.editEmailLogin.text.toString().trim()
            val senha = binding.editSenhaLogin.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUsuario(email, senha)
        }

        binding.txtEsqueciSenha.setOnClickListener {
            // Implementar a funcionalidade de recuperação de senha aqui
        }
    }

    private fun loginUsuario(email: String, senha: String) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        firestore.collection("usuarios").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Usuário existe no Firestore, navegar para a TelaUsuario
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // Usuário não encontrado no Firestore
                                    Toast.makeText(this, "Usuário não encontrado.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("TelaLogin", "Erro ao verificar usuário no Firestore", e)
                                Toast.makeText(this, "Erro ao verificar dados do usuário.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Log.w("TelaLogin", "Erro ao fazer login", task.exception)
                    Toast.makeText(this, "Erro ao fazer login. Verifique suas credenciais.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}