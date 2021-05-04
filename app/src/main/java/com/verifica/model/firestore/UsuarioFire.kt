package com.verifica.model.firestore

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class UsuarioFire(var email: String? = null, var contrasena: String? = null, var id_dispositivo: String? = null, var imei: String? = null, var sec_hardware: Boolean? = null) :
    Serializable {
}