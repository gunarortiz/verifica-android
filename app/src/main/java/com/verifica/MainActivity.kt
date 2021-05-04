package com.verifica

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.gunar.uta.data.PairData
import com.gunar.uta.data.Static
import com.gunar.uta.data.Static.Companion.ACEPTADO
import com.gunar.uta.data.Static.Companion.CODE
import com.gunar.uta.data.Static.Companion.LAST_NAMES
import com.gunar.uta.data.Static.Companion.NAMES
import com.gunar.uta.data.Static.Companion.PENDIENTE
import com.gunar.uta.data.Static.Companion.PHONE
import com.gunar.uta.data.Static.Companion.PHOTO
import com.gunar.uta.data.Static.Companion.RECHAZADO
import com.verifica.adapter.SolicitudAdapter
import com.verifica.model.firestore.SolicitudFire
import com.verifica.model.local.Usuario
import com.verifica.ui.ConfirmationDetailsActivity
import com.verifica.ui.SuccessfulSignupActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class MainActivity : AppCompatActivity() {
    private var usuario: Usuario = Usuario()

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val list: ArrayList<PairData<SolicitudFire>> = ArrayList()
    private val adapter = SolicitudAdapter(list, this)

    var listener: ListenerRegistration? = null
    var viewNews: Boolean = true

    private var keletonScreen: RecyclerViewSkeletonScreen? = null

    private var code: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseFirestore.getInstance().clearPersistence();


        loadView()

        var rv = findViewById(R.id.rv) as RecyclerView
        rv.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        rv.adapter = adapter




        adapter.onItemClick = { pairData1 ->
            val intent = Intent(applicationContext, ConfirmationDetailsActivity::class.java)
            intent.putExtra("pairDataSoli", pairData1)
//            intent.putExtra("update", true)
//            Toast.makeText(applicationContext, car.placa, Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        loadData()

    }



    private fun loadData(){
        list.clear()
        empty_list.visibility = View.GONE

        keletonScreen = Skeleton.bind(rv)
            .adapter(adapter)
            .load(R.layout.item_skeleton_service)
            .show()


        var query: Query? = null

        if(viewNews){
            query = db.collection("solicitud").whereEqualTo("estado", PENDIENTE).whereEqualTo("codigo_usuario", code)
        }
        else{
            query = db.collection("solicitud").whereEqualTo("codigo_usuario", code)
        }


        listener = query!!.addSnapshotListener { value, e ->


            if (e != null) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()

//                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            list.clear()

            for (documento: QueryDocumentSnapshot in value!!) {
                val soli = documento.toObject(SolicitudFire::class.java)

                if(!viewNews){
                    if(soli.estado != PENDIENTE){
                        list.add(PairData<SolicitudFire>(documento.id, soli))
                    }
                }
                else{
                    list.add(PairData<SolicitudFire>(documento.id, soli))
                }

            }

            keletonScreen!!.hide()
            adapter.notifyDataSetChanged()

            if(list.size==0){
                empty_list.visibility = View.VISIBLE
            }
            else{
                empty_list.visibility = View.GONE
            }

        }
    }


    private fun loadView(){
        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key), MODE_PRIVATE
        )

        code = sharedPref.getString(CODE, "")+""


        usuario.nombres = tryDecrypt(
            stringToByteArray(sharedPref.getString(NAMES, "")!!),
            stringToByteArray(sharedPref.getString(NAMES + "IV", "")!!)
        )

        usuario.apellidos = tryDecrypt(
            stringToByteArray(sharedPref.getString(LAST_NAMES, "")!!),
            stringToByteArray(sharedPref.getString(LAST_NAMES + "IV", "")!!)
        )

        usuario.telCelular = tryDecrypt(
            stringToByteArray(sharedPref.getString(PHONE, "")!!),
            stringToByteArray(sharedPref.getString(PHONE + "IV", "")!!)
        )

        usuario.foto = tryDecrypt(
            stringToByteArray(sharedPref.getString(PHOTO, "")!!),
            stringToByteArray(sharedPref.getString(PHOTO + "IV", "")!!)
        )

        val imageByteArray: ByteArray = Base64.decode(usuario!!.foto, Base64.DEFAULT)
        Glide.with(this)
            .load(imageByteArray)
            .into(iv_photo)

        tv_name.text = "Hola "+usuario!!.nombres!!.split(" ")[0]+"!"
        tv_code.text = code

        iv_about.setOnClickListener {
            alert("Código", "Este codigo es unico por usuario y no cambia, se te solicitara la primera vez que ingreses a un servicio")
        }

        new_btn.setOnClickListener {
            if(!viewNews){
                listener!!.remove()
                viewNews = true
                loadData()
                new_btn.setTextColor(ContextCompat.getColor(this, R.color.orange))
                old_btn.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        old_btn.setOnClickListener {
            if(viewNews) {
                listener!!.remove()
                viewNews = false
                loadData()
                new_btn.setTextColor(ContextCompat.getColor(this, R.color.black))
                old_btn.setTextColor(ContextCompat.getColor(this, R.color.orange))
            }
        }

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

    fun alert(title: String, message: String) {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setPositiveButton("Entendido") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()

        return
    }
}