package com.verifica.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.verifica.R
import com.verifica.setup.FileCompressor
import java.util.*
import kotlinx.android.synthetic.main.activity_signup.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class SignupActivity : AppCompatActivity() {
    val REQUEST_TAKE_PHOTO = 1
    val REQUEST_GALLERY_PHOTO = 2
    var mPhotoFile: File? = null
    var mCompressor: FileCompressor? = null

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        imagen_btn.setOnClickListener {
            selectImage()
        }

        register_btn.setOnClickListener {
            validateEmail()
        }

        mCompressor = FileCompressor(this);

        Toast.makeText(this, randomAlphaNumericString(6), Toast.LENGTH_LONG).show()
    }

    fun validateEmail() {
        if (etEmail.text.matches(emailPattern.toRegex())) {
            Toast.makeText(applicationContext, "Valid email address",
                Toast.LENGTH_SHORT).show()
        } else {
            etEmail.error = "Email incorrecto"
        }
    }

    fun randomAlphaNumericString(desiredStrLength: Int): String {
        val charPool: List<Char> = ('A'..'N') + ('P'..'Z') + ('1'..'9')

        // 35 elevado a 6
        return (1..desiredStrLength)
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