package com.bunamiranda.meuslivros.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bunamiranda.meuslivros.databinding.ItemLivroBinding
import com.bunamiranda.meuslivros.model.Livro
import com.google.firebase.firestore.FirebaseFirestore

class AdapterLivros(
    private val context: Context,
    private val listaLivros: MutableList<Livro>,
    private val onItemClick: (Livro) -> Unit
) : RecyclerView.Adapter<AdapterLivros.LivroViewHolder>() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val binding = ItemLivroBinding.inflate(LayoutInflater.from(context), parent, false)
        return LivroViewHolder(binding)
    }

    override fun getItemCount() = listaLivros.size

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = listaLivros[position]
        holder.bind(livro, onItemClick)
    }

    inner class LivroViewHolder(private val binding: ItemLivroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(livro: Livro, onItemClick: (Livro) -> Unit) {
            binding.txtTituloLivro.text = livro.titulo
            binding.txtAutor.text = livro.autores
            binding.root.setOnClickListener {
                onItemClick(livro)
            }
            binding.btnDelete.setOnClickListener {
                deleteLivro(livro)
            }
        }

        private fun deleteLivro(livro: Livro) {
            firestore.collection("livros").document(livro.id)
                .delete()
                .addOnSuccessListener {
                    listaLivros.remove(livro)
                    notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    // Tratar falha na remoção
                }
        }
    }
}