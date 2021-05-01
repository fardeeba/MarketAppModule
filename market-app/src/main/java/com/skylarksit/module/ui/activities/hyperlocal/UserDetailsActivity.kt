package com.skylarksit.module.ui.activities.hyperlocal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.skylarksit.module.MainActivity
import com.skylarksit.module.R
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.Json
import com.skylarksit.module.pojos.ResponseBean
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities

class UserDetailsActivity : MyAppCompatActivity() {
    lateinit var fullNameField: EditText
    lateinit var emailField: EditText
    lateinit var confirmButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details_activity)
        fullNameField = findViewById(R.id.fullNameField)
        emailField = findViewById(R.id.emailField)
        confirmButton = findViewById(R.id.confirmButton)
        confirmButton.setOnClickListener(View.OnClickListener setOnClickListener@{
            val fullName = fullNameField.text.toString().trim { it <= ' ' }
            val email = emailField.text.toString().trim { it <= ' ' }
            val shake = AnimationUtils.loadAnimation(this@UserDetailsActivity, R.anim.shake)
            if (fullName.isEmpty()) {
                fullNameField.startAnimation(shake)
                Toast.makeText(context, getString(R.string.enter_full_name), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                emailField.startAnimation(shake)
                Toast.makeText(context, getString(R.string.enter_email), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!Utilities.isValidEmail(email)) {
                emailField.startAnimation(shake)
                Toast.makeText(context, getString(R.string.enter_valid_email), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Rest.request().uri("updateFullName/")
                .params(Json.param().p("fullName", fullName).p("email", email).toJson()).showLoader(
                    getString(
                        R.string.updating
                    )
                ).response(
                    ResponseBean::class.java
                ) {
                    LocalStorage.instance().save(context, "fullName", fullName)
                    LocalStorage.instance().save(context, "email", email)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    Utilities.loginCrashlytics(context)
                    finish()
                }.post()
        })
    }
}
