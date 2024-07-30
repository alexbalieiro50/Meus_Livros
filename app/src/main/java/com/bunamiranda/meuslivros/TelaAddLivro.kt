package com.bunamiranda.meuslivros

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bunamiranda.meuslivros.databinding.ActivityTelaAddLivroBinding
import com.bunamiranda.meuslivros.model.Livro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

class TelaAddLivro : AppCompatActivity() {

    private lateinit var binding: ActivityTelaAddLivroBinding
    private var imageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTelaAddLivroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSelecionarImagem.setOnClickListener {
            selecionarImagem()
        }

        binding.btnSalvarLivro.setOnClickListener {
            salvarLivro()
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
                val resizedBitmap = getResizedBitmap(it, contentResolver, 1000, 1000) // Redimensiona a imagem para 1000x1000
                resizedBitmap?.let { bitmap ->
                    binding.imgLivro.setImageBitmap(bitmap)
                } ?: run {
                    Toast.makeText(this, "Falha ao redimensionar a imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun salvarLivro() {
        val titulo = binding.editTitulo.text.toString().trim()
        val anoPublicacao = binding.editAnoPublicacao.text.toString().trim()
        val numPaginas = binding.editNumPaginas.text.toString().trim()
        val editora = binding.editEditora.text.toString().trim()
        val autores = binding.editAutores.text.toString().trim()
        val isbn = binding.editIsbn.text.toString().trim()
        val descricao = binding.editDescricao.text.toString().trim()

        if (titulo.isEmpty() || anoPublicacao.isEmpty() || numPaginas.isEmpty() || editora.isEmpty() || autores.isEmpty() || isbn.isEmpty() || descricao.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Por favor, preencha todos os campos e selecione uma imagem.", Toast.LENGTH_SHORT).show()
            return
        }

        // Salvar a imagem no Firebase Storage
        val imageRef = storage.reference.child("livros/${UUID.randomUUID()}.jpg")
        val bitmap = binding.imgLivro.drawable.toBitmap()
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                // Salvar os dados do livro no Firestore
                val livro = Livro(
                    id = "",
                    titulo = titulo,
                    anoPublicacao = anoPublicacao,
                    numPaginas = numPaginas,
                    editora = editora,
                    autores = autores,
                    isbn = isbn,
                    descricao = descricao,
                    imagemUrl = imageUrl
                )
                firestore.collection("livros")
                    .add(livro)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(this, "Livro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar livro.", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Erro ao fazer upload da imagem.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1
    }

    private fun getResizedBitmap(imageUri: Uri, contentResolver: ContentResolver, maxWidth: Int, maxHeight: Int): Bitmap? {
        var inputStream: InputStream? = null
        try {
            inputStream = contentResolver.openInputStream(imageUri)
            // Decode image size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate the scale
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
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return null
    }
}
