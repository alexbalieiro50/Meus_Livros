package com.bunamiranda.meuslivros

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bunamiranda.meuslivros.databinding.ActivityTelaEditarLivroBinding
import com.bunamiranda.meuslivros.model.Livro
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

class TelaEditarLivro : AppCompatActivity() {

    private lateinit var binding: ActivityTelaEditarLivroBinding
    private var imageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var livro: Livro? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTelaEditarLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        livro = intent.getParcelableExtra("livro")
        livro?.let {
            updateUI(it)
        }

        binding.btnSelecionarNovaImagem.setOnClickListener {
            selecionarImagem()
        }

        binding.btnAtualizarLivro.setOnClickListener {
            atualizarLivro()
        }
    }

    private fun selecionarImagem() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                val resizedBitmap = getResizedBitmap(it, contentResolver, 1000, 1000)
                resizedBitmap?.let { bitmap ->
                    binding.imgLivroAtualizar.setImageBitmap(bitmap)
                    Log.d(TAG, "Imagem selecionada e carregada na ImageView")
                } ?: run {
                    Log.e(TAG, "Falha ao redimensionar a imagem")
                    Toast.makeText(this, "Falha ao redimensionar a imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun atualizarLivro() {
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

        binding.progressBar.isVisible = true

        livro?.let {
            val livroAtualizado = Livro(
                id = it.id,
                titulo = titulo,
                anoPublicacao = anoPublicacao,
                numPaginas = numPaginas,
                editora = editora,
                autores = autores,
                isbn = isbn,
                descricao = descricao,
                imagemUrl = it.imagemUrl
            )

            if (imageUri != null) {
                Log.d(TAG, "Nova imagem selecionada, fazendo upload")
                val imageRef = storage.reference.child("livros/${UUID.randomUUID()}.jpg")
                val drawable = binding.imgLivroAtualizar.drawable

                if (drawable != null) {
                    Log.d(TAG, "Imagem válida, iniciando processo de compressão")
                    val bitmap = drawable.toBitmap()
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    val uploadTask = imageRef.putBytes(data)
                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Log.d(TAG, "Upload da imagem bem-sucedido, URL: $uri")
                            livroAtualizado.imagemUrl = uri.toString()
                            salvarLivroAtualizado(livroAtualizado)
                        }.addOnFailureListener { e ->
                            binding.progressBar.isVisible = false
                            Log.e(TAG, "Falha ao obter URL de download", e)
                            Toast.makeText(this, "Erro ao obter URL de download da imagem.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        binding.progressBar.isVisible = false
                        Log.e(TAG, "Falha ao fazer upload da imagem", it)
                        Toast.makeText(this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    binding.progressBar.isVisible = false
                    Log.e(TAG, "Drawable é nulo, imagem inválida")
                    Toast.makeText(this, "Imagem inválida.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d(TAG, "Nenhuma nova imagem selecionada, apenas atualizando dados do livro")
                salvarLivroAtualizado(livroAtualizado)
            }
        }
    }

    private fun salvarLivroAtualizado(livro: Livro) {
        firestore.collection("livros").document(livro.id)
            .set(livro)
            .addOnSuccessListener {
                binding.progressBar.isVisible = false
                Log.d(TAG, "Livro atualizado com sucesso")
                Toast.makeText(this, "Livro atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.isVisible = false
                Log.e(TAG, "Erro ao atualizar livro no Firestore", e)
                Toast.makeText(this, "Erro ao atualizar livro.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(livro: Livro) {
        binding.editAtualizarTitulo.setText(livro.titulo)
        binding.editAtualizarAnoPublicacao.setText(livro.anoPublicacao)
        binding.editAtualizarNumPaginas.setText(livro.numPaginas)
        binding.editAtualizarEditora.setText(livro.editora)
        binding.editAtualizarAutores.setText(livro.autores)
        binding.editAtualizarIsbn.setText(livro.isbn)
        binding.editAtualizarDescricao.setText(livro.descricao)

        Glide.with(this)
            .load(livro.imagemUrl)
            .into(binding.imgLivroAtualizar)
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1
        private const val TAG = "TelaEditarLivro"
    }

    private fun getResizedBitmap(imageUri: Uri, contentResolver: ContentResolver, maxWidth: Int, maxHeight: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var scale = 1
            while (options.outWidth / scale >= maxWidth || options.outHeight / scale >= maxHeight) {
                scale *= 2
            }

            val options2 = BitmapFactory.Options()
            options2.inSampleSize = scale
            inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options2)
            inputStream?.close()

            return bitmap
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Arquivo não encontrado", e)
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }
}
