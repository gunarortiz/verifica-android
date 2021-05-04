package com.verifica.model.local

import java.io.Serializable

class Solicitud(var servicio: Servicio, var descripcion: String, var fechaSolicitud: String, var tipo: String): Serializable {
}