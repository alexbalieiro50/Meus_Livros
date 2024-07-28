package com.bunamiranda.meuslivros

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaEditarLivroBinding
import com.bunamiranda.meuslivros.model.Livro
import com.google.firebase.firestore.FirebaseFirestore

class TelaEditarLivro : AppCompatActivity() {

    private lateinit var binding: ActivityTelaEditarLivroBinding
    private lateinit var firestore: FirebaseFirestore
    private var livroId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTelaEditarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val livro = intent.getParcelableExtra<Livro>("livro")
        livro?.let {
            carregarDados(it)
            livroId = it.id
        }

        binding.btnAtualizarLivro.setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun carregarDados(livro: Livro) {
        binding.editAtualizarTitulo.setText(livro.titulo)
        binding.editAtualizarAnoPublicacao.setText(livro.anoPublicacao)
        binding.editAtualizarNumPaginas.setText(livro.numPaginas)
        binding.editAtualizarEditora.setText(livro.editora)
        binding.editAtualizarAutores.setText(livro.autores)
        binding.editAtualizarIsbn.setText(livro.isbn)
        binding.editAtualizarDescricao.setText(livro.descricao)
    }

    private fun salvarAlteracoes() {
        val titulo = binding.editAtualizarTitulo.text.toString().trim()
        val anoPublicacao = binding.editAtualizarAnoPublicacao.text.toString().trim()
        val numPaginas = binding.editAtualizarNumPaginas.text.toString().trim()
        val editora = binding.editAtualizarEditora.text.toString().trim()
        val autores = binding.editAtualizarAutores.text.toString().trim()
        val isbn = binding.editAtualizarIsbn.text.toString().trim()
        val descricao = binding.editAtualizarDescricao.text.toString().trim()

        if (titulo.isEmpty() || anoPublicacao.isEmpty() || numPaginas.isEmpty() || editora.isEmpty() || autores.isEmpty() || isbn.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        livroId?.let {
            val bookData = hashMapOf(
                "titulo" to titulo,
                "anoPublicacao" to anoPublicacao,
                "numPaginas" to numPaginas,
                "editora" to editora,
                "autores" to autores,
                "isbn" to isbn,
                "descricao" to descricao
            )

            firestore.collection("livros").document(it)
                .set(bookData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Livro atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar livro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}