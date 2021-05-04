package com.verifica.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.gunar.uta.data.PairData
import com.gunar.uta.data.Static
import com.verifica.R
import com.verifica.model.firestore.SolicitudFire
import com.verifica.model.local.Usuario
import kotlinx.android.synthetic.main.activity_confirmation_details.*
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class ConfirmationDetailsActivity : AppCompatActivity() {

    private var usuario: Usuario = Usuario()
    private var code: String = ""

    private var solicitud: PairData<SolicitudFire>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation_details)

        val extras = intent.extras
        if (extras != null) {
            solicitud = extras.getSerializable("pairDataSoli") as PairData<SolicitudFire>
        }

        loadData()

        loadView()
    }

    private fun loadData(){
        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key), MODE_PRIVATE
        )

        code = sharedPref.getString(Static.CODE, "")+""


        usuario.nombres = tryDecrypt(
            stringToByteArray(sharedPref.getString(Static.NAMES, "")!!),
            stringToByteArray(sharedPref.getString(Static.NAMES + "IV", "")!!)
        )

        usuario.apellidos = tryDecrypt(
            stringToByteArray(sharedPref.getString(Static.LAST_NAMES, "")!!),
            stringToByteArray(sharedPref.getString(Static.LAST_NAMES + "IV", "")!!)
        )

        usuario.telCelular = tryDecrypt(
            stringToByteArray(sharedPref.getString(Static.PHONE, "")!!),
            stringToByteArray(sharedPref.getString(Static.PHONE + "IV", "")!!)
        )

        usuario.foto = tryDecrypt(
            stringToByteArray(sharedPref.getString(Static.PHOTO, "")!!),
            stringToByteArray(sharedPref.getString(Static.PHOTO + "IV", "")!!)
        )
    }

    private fun loadView(){
        tv_name_service.text = solicitud!!.data!!.servicio!!.nombre
        tv_messagge_service.text = solicitud!!.data!!.descripcion

        val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Bolivia"))
        cal.setTime(solicitud!!.data!!.fecha_solicitud)
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)

        val date = "$day/$month/$year"
        tv_date_service.text = date

        Glide.with(applicationContext)
            .load(solicitud!!.data!!.servicio!!.logo)
            .into(iv_photo_service)
    }

    private fun stringToByteArray(value: String): ByteArray{
        val split: Array<String> = value.substring(1, value.length - 1).split(", ").toTypedArray()
        val array = ByteArray(split.size)
        for (i in split.indices) {
            array[i] = split[i].toByte()
        }
        return array
    }

    private fun tryDecrypt(ciphertext: ByteArray, iv: ByteArray): String{
        try {
            val keyStore = KeyStore.getInstance(Static.KEYSTORE_TYPE)
            keyStore.load(null)
            val secretKey = keyStore.getKey(Static.KEY_ALIAS, null)

            val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")

            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val plaintext = cipher.doFinal(ciphertext)

            Log.w("TAG", "plaintext : " + String(plaintext))
            return String(plaintext)
        }
        catch (e: Exception) {
            Log.w("TAG", "error" + e.message)
            throw RuntimeException(e)
        }
        return ""
    }
}