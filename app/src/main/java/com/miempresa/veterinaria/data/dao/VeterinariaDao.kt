package com.miempresa.veterinaria.data.dao

import androidx.room.*
import com.miempresa.veterinaria.data.entity.*
import com.miempresa.veterinaria.data.relation.ConsultaConDetalles
import com.miempresa.veterinaria.data.relation.MascotaConCliente
import kotlinx.coroutines.flow.Flow

@Dao
interface VeterinariaDao {
    // --- Clientes ---
    @Query("SELECT * FROM clientes")
    fun obtenerClientes(): Flow<List<ClienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCliente(cliente: ClienteEntity)

    @Delete
    suspend fun eliminarCliente(cliente: ClienteEntity)

    // --- Mascotas ---
    @Transaction
    @Query("SELECT * FROM mascotas")
    fun obtenerMascotasConCliente(): Flow<List<MascotaConCliente>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMascota(mascota: MascotaEntity)

    @Delete
    suspend fun eliminarMascota(mascota: MascotaEntity)

    // --- Consultas ---
    @Transaction
    @Query("SELECT * FROM consultas")
    fun obtenerConsultasConDetalles(): Flow<List<ConsultaConDetalles>>

    @Query("SELECT * FROM consultas")
    fun obtenerConsultas(): Flow<List<ConsultaEntity>>

    @Insert
    suspend fun insertarConsulta(consulta: ConsultaEntity)

    @Delete
    suspend fun eliminarConsulta(consulta: ConsultaEntity)
}
