package com.example.clubaereoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.clubaereoapp.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao
{
    //registrar un nuevo usuario en la base de datos sqlite
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UsuarioEntity)

    //buscar un usuario por correo
    @Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UsuarioEntity?
}