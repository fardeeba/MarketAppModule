package com.skylarksit.module.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.skylarksit.module.R
import com.skylarksit.module.utils.MyAppCompatActivity

class NicknameActivity : MyAppCompatActivity(), View.OnClickListener {
    private lateinit var typeYourOwnNickName: EditText
    override fun onClick(v: View) {
        val nickName = (v as TextView).text.toString()
        val i = Intent()
        i.putExtra("RESULT", nickName)
        when (nickName) {
            "Home" -> i.putExtra("type", 1)
            "Work" -> i.putExtra("type", 2)
            else -> i.putExtra("type", 4)
        }
        setResult(RESULT_OK, i)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)
        val nickname = intent.getStringExtra("nickname")
        val homeNickName = findViewById<TextView>(R.id.homeNickName)
        val workNickName = findViewById<TextView>(R.id.workNickName)
        val countryHouseNickName = findViewById<TextView>(R.id.countryHouseNickName)
        typeYourOwnNickName = findViewById(R.id.typeYourOwnNickName)
        typeYourOwnNickName.setSingleLine()
        if (nickname != null && nickname.isNotEmpty()) {
            typeYourOwnNickName.setText(nickname)
        }
        typeYourOwnNickName.findFocus()
        homeNickName.setOnClickListener(this)
        countryHouseNickName.setOnClickListener(this)
        workNickName.setOnClickListener(this)
        typeYourOwnNickName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                val customNickName = typeYourOwnNickName.text.toString()
                if (customNickName.isEmpty()) {
                    Toast.makeText(context, getString(R.string.field_is_empty), Toast.LENGTH_SHORT).show()
                } else {
                    val i = Intent()
                    i.putExtra("RESULT", customNickName)
                    i.putExtra("type", 4)
                    setResult(RESULT_OK, i)
                    finish()
                }
            }
            false
        }
    }
}
