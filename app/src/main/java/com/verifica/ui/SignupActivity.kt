package com.verifica.ui


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyInfo
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.verifica.R
import com.verifica.model.firestore.UsuarioFire
import com.verifica.model.local.Auth
import com.verifica.model.local.Usuario
import com.verifica.setup.FileCompressor
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.*
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.cert.CertificateException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.security.auth.x500.X500Principal


class SignupActivity : AppCompatActivity() {
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_GALLERY_PHOTO = 2
    var mPhotoFile: File? = null
    var mCompressor: FileCompressor? = null

    private val AUTHENTICATION_DURATION_SECONDS = 30

    var code = ""

    var usuario: Usuario? = null
    var deviceToken: String = ""

    private val SECRET_BYTE_ARRAY = byteArrayOf(1, 2, 3, 4, 5, 6)


    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    private val phonePattern = "^\\+[1-9]{1}[0-9]{7,11}\$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        togglePass()

        randomAlphaNumericString(6)

        imagen_btn.setOnClickListener {
            selectImage()
        }

        register_btn.setOnClickListener {
//            validateEmail()
//            saveUserFirestore()
            createUser()
        }

        mCompressor = FileCompressor(this)

//        Toast.makeText(this, code, Toast.LENGTH_LONG).show()

//        saveUserFirestore()

//        createKey(baseContext)

        if(isHardwareBackedKeyStore()){
            Toast.makeText(this, "sii", Toast.LENGTH_LONG).show()

        }
        else{
            Toast.makeText(this, "noo", Toast.LENGTH_LONG).show()
        }

        tryEncrypt()

    }

    fun togglePass(){
        show_pass_btn.setOnClickListener {
            if (etPass.transformationMethod == PasswordTransformationMethod.getInstance()) {
                etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            } else {
                etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        }
    }

    fun createUser(){
        if (etNombres.text.toString() == "") {
            etNombres.error = "Campo obligatorio"
            return
        }
        if (etApellidos.text.toString() == "") {
            etApellidos.error = "Campo obligatorio"
            return
        }
        if (etEmail.text.toString() == "" || !etEmail.text.matches(emailPattern.toRegex())) {
            etEmail.error = "Escribe un email valido"
            return
        }
        if (etCelular.text.toString() == "" || !etCelular.text.matches(phonePattern.toRegex())) {
            etCelular.error = "Escribe un número valido"
            return
        }
        if (etPass.text.toString() == "") {
            etPass.error = "Campo obligatorio"
            return
        }
        if (mPhotoFile == null){
            Toast.makeText(this, "Selleccione una imagen de perfil", Toast.LENGTH_LONG).show()
            return
        }

        val inputStream: InputStream = FileInputStream(mPhotoFile!!) // You can get an inputStream using any I/O API

        var bytes: ByteArray
        val buffer = ByteArray(8192)
        var bytesRead: Int = 0
        val output = ByteArrayOutputStream()

        try {
            while (inputStream.read(buffer).also({ bytesRead = it }) != -1) {
                output.write(buffer, 0, bytesRead)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        bytes = output.toByteArray()
        val encodedString = Base64.encodeToString(bytes, Base64.DEFAULT)

        cover.visibility = View.VISIBLE

        var date: Date = SimpleDateFormat("dd/MM/yyyy").parse(etFecha.dayOfMonth.toString() + "/" + etFecha.month.toString() + "/" + etFecha.year.toString())

        usuario = Usuario(
            etNombres.text.toString(),
            etApellidos.text.toString(),
            date,
            etCelular.text.toString(),
            encodedString,
            code,
            Auth(etEmail.text.toString(), etEmail.text.toString())
        )

        Toast.makeText(this, encodedString, Toast.LENGTH_LONG).show()



    }

    fun saveUserFirestore(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            deviceToken = token + ""
//            etNombres.setText(token)
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })

        db.collection("usuario").document(code).get()
            .addOnCompleteListener {
                val refreshedToken: String = FirebaseInstanceId.getInstance().token!!

                Toast.makeText(
                    applicationContext,
                    refreshedToken,
                    Toast.LENGTH_LONG
                ).show()

                if (it.isSuccessful) {
                    if (it.result!!.data != null) {
                        randomAlphaNumericString(6)
                        usuario!!.codigo = code
                        saveUserFirestore()
                    }
                    else{

                        var user: UsuarioFire = UsuarioFire(
                            usuario!!.auth!!.email,
                            usuario!!.auth!!.contrasena,
                            deviceToken,
                            getDeviceId(),
                            true
                        )
                        db.collection("usuario").document(code)
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(
                                    applicationContext,
                                    "Se creo",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this, SuccessfulSignupActivity::class.java)

                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

                                startActivity(intent)
                            }

                    }
                }

            }
    }

    fun getDeviceId(): String? {
        return Settings.Secure.getString(
            baseContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun validateEmail() {
        if (etEmail.text.matches(emailPattern.toRegex())) {
            Toast.makeText(
                applicationContext, "Valid email address",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            etEmail.error = "Email incorrecto"
        }
    }

//    private fun createKey() {
//        // Generate a key to decrypt payment credentials, tokens, etc.
//        // This will most likely be a registration step for the user when they are setting up your app.
//        try {
//            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
//            keyStore.load(null)
//            val keyGenerator: KeyGenerator = KeyGenerator.getInstance(
//                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
//            )
//
//            // Set the alias of the entry in Android KeyStore where the key will appear
//            // and the constrains (purposes) in the constructor of the Builder
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                keyGenerator.init(
//                    KeyGenParameterSpec.Builder(
//                        KEY_NAME,
//                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
//                    )
//                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
//                        .setUserAuthenticationRequired(true) // Require that the user has unlocked in the last 30 seconds
//                        .setUserAuthenticationValidityDurationSeconds(
//                            AUTHENTICATION_DURATION_SECONDS
//                        )
//                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
//                        .build()
//                )
//
//
//            }
//            keyGenerator.generateKey()
//        } catch (e: NoSuchAlgorithmException) {
//            throw RuntimeException("Failed to create a symmetric key", e)
//        } catch (e: NoSuchProviderException) {
//            throw RuntimeException("Failed to create a symmetric key", e)
//        } catch (e: InvalidAlgorithmParameterException) {
//            throw RuntimeException("Failed to create a symmetric key", e)
//        } catch (e: KeyStoreException) {
//            throw RuntimeException("Failed to create a symmetric key", e)
//        } catch (e: CertificateException) {
//            throw RuntimeException("Failed to create a symmetric key", e)
//        } catch (e: IOException) {
//            throw RuntimeException("Failed to create a symmetric key", e)
//        }
//    }

    private val KEY_ALIAS = "Verifica_Key_Alias1"
    private val KEYSTORE_TYPE = "AndroidKeyStore"

    private fun isKeyPresent(): Boolean{
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null)
        Toast.makeText(this, privateKey.toString(), Toast.LENGTH_SHORT).show()
        if (privateKey != null){
            return true
        }
        return false
    }


    fun createKey(context: Context): Boolean {
        try {

            return if (!isKeyPresent()) {
                Log.d("TAG", "Creating new KEY. KEY ALIAS NOT FOUND")
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 10)
                val spec = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(X500Principal("CN=" + KEY_ALIAS.toString() + ", O=Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.time)
                        .setEndDate(end.time)
                        .build()
                } else {
                    TODO("VERSION.SDK_INT < JELLY_BEAN_MR2")
                }
                val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA", KEYSTORE_TYPE)
                generator.initialize(spec)
                generator.generateKeyPair()
                true
            } else {
                Log.d("TAG", "KEY ALIAS Exists")
                false
            }
        } catch (e: java.lang.Exception) {
//            Log.e(TAG, Log.getStackTraceString(e))
        }
        return false
    }

    private fun isHardwareBackedKeyStore(): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return false
        } else {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val privateKey = keyStore.getKey(KEY_ALIAS, null)
                val keyFactory = KeyFactory.getInstance(privateKey.algorithm, "AndroidKeyStore")
                val keyInfo = keyFactory.getKeySpec(
                    privateKey,
                    KeyInfo::class.java
                )
                return keyInfo.isInsideSecureHardware
            } catch (e: Exception) {
            }
        }
        return false
    }




    @RequiresApi(Build.VERSION_CODES.M)
    private fun tryEncrypt(): Boolean {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKey = keyStore.getKey(KEY_ALIAS, null)
            Log.w("TAG", "decrypt" + secretKey.toString())
            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")


            // Try encrypting something, it will only work if the user authenticated within
            // the last AUTHENTICATION_DURATION_SECONDS seconds.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val nombre = "gunar"

            val iv = cipher.getIV();


            val ciphertext: ByteArray = cipher.doFinal(nombre.toByteArray(Charset.defaultCharset()))
            Log.w("TAG", "crypt" + ciphertext.toString())


            tryDecrypt(ciphertext)
            return true
        }
        catch (e: BadPaddingException) {
            Log.w("TAG", "error" + e.message)
            throw RuntimeException(e)
        } catch (e: IllegalBlockSizeException) {
            throw RuntimeException(e)
        } catch (e: KeyStoreException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException(e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException(e)
        }
    }

    private fun tryDecrypt(ciphertext: ByteArray){
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKey = keyStore.getKey(KEY_ALIAS, null)

            Log.w("TAG", "decrypt" + secretKey.toString())

            val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")

            cipher.init(Cipher.DECRYPT_MODE, secretKey)


            val decryptedBytes = cipher.doFinal(ciphertext, 256)
            Log.w("TAG", "decrypt" + decryptedBytes)

        }
        catch (e: Exception) {
            Log.w("TAG", "error" + e.message)
            throw RuntimeException(e)
        }
    }




    fun randomAlphaNumericString(desiredStrLength: Int): Unit {
        val charPool: List<Char> = ('A'..'N') + ('P'..'Z') + ('1'..'9')

        // 35 elevado a 6
        code = (1..desiredStrLength)
            .map{ kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    private fun selectImage() {
        val items = arrayOf<CharSequence>(
            "Tomar foto", "Seleccionar de galería",
            "Cancelar"
        )
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setItems(items) { dialog, item ->
            if (items[item] == "Tomar foto") {
                requestStoragePermission(true)
            } else if (items[item] == "Seleccionar de galería") {
                requestStoragePermission(false)
            } else if (items[item] == "Cancelar") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    /**
     * Capture image from camera
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    applicationContext, "com.verifica.provider",
                    photoFile
                )
                mPhotoFile = photoFile
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }


    /**
     * Select image fro gallery
     */
    private fun dispatchGalleryIntent() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {

//                progressBar2.visibility = View.VISIBLE
                try {
                    mPhotoFile = mCompressor!!.compressToFile(mPhotoFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                image_container.visibility = View.VISIBLE

//                uploadToFirebase(mPhotoFile)


                Glide.with(this).load(mPhotoFile).apply(
                    RequestOptions().centerCrop().circleCrop()
//                        .placeholder(R.drawable.profile_pic_place_holder)
                ).into(profile_select_image!!)


            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
//                progressBar2.visibility = View.VISIBLE
                val selectedImage = data!!.data
                try {
                    mPhotoFile =
                        mCompressor!!.compressToFile(File(getRealPathFromUri(selectedImage)))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                image_container.visibility = View.VISIBLE
//                uploadToFirebase(mPhotoFile)

                Glide.with(this).load(mPhotoFile).apply(
                    RequestOptions().centerCrop().circleCrop()
//                        .placeholder(R.drawable.profile_pic_place_holder)
                ).into(profile_select_image!!)


            }
        }
    }

    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private fun requestStoragePermission(isCamera: Boolean) {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            dispatchTakePictureIntent()
                        } else {
                            dispatchGalleryIntent()
                        }
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->
                Toast.makeText(
                    applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }


    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    /**
     * Create file with current timestamp name
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }

    /**
     * Get real file path from URI
     *
     * @param contentUri
     * @return
     */
    fun getRealPathFromUri(contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(contentUri!!, proj, null, null, null)
            assert(cursor != null)
            val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }
}
