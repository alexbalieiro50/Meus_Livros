package com.bunamiranda.meuslivros.model

import android.os.Parcel
import android.os.Parcelable


data class Livro(
    var id: String = "",
    var titulo: String = "",
    var autores: String = "",
    var anoPublicacao: String = "",
    var numPaginas: String = "",
    var editora: String = "",
    var isbn: String = "",
    var descricao: String = "",
    var imagemUrl: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(titulo)
        parcel.writeString(autores)
        parcel.writeString(anoPublicacao)
        parcel.writeString(numPaginas)
        parcel.writeString(editora)
        parcel.writeString(isbn)
        parcel.writeString(descricao)
        parcel.writeString(imagemUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Livro> {
        override fun createFromParcel(parcel: Parcel): Livro {
            return Livro(parcel)
        }

        override fun newArray(size: Int): Array<Livro?> {
            return arrayOfNulls(size)
        }
    }
}