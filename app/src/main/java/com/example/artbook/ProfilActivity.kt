package com.example.artbook

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.drm.DrmStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_profil.*
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest

class ProfilActivity : AppCompatActivity() {

    var selectedPicture: Uri? = null
    var selectedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)


        var edtArtName = findViewById<EditText>(R.id.artName)
        var imgArtPhoto = findViewById<ImageView>(R.id.imageView)
        var edtArtistName = findViewById<EditText>(R.id.artistName)
        var edtArtYear = findViewById<EditText>(R.id.artYear)
        var btnArtAdd = findViewById<Button>(R.id.button)
        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("new")) {
            edtArtName.setText("")
            edtArtistName.setText("")
            edtArtYear.setText("")
            btnArtAdd.visibility = View.VISIBLE
            val selectedImageBackground = BitmapFactory.decodeResource(
                applicationContext.resources,
                R.drawable.ic_launcher_background
            )
            imgArtPhoto.setImageBitmap(selectedImageBackground)
        } else {
            val selectedId = intent.getIntExtra("id", 1)
            btnArtAdd.visibility = View.INVISIBLE

            try {
                val database = openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)
                val cursor = database.rawQuery("SELECT *FROM arts WHERE id=?", arrayOf(selectedId.toString()))

                val artNamex = cursor.getColumnIndex("artname")
                val artistNamex = cursor.getColumnIndex("artistname")
                val artYearx = cursor.getColumnIndex("artyear")
                val imagex = cursor.getColumnIndex("image")
                while (cursor.moveToNext()) {
                    edtArtName.setText(cursor.getString(artNamex))
                    edtArtistName.setText(cursor.getString(artistNamex))
                    edtArtYear.setText(cursor.getString(artYearx))

                    val byteArray=cursor.getBlob(imagex)
                    val bitmap=BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                    imgArtPhoto.setImageBitmap(bitmap)

                }
                cursor.close()

            } catch (ex: Exception) {
                println(ex.printStackTrace())
            }
        }


        imgArtPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                val intentGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentGallery, 2)

            }
        }
        btnArtAdd.setOnClickListener {
            val artName = edtArtName.text.toString()
            val artistName = edtArtistName.text.toString()
            val artYear = edtArtYear.text.toString()

            if (selectedBitmap != null) {
                val outputStream = ByteArrayOutputStream()
                val smallBitmap = makeSmallerBitMap(selectedBitmap!!, 300)
                smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
                val byteArray = outputStream.toByteArray()

                try {
                    val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE, null)

                    database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR,artyear VARCHAR,image BLOB)")

                    val sqlString =
                        "INSERT INTO arts(artname,artistname,artyear,image) VALUES(?,?,?,?)"

                    val statement = database.compileStatement(sqlString)

                    statement.bindString(1, artName)
                    statement.bindString(2, artistName)
                    statement.bindString(3, artYear)
                    statement.bindBlob(4, byteArray)
                    statement.execute()
                } catch (ex: Exception) {
                    Log.i("dbError", "${ex.printStackTrace()}")
                }
                finish()
            }
        }

    }

    fun makeSmallerBitMap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRate: Double = width.toDouble() / height.toDouble()
        if (bitmapRate > 1) {
            width = maxSize
            val scalHeight = width / bitmapRate
            height = scalHeight.toInt()
        } else {
            height = maxSize
            val scaleWidth = height + bitmapRate
            width = scaleWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] > PackageManager.PERMISSION_GRANTED) {
                val intentGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentGallery, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        try {
            if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
                selectedPicture = data.data
                if (selectedPicture != null) {
                    if (Build.VERSION.SDK_INT >= 28) {
                        val source =
                            ImageDecoder.createSource(this.contentResolver, selectedPicture!!)
                        selectedBitmap = ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(selectedBitmap)
                    } else {
                        selectedBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, selectedPicture)
                        imageView.setImageBitmap(selectedBitmap)
                    }
                }
            }
        } catch (ex: Exception) {
            println(ex)
        }

        super.onActivityResult(requestCode, resultCode, data)

    }
}
