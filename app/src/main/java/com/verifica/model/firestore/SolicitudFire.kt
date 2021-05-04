package com.verifica.model.firestore

import java.io.Serializable
import java.util.*

class SolicitudFire(var descripcion: String? = null, var fecha_solicitud: Date? =  null, var codigo_usuario: String? = null, var estado: String? = null, var tipo:  String? = null, var servicio: ServicioFire? = null ): Serializable {
}