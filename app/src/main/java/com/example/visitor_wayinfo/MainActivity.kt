package com.example.visitor_wayinfo

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.visitor_wayinfo.databinding.ActivityMainBinding
import com.example.visitor_wayinfo.other.Adapter_v
import com.example.visitor_wayinfo.other.Demo
import com.example.visitor_wayinfo.other.remain
import com.google.zxing.integration.android.IntentIntegrator
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.sql.Connection
import java.sql.Statement
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(),View.OnClickListener {
    private val CAMERA_REQUEST_CODE = 100
    private val context: Context = this@MainActivity
    private lateinit var connectionClass: ConnectionClass
    private lateinit var binding: ActivityMainBinding
    private var conn: Connection? = null
    private lateinit var con: Connection
    private lateinit var statement: Statement
    private lateinit var sessionManager: SessionManager
    private val rvtwoList = ArrayList<Demo>()
    private val rvtwo = ArrayList<Demo>()
    private var rem=""
    private var usern=""
    private var departm=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //connection
        connectionClass = ConnectionClass(context)
        sessionManager = SessionManager(this)
        Handler(mainLooper).postDelayed({
            con = connectionClass.CONN()!!
            statement = con.createStatement() }, 200)






        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // here, Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 50)
        }




        binding.btnQrCode.setOnClickListener(this)









//        //bar code
        binding.textBr.setOnKeyListener { view, keyCode, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                return@setOnKeyListener false
            }
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                binding.remarks.requestFocus()
                return@setOnKeyListener true
            }
            false
        }
        binding.List.setOnClickListener {
            intent = Intent(applicationContext, remain::class.java)
            startActivity(intent)

        }


        binding.submit.setOnClickListener {
            rem= binding.remarks.text.toString()
            usern=binding.username.text.toString()
            departm=binding.department.text.toString()
            insertIndb(rem,usern,departm)

        }

        binding.cam.setOnClickListener(View.OnClickListener {
//            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
//            startActivityForResult(intent, 7)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)



        })

    }

    private fun createDirectory() {
        val myDirectory1 =
            File(Environment.getExternalStorageDirectory(), "Visitor photo")
        if (!myDirectory1.exists()) {
            myDirectory1.mkdirs()
        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            saveMediaToStorage(bitmap)
            binding.imageView.setImageBitmap(bitmap)
        }
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(baseContext, "Cancelled", Toast.LENGTH_SHORT).show()
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message
                binding.textBr.setText(intentResult.contents)
                 rem= binding.remarks.text.toString()
                usern=binding.username.text.toString()
                departm=binding.department.text.toString()

                rvtwoList.add(0, Demo(intentResult.contents,rem,usern,departm))
                binding.rvrecyclerView.adapter = Adapter_v(this, rvtwoList)
                binding.tvcoutn.text = rvtwoList.size.toString()





            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()
        }


    }






    private fun insertIndb(rem: String, usern: String, departm: String,) {
        if (::con.isInitialized) {
            for (itemDetail in rvtwoList) {
                try {
                    var n = 0
                    val sql =
                        "INSERT INTO visitor (Barcode,Remarks,UserName,Department) VALUES (?,?,?,?)"
                        con = connectionClass.CONN()!!
                        statement = con.createStatement()
                       val statement = con.prepareStatement(sql)
                    statement.setString(1, itemDetail.barcode)//BarCode
                    statement.setString(2, rem)//Qty
                    statement.setString(3, usern)//Qty
                    statement.setString(4, departm)//Qty

                    n = statement.executeUpdate()
                    if (n > 0) {
                        Toast.makeText(this, "successfully Inserted", Toast.LENGTH_SHORT).show()
                        binding.textBr.text.clear()
                        binding.remarks.text.clear()
                        rvtwoList.clear()
                    } else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                } catch (e: java.lang.Exception) {
                    Log.e(ContentValues.TAG, "error: " + e.message)
                    e.printStackTrace()
                }
            }
        }




    }



    override fun onClick(v: View?) {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setCameraId(0)
        intentIntegrator.setPrompt("Scan a barcode or QR Code")
        intentIntegrator.setOrientationLocked(true)
        intentIntegrator.initiateScan()

    }


}

