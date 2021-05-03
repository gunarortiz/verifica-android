package com.verifica.model.local

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Usuario(var nombres: String? = null, var apellidos: String? = null, var fechaNac: Date? = null, var telCelular: String? = null, var foto: String? = null, var codigo: String? = null, var auth: Auth? = null) :
    Serializable {
}