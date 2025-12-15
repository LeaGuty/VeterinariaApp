package com.miempresa.veterinaria.util

import java.util.regex.Pattern

object Validaciones {

    fun esCorreoValido(correo: String): Boolean {
        val patron = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return patron.matcher(correo).matches()
    }

    fun esTelefonoValido(telefono: String): Boolean {
        return telefono.length >= 8 && telefono.all { it.isDigit() || it == '+' || it == ' ' }
    }

    fun esTextoValido(texto: String): Boolean {
        return texto.isNotBlank()
    }

    // Algoritmo de validación de RUT Chileno (Módulo 11)
    fun esRutValido(rut: String): Boolean {
        // Limpiamos puntos y guión
        val rutLimpio = rut.replace(".", "").replace("-", "").uppercase()

        // Validamos largo mínimo y formato básico
        if (rutLimpio.length < 2 || !rutLimpio.all { it.isDigit() || it == 'K' }) return false

        try {
            val cuerpo = rutLimpio.substring(0, rutLimpio.length - 1)
            val dv = rutLimpio.last()

            var suma = 0
            var multiplicador = 2

            // Recorremos el cuerpo de atrás hacia adelante
            for (i in cuerpo.length - 1 downTo 0) {
                suma += cuerpo[i].toString().toInt() * multiplicador
                multiplicador++
                if (multiplicador == 8) multiplicador = 2
            }

            val resto = 11 - (suma % 11)
            val dvCalculado = when (resto) {
                11 -> '0'
                10 -> 'K'
                else -> resto.toString()[0]
            }

            return dv == dvCalculado
        } catch (e: Exception) {
            return false
        }
    }
}