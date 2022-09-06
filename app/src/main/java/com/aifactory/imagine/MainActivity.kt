package com.aifactory.imagine

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.FileNotFoundException

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import android.webkit.MimeTypeMap

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.FileOutputStream
import android.os.Environment
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val GRANT_CAMERA_PERMISSION_REQUEST_CODE = 1;
    val CAMERA_REQUEST_CODE = 2;
    val GALLERY_REQUEST_CODE = 3;

    private lateinit var openGallery: ImageView
    private lateinit var openCamera: ImageView
    private lateinit var imagePreview: ImageView
    private lateinit var processButton: Button
    private lateinit var saveButton: Button

    private var destinationName = "";
    private var destinationType = "";

    private val MODEL_PATH = "model.tflite"

    private val tfLite by lazy {
        Interpreter(
            FileUtil.loadMappedFile(this, MODEL_PATH),
            Interpreter.Options())
    }

    private lateinit var modelHelper: ModelHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modelHelper = ModelHelper(tfLite)
        setContentView(R.layout.activity_main)
        bindViews()
        configureActions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when(requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val photo: Bitmap? = data?.extras?.get("data") as? Bitmap
                    photo?.let {
                        imagePreview.setImageBitmap(it)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    try {
                        data?.data?.let {
                            destinationType = getMimeType(this, it)!!
                            destinationName = it.lastPathSegment!! + "_redshift"

                            imagePreview.setImageBitmap(
                                BitmapFactory.decodeStream(
                                    contentResolver.openInputStream(it)
                                )
                            )
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GRANT_CAMERA_PERMISSION_REQUEST_CODE -> {
                loadImageFromCamera()
            }
        }
    }

    private fun bindViews() {
        openGallery = findViewById(R.id.openGallery)
        openCamera = findViewById(R.id.openCamera)
        imagePreview = findViewById(R.id.imagePreview)
        processButton = findViewById(R.id.processButton)
        saveButton = findViewById(R.id.saveButton)
        openGallery.rootView.setBackgroundColor(Color.WHITE)
    }

    private fun configureActions() {
        Log.d("MainActivity", ContextCompat
            .checkSelfPermission(this, CAMERA).toString())
        openGallery.setOnClickListener {
            loadImageFromGallery()
        }
        openCamera.setOnClickListener {
            if (
                ContextCompat
                    .checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(CAMERA),
                    GRANT_CAMERA_PERMISSION_REQUEST_CODE
                )
            } else { loadImageFromCamera() }
        }

        // Process image
        processButton.setOnClickListener {
            // TODO: - Bartol Freskura (ali filip rijesio)
            val processedImage = modelHelper.process(imagePreview.drawable.toBitmap())
            imagePreview.setImageBitmap(processedImage)
        }

        // Save action
        saveButton.setOnClickListener {
            val dir = Environment.DIRECTORY_DCIM
            val fileName = destinationName + destinationType
            val file = File(dir, fileName)
            try {
                val out = FileOutputStream(file)
                // TODO: - Save image as a 16 bit jpeg (or any other suitable format)
            } catch (e: Exception) {
                // TODO: - Handle exception
            }
        }
    }

    private fun loadImageFromCamera() {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE)
    }

    private fun loadImageFromGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST_CODE)
    }

    fun getMimeType(context: Context, uri: Uri): String? {
        //Check uri format to avoid null
        return if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                //If scheme is a content
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else {
                //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path!!)).toString())
            }
    }

}
