package com.verifica.model.local

import java.io.Serializable
import java.util.*

class Respuesta(var certificado: Certificado, concedido: Boolean, var fecha: Date, var idSolicitud: String): Serializable {
}