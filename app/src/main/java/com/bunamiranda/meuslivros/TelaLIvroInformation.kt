package com.bunamiranda.meuslivros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bunamiranda.meuslivros.databinding.ActivityTelaLivroInformationBinding
import com.bunamiranda.meuslivros.model.Livro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class TelaLIvroInformation : AppCompatActivity() {
    private lateinit var binding: ActivityTelaLivroInformationBinding
    private lateinit var firestore: FirebaseFirestore
    private var livro: Livro? = null
    private var livroListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTelaLivroInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        livro = intent.getParcelableExtra("livro")
        if (livro != null) {
            updateUI(livro!!)

            // Adiciona o listener de snapshot para ouvir mudanças em tempo real
            livroListener = firestore.collection("livros").document(livro!!.id)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        Log.w("TelaInforLivros", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        livro = documentSnapshot.toObject(Livro::class.java)
                        livro?.let {
                            it.id = documentSnapshot.id
                            updateUI(it)
                        }
                    }
                }
        }

        binding.btnEditar.setOnClickListener {
            livro?.let {
                val intent = Intent(this, TelaEditarLivro::class.java).apply {
                    putExtra("livro", it)
                }
                startActivity(intent)
            }
        }

        binding.btnExcluir.setOnClickListener {
            livro?.let { deleteLivro(it) }
        }
    }

    private fun updateUI(livro: Livro) {
        binding.txtTituloLivroInfor.text = livro.titulo
        binding.txtAutorInfor.text = livro.autores
        binding.txtDescricao.text = livro.descricao

        // Carregar a imagem usando Glide
        Glide.with(this)
            .load(livro.imagemUrl)
            .into(binding.imgLivroInfor)
    }

    private fun deleteLivro(livro: Livro) {
        // Verificar se a URL da imagem não está vazia ou nula
        if (!livro.imagemUrl.isNullOrEmpty()) {
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(livro.imagemUrl)

            storageReference.delete().addOnSuccessListener {
                // Imagem excluída com sucesso, agora excluir o documento do Firestore
                firestore.collection("livros").document(livro.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Livro excluído com sucesso.", Toast.LENGTH_SHORT).show()
                        finish() // Finaliza a atividade atual e volta para a anterior
                    }
                    .addOnFailureListener { e ->
                        Log.w("TelaInforLivros", "Erro ao excluir livro", e)
                        Toast.makeText(this, "Erro ao excluir livro.", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Log.w("TelaInforLivros", "Erro ao excluir imagem", e)
                Toast.makeText(this, "Erro ao excluir imagem.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Se não houver imagem, apenas excluir o documento do Firestore
            firestore.collection("livros").document(livro.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Livro excluído com sucesso.", Toast.LENGTH_SHORT).show()
                    finish() // Finaliza a atividade atual e volta para a anterior
                }
                .addOnFailureListener { e ->
                    Log.w("TelaInforLivros", "Erro ao excluir livro", e)
                    Toast.makeText(this, "Erro ao excluir livro.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        livroListener?.remove()
    }
}