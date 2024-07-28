package com.bunamiranda.meuslivros

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaAddLivroBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class TelaAddLivro : AppCompatActivity() {

    private lateinit var binding: ActivityTelaAddLivroBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTelaAddLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        binding.btnSalvarLivro.setOnClickListener {
            saveBookData()
        }
    }

    private fun saveBookData() {
        val titulo = binding.editTitulo.text.toString().trim()
        val anoPublicacao = binding.editAnoPublicacao.text.toString().trim()
        val numPaginas = binding.editNumPaginas.text.toString().trim()
        val editora = binding.editEditora.text.toString().trim()
        val autores = binding.editAutores.text.toString().trim()
        val isbn = binding.editIsbn.text.toString().trim()
        val descricao = binding.editDescricao.text.toString().trim()

        if (titulo.isEmpty() || anoPublicacao.isEmpty() || numPaginas.isEmpty() || editora.isEmpty() || autores.isEmpty() || isbn.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val bookData = hashMapOf(
            "titulo" to titulo,
            "anoPublicacao" to anoPublicacao,
            "numPaginas" to numPaginas,
            "editora" to editora,
            "autores" to autores,
            "isbn" to isbn,
            "descricao" to descricao
        )

        saveBookDataToFirestore(bookData)
    }

    private fun saveBookDataToFirestore(bookData: HashMap<String, String>) {
        firestore.collection("livros")
            .add(bookData)
            .addOnSuccessListener {
                Toast.makeText(this, "Livro salvo com sucesso.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar livro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}