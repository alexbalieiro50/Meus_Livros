package com.bunamiranda.meuslivros

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bunamiranda.meuslivros.adapter.AdapterLivros
import com.bunamiranda.meuslivros.databinding.ActivityMainBinding
import com.bunamiranda.meuslivros.model.Livro
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapterLivros: AdapterLivros
    private val listaLivros: MutableList<Livro> = mutableListOf()
    private lateinit var firestore: FirebaseFirestore
    private var livrosListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerViewLivros = binding.recyclerViewLivros
        recyclerViewLivros.layoutManager = GridLayoutManager(this, 2)
        recyclerViewLivros.setHasFixedSize(true)
        adapterLivros = AdapterLivros(this, listaLivros) { livro ->
            val intent = Intent(this, TelaLIvroInformation::class.java).apply {
                putExtra("livro", livro)
            }
            startActivity(intent)
        }
        recyclerViewLivros.adapter = adapterLivros

        startLivrosListener()

        // Adiciona a funcionalidade ao botão flutuante
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, TelaAddLivro::class.java)
            startActivity(intent)
        }
    }

    private fun startLivrosListener() {
        livrosListener = firestore.collection("livros")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MainActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                listaLivros.clear()
                for (document in snapshots!!) {
                    val livro = document.toObject(Livro::class.java).apply { id = document.id }
                    listaLivros.add(livro)
                }
                adapterLivros.notifyDataSetChanged()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        livrosListener?.remove()
    }

    companion object {
        private const val ADD_BOOK_REQUEST_CODE = 1
    }
}
