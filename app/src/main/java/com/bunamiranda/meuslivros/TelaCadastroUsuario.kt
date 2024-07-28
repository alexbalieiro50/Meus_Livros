package com.bunamiranda.meuslivros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaCadastroUsuarioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TelaCadastroUsuario : AppCompatActivity() {

    private lateinit var binding: ActivityTelaCadastroUsuarioBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTelaCadastroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCriarUsuario.setOnClickListener {
            val nome = binding.editNomeUsuario.text.toString().trim()
            val email = binding.editEmailUsuario.text.toString().trim()
            val telefone = binding.editTelefoneUsuario.text.toString().trim()
            val senha = binding.editSenhaUsuario.text.toString().trim()
            val confirmarSenha = binding.editConfSenhaUsuario.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || telefone.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (senha != confirmarSenha) {
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            criarUsuario(nome, email, telefone, senha)
        }
    }

    private fun criarUsuario(nome: String, email: String, telefone: String, senha: String) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    val userMap = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "telefone" to telefone
                    )

                    if (userId != null) {
                        firestore.collection("usuarios").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Cadastro realizado com sucesso.", Toast.LENGTH_SHORT).show()
                                // Navegar para a tela inicial após o cadastro
                                val intent = Intent(this, TelaInicial::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()  // Finalizar a atividade atual para removê-la da pilha
                            }
                            .addOnFailureListener { e ->
                                Log.w("TelaCadastroUsuario", "Erro ao salvar usuário no Firestore", e)
                                Toast.makeText(this, "Erro ao salvar dados do usuário.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Log.w("TelaCadastroUsuario", "Erro ao criar usuário", task.exception)
                    Toast.makeText(this, "Erro ao criar usuário.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
