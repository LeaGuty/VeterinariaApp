package com.miempresa.veterinaria.data.remote

/**
 * RandomUserResponse - Modelos de datos para la API RandomUser.me (Paso 6)
 *
 * Estas data classes representan la estructura JSON devuelta por la API
 * y son convertidas automáticamente por Gson.
 *
 * Ejemplo de respuesta JSON:
 * ```json
 * {
 *   "results": [{
 *     "picture": {
 *       "large": "https://randomuser.me/api/portraits/men/75.jpg",
 *       "medium": "https://randomuser.me/api/portraits/med/men/75.jpg",
 *       "thumbnail": "https://randomuser.me/api/portraits/thumb/men/75.jpg"
 *     }
 *   }]
 * }
 * ```
 *
 * @author Veterinaria App Team
 * @since 1.0
 */

/**
 * Respuesta raíz de la API RandomUser
 *
 * Contiene una lista de resultados, cada uno con información de un usuario aleatorio
 *
 * @property results Lista de usuarios devueltos (generalmente 1 por defecto)
 */
data class RandomUserResponse(
    val results: List<UserResult>
)

/**
 * Resultado individual de usuario
 *
 * Representa los datos de un usuario aleatorio generado por la API.
 * Solo incluimos el campo 'picture' ya que es lo único que necesitamos.
 *
 * @property picture Objeto con las URLs de las imágenes del usuario
 */
data class UserResult(
    val picture: UserPicture
)

/**
 * URLs de las imágenes del usuario
 *
 * La API proporciona tres tamaños de imagen:
 * - large: 128x128 px - Para perfiles y vistas detalladas
 * - medium: 64x64 px - Para listas y tarjetas
 * - thumbnail: 48x48 px - Para miniaturas
 *
 * @property large URL de imagen en alta resolución (128x128 px)
 * @property medium URL de imagen en resolución media (64x64 px)
 * @property thumbnail URL de miniatura (48x48 px)
 */
data class UserPicture(
    val large: String,
    val medium: String,
    val thumbnail: String
)
