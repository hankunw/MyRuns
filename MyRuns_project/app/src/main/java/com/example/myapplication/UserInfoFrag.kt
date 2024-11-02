package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.Util.Util
import java.io.File
//import com.example.camerademokotlin.Util

class UserInfoFrag : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var changePhoto: Button
    private lateinit var tempImgUri: Uri
    private lateinit var myViewModel: MyViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>

    private lateinit var usrName: EditText
    private lateinit var usrEmail: EditText
    private lateinit var usrPhoneNumber: EditText
    private lateinit var usrGender: RadioGroup
    private lateinit var usrClass: EditText
    private lateinit var usrMajor: EditText

    private val KEY_NAME = "USER_NAME"
    private val KEY_EMAIL = "USER_EMAIL"
    private val KEY_PHONE = "USER_PHONE"
    private val KEY_GENDER = "USER_GENDER"
    private val KEY_CLASS = "USER_CLASS"
    private val KEY_MAJOR = "USER_MAJOR"

    private lateinit var submitButton:Button
    private lateinit var cancelButton:Button

    private val tempImgFileName = "mike_temp_img.jpg"
    var  GALLERY_REQUEST_CODE=123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_modification)
        imageView = findViewById(R.id.imageProfile)
        changePhoto= findViewById(R.id.btnChangePhoto)

        // get input name:
        usrName = findViewById(R.id.edit_name)
        usrEmail = findViewById(R.id.edit_email)
        usrPhoneNumber = findViewById(R.id.edit_phone)
        usrGender = findViewById(R.id.edit_gender)
        usrClass = findViewById(R.id.edit_class)
        usrMajor = findViewById(R.id.edit_major)
        submitButton = findViewById(R.id.submit)
        cancelButton = findViewById(R.id.cancel)


        Util.checkPermissions(this)


        var sharedPreferences : SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        var spEditor: SharedPreferences.Editor = sharedPreferences.edit()
//        spEditor.clear()
//        spEditor.apply()

        if(sharedPreferences != null)
            loadProfileSP(sharedPreferences)

        if(savedInstanceState != null)            //using savedInstanceState, does not psv after activity dies
            loadProfileSIS(savedInstanceState)
        //println("reaching here")


        val tempImgFile = File(getExternalFilesDir(null), tempImgFileName) //XD: try Environment.DIRECTORY_PICTURES instead of "null"
        tempImgUri = FileProvider.getUriForFile(this,
            "com.xd.MyRuns", tempImgFile)


        changePhoto.setOnClickListener(){
            var dialog = AlertDialog.Builder(this)
                .setTitle("Pick Profile Picture")
                .setItems(arrayOf("Open Camera","Select from Gallery")
                ) { dialog, which ->
                    dialog.dismiss()
                    if(which==0){
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
                        cameraResult.launch(intent)
                    }else{
                        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
                    }
                }
            dialog.show()

        }
        submitButton.setOnClickListener(){
//            var spEditor: SharedPreferences.Editor = sharedPreferences.edit()
            saveProfile(sharedPreferences)
            // println("hello world")
        }
        cancelButton.setOnClickListener(){
            cancelOnClick(it)
        }

        cameraResult = registerForActivityResult(StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                println("reaching here")
                val bitmap = Util.getBitmap(this, tempImgUri)
                myViewModel.userImage.value = bitmap

                //***
//                line = tempImgUri.path.toString()
//                textView.setText(line)
            }
        }
        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.userImage.observe(this, { it ->
            imageView.setImageBitmap(it)
        })

        if(tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            imageView.setImageBitmap(bitmap)
        }
    }

    fun loadProfileSP(sharedPreferences : SharedPreferences){

        usrName.setText(sharedPreferences.getString(KEY_NAME, ""))
        usrEmail.setText(sharedPreferences.getString(KEY_EMAIL, ""))
        usrPhoneNumber.setText(sharedPreferences.getString(KEY_PHONE, ""))
        usrGender.clearCheck()
        usrGender.check(sharedPreferences.getInt(KEY_GENDER, R.id.male_button))
        usrClass.setText(sharedPreferences.getString(KEY_CLASS, ""))
        usrMajor.setText(sharedPreferences.getString(KEY_MAJOR, ""))
    }
    fun loadProfileSIS(savedInstanceState: Bundle?){
        usrName.setText(savedInstanceState?.getString(KEY_NAME, ""))
        usrEmail.setText(savedInstanceState?.getString(KEY_EMAIL, ""))
        usrPhoneNumber.setText(savedInstanceState?.getString(KEY_PHONE, ""))
        usrGender.clearCheck()
        savedInstanceState?.getInt(KEY_GENDER, R.id.male_button)
            ?.let { usrGender.check(it) }
        usrClass.setText(savedInstanceState?.getString(KEY_CLASS, ""))
        usrMajor.setText(savedInstanceState?.getString(KEY_MAJOR, ""))
    }
    fun saveProfile(sharedPreferences: SharedPreferences){
        var settings : SharedPreferences = getSharedPreferences("MyPrefs", 0)
        var spEditor : SharedPreferences.Editor  = settings.edit();


        spEditor.putString(KEY_NAME, usrName.text.toString())
        spEditor.putString(KEY_EMAIL, usrEmail.text.toString())
        spEditor.putString(KEY_PHONE,usrPhoneNumber.text.toString())
        spEditor.putInt(KEY_GENDER, usrGender.checkedRadioButtonId)
        spEditor.putString(KEY_CLASS, usrClass.text.toString())
        spEditor.putString(KEY_MAJOR,usrMajor.text.toString())

        spEditor.commit()
        println("finish saving")
        this.finish()
    }
    private fun cancelOnClick(view:View){
        this.finish()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_NAME, usrName.text.toString())
        outState.putString(KEY_EMAIL, usrEmail.text.toString())
        outState.putString(KEY_PHONE, usrPhoneNumber.text.toString())
        outState.putInt(KEY_GENDER, usrGender.checkedRadioButtonId)
        outState.putString(KEY_CLASS, usrClass.text.toString())
        outState.putString(KEY_MAJOR, usrMajor.text.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> {
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}