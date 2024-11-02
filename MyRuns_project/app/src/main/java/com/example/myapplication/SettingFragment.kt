package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.w3c.dom.Text

class SettingFragment : Fragment() {
    lateinit var unitText: LinearLayout
    lateinit var userInfo: LinearLayout
    lateinit var commentsText:LinearLayout
    lateinit var setting_privacy:RelativeLayout
    lateinit var privacy_post_check:CheckBox
    lateinit var website_url: LinearLayout
    lateinit var sp: SharedPreferences
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        sp=requireContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        return inflater.inflate(R.layout.setting_fragment, container, false)
    }

//    override fun onStart() {
//        super.onStart()
//        sp=requireContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
//    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var privacyChecked = sp.getBoolean("privacy_checked",false)
        userInfo=view.findViewById(R.id.setting_userinfo)
        unitText=view.findViewById(R.id.setting_unit)
        commentsText=view.findViewById(R.id.setting_comments)
        setting_privacy=view.findViewById(R.id.setting_privacy)
        privacy_post_check=view.findViewById(R.id.privacy_post)
        website_url=view.findViewById(R.id.setting_misc)
        privacy_post_check.isChecked = privacyChecked
        userInfo.setOnClickListener(){
            val intent = Intent(requireContext(),UserInfoFrag::class.java)
            startActivity(intent)
        }
        setting_privacy.setOnClickListener(){
            privacy_post_check.isChecked = !privacy_post_check.isChecked
            var edit=sp.edit()
            edit.putBoolean("privacy_checked",privacy_post_check.isChecked)
            edit.commit()
        }
        privacy_post_check.setOnClickListener(){
            var edit=sp.edit()
            edit.putBoolean("privacy_checked",privacy_post_check.isChecked)
            edit.commit()
        }
        unitText.setOnClickListener(){
            var unit_checkBox = sp.getInt("unit_check",-1)
            var dialog =AlertDialog.Builder(requireContext())
                .setTitle("Unit")
                .setSingleChoiceItems(arrayOf("Metric(Kilometers)","Imperial(Miles)"),unit_checkBox
                ) { dialog, which ->
                    var edit = sp.edit();
                    edit.putInt("unit_check", which)
                    edit.commit()
                    dialog.dismiss()
                }
            dialog.setPositiveButton("Cancel",null)
            dialog.create()
            dialog.show()
        }
        commentsText.setOnClickListener(){
            var comments = sp.getString("setting_comments","")
            var edInput= EditText(requireContext())
            edInput.setText(comments)
            var dialog =AlertDialog.Builder(requireContext())
                .setTitle("Comments")
                .setView(edInput)
                .setNegativeButton("CANCEL",null)
            dialog.setPositiveButton("OK") { dialog, which ->
                val comments = edInput.text.toString()
                val edit = sp.edit()
                edit.putString("setting_comments", comments)
                edit.commit()
            }
            dialog.create()
            dialog.show()

        }
        website_url.setOnClickListener(){
            val uri = Uri.parse("https://www.sfu.ca/computing.html")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

    }


}