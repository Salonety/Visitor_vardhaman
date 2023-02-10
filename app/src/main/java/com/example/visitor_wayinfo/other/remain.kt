package com.example.visitor_wayinfo.other

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.visitor_wayinfo.ConnectionClass
import com.example.visitor_wayinfo.SessionManager
import com.example.visitor_wayinfo.databinding.ActivityRemainBinding
import com.opencsv.CSVWriter
import java.io.FileWriter
import java.io.IOException
import java.sql.Connection
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class remain : AppCompatActivity() {
    private val context: Context = this@remain
    private val PERMISSION_REQUEST_CODE = 112
    private lateinit var connectionClass: ConnectionClass
    private lateinit var binding: ActivityRemainBinding
    private var conn: Connection? = null
    private lateinit var con: Connection
    private lateinit var statement: Statement
    private lateinit var sessionManager: SessionManager
    private val rvtwo = ArrayList<Demo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //connection
        connectionClass = ConnectionClass(context)
        sessionManager = SessionManager(this)
        Handler(mainLooper).postDelayed({
            con = connectionClass.CONN()!!
            statement = con.createStatement()
            getlist() }, 200)


        if (!checkPermission()) {
            requestPermission()
        }
        createDirectory()

        binding.download.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Are you sure want to Save File?")
                    .setCancelText("No")
                    .setConfirmText("Yes")
                    .showCancelButton(true)
                    .setConfirmClickListener { sDialog ->
                        sDialog.cancel()
                        writeDataToCSV()
                    }
                    .setCancelClickListener { sDialog ->
                        sDialog.cancel()
                    }
                    .show()


        }


    }


    @RequiresApi(Build.VERSION_CODES.ECLAIR)
    private fun writeDataToCSV() {
        //val file = File(dir, "visitor_data.xlsx")
            val randomNumber1: Int = Random().nextInt(1000)
            val dm= SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

            val file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/Visitor File"+"/"+ "VisitorListFile"+randomNumber1+dm+".xlsx"
            )

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("RecyclerView Data")

        // Define the headers
        val headers = arrayOf("Barcode", "Remarks", "Username", "Department")
        val headerRow = sheet.createRow(0)
        for (i in headers.indices) {
            val cell = headerRow.createCell(i)
            cell.setCellValue(headers[i])
        }

        // Add the data from the RecyclerView
        for (i in rvtwo.indices) {
            val data = rvtwo[i]
            val row = sheet.createRow(i + 1)
            val barcodeCell = row.createCell(0)
            barcodeCell.setCellValue(data.barcode)
            val remarksCell = row.createCell(1)
            remarksCell.setCellValue(data.remarks)
            val usernameCell = row.createCell(2)
            usernameCell.setCellValue(data.user)
            val departmentCell = row.createCell(3)
            departmentCell.setCellValue(data.depa)
        }

        // Create the visitor folder if it does not exist
        val visitorFolder = File("Visitor File")
        if (!visitorFolder.exists()) {
            visitorFolder.mkdir()
        }

        // Write the data to the file
        val fileOut = FileOutputStream(file)
        workbook.write(fileOut)
        fileOut.close()
        workbook.close()
//        if (rvtwo.size > 0) {
//            val randomNumber1: Int = Random().nextInt(1000)
//            val dm= SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
//
//            val csvfile = File(
//                Environment.getExternalStorageDirectory()
//                    .toString() + "/Visitor File"+"/"+ "VisitorListFile"+randomNumber1+dm
//            )
//            val csv = csvfile.absolutePath
//            val writer: CSVWriter?
//            try {
//                writer = CSVWriter(
//                    FileWriter(csv),
//                    CSVWriter.NO_ESCAPE_CHARACTER,
//                    CSVWriter.NO_QUOTE_CHARACTER,
//                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
//                    CSVWriter.RFC4180_LINE_END
//                )
////                writer.writeAll(data);
////                val headerRecord = arrayOf(" ")
////                writer.writeNext(headerRecord)
//
//                for (i in rvtwo) {
//
//                    writer.writeNext(arrayOf(i.barcode+"|"+i.remarks+"|"+i.user+"|"+i.depa))
//                }
//                Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show()
//
//                rvtwo.clear()
//                writer.close()
//                overridePendingTransition(0, 0)
//                finish()
//                startActivity(intent)
//                overridePendingTransition(0, 0)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//
//        }
    }
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(
                this,
                "Write External Storage permission allows us to save files. Please allow this permission in App Settings.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .")
                createDirectory()
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .")
            }
        }
    }

    private fun createDirectory() {
        val myDirectory =
            File(Environment.getExternalStorageDirectory(), "Visitor File")
        if (!myDirectory.exists()) {
            myDirectory.mkdirs()
        }

    }


    private fun getlist() {
        val quary = "SELECT barcode,remarks,username,department FROM visitor"
        val resultSet = statement.executeQuery(quary)
        while (resultSet.next()) {
            val barcode =resultSet.getString("Barcode")
           val remarks= resultSet.getString("Remarks")
            val username= resultSet.getString("UserName")
            val depart= resultSet.getString("Department")
            rvtwo.add(Demo(barcode,remarks,username,depart))

            binding.remainCount.text=rvtwo.size.toString()
        }
        binding.rv2.adapter=Adapter_v(this,rvtwo)


    }

}