// Archivo: com/miempresa/veterinaria/data/remote/RandomUserResponse.kt
package com.miempresa.veterinaria.data.remote

// Esta es la estructura que devuelve https://randomuser.me/api/
data class RandomUserResponse(
    val results: List<UserResult>
)

data class UserResult(
    val picture: UserPicture
)

data class UserPicture(
    val large: String,  // URL de la imagen en alta resolución
    val medium: String, // URL de la imagen media
    val thumbnail: String // URL de la miniatura
)