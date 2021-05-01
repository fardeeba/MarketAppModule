@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.appShared
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.lib.RestClient
import com.skylarksit.module.libs.Json
import com.skylarksit.module.network.SkylarksRequest
import com.skylarksit.module.pojos.services.AppInviteResultBean
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.util.*

open class SettingsActivity : MyAppCompatActivity() {
    lateinit var referralAppCell: View
    private lateinit var genderItems: Array<CharSequence>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        referralAppCell = findViewById(R.id.referralAppCell)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        initGenderArray()
        initProfile()
        initSettings()
        findViewById<View>(R.id.referralAppCell).setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                inviteLink
            }
        })
    }

    val inviteLink: Unit
        get() {
            Rest.request().baseUrl("api/market/app/").uri("appInviteLink").params(Json.param()).showLoader().response(
                AppInviteResultBean::class.java
            ) { response ->
                response as AppInviteResultBean
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, response.inviteLink)
                sendIntent.type = "text/plain"
                startActivityForResult(Intent.createChooser(sendIntent, "Share " + getString(R.string.app_name)), 324)
            }.get()
        }

    private fun initGenderArray() {
        genderItems = arrayOf(resources.getString(R.string.male), resources.getString(R.string.female))
    }

    private fun initSettings() {
        val sendFeedback = findViewById<RelativeLayout>(R.id.feedbackCell)
        val shareCell = findViewById<RelativeLayout>(R.id.shareCell)
        val faqCell = findViewById<RelativeLayout>(R.id.faqCell)
        val privacyCell = findViewById<RelativeLayout>(R.id.privacyCell)


        sendFeedback.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "plain/text"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(model.supportEmail))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " feedback")
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feeback)))
        }
        shareCell.setOnClickListener {
            appShared()
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            //            sendIntent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?" + BuildConfig.APPLICATION_ID);
            sendIntent.type = "text/plain"
            startActivityForResult(
                Intent.createChooser(
                    sendIntent,
                    getString(R.string.app_name) + " " + getString(R.string.app_name)
                ), 324
            )
        }
        if (model.faqUrl != null) {
            faqCell.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(model.faqUrl))
                startActivity(browserIntent)
            }
        } else {
            faqCell.visibility = View.GONE
        }
        if (model.termsUrl != null) {
            privacyCell.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(model.termsUrl))
                startActivity(browserIntent)
            }
        } else {
            privacyCell.visibility = View.GONE
        }
    }

    override fun onResume() {
        if (getBooleanValue("continueFinish")) {
            finish()
        }
        super.onResume()
    }

    private var activeField = 0
    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private fun initProfile() {
        nameField = findViewById(R.id.nameField)
        emailField = findViewById(R.id.emailField)
        birthdateField = findViewById(R.id.birthdateField)
        val genderField = findViewById<TextView>(R.id.genderField)
        nameField.setSingleLine()
        emailField.setSingleLine()
        nameField.setText(getStringValue("fullName"))
        nameField.onFocusChangeListener = OnFocusChangeListener { _, b ->
            if (b) {
                activeField = 1
            } else {
                updateName()
            }
        }
        nameField.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                updateName()
            }
            false
        }
        nameField.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                updateName()
            }
            false
        }
        var email = getStringValue("email")
        if (email.compareTo("null", ignoreCase = true) == 0) {
            email = ""
        }
        emailField.setText(email)
        emailField.onFocusChangeListener = OnFocusChangeListener { _, b ->
            if (b) {
                activeField = 2
            } else {
                updateEmail()
            }
        }
        emailField.setOnKeyListener { _, i, keyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                updateEmail()
            }
            false
        }
        emailField.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_UNSPECIFIED) {
                updateEmail()
            }
            false
        }
        var bd = getStringValue("birthdate")
        if (bd.compareTo("null", ignoreCase = true) == 0) {
            bd = ""
        }
        birthdateField.text = bd
        birthdateField.setOnClickListener {
            hideKeyboard(this@SettingsActivity)
            val newFragment: DialogFragment = DatePickerFragment()
            newFragment.show(supportFragmentManager, "datePicker")
        }
        val cg = getStringValue("gender")
        when {
            cg.equals("m", ignoreCase = true) -> {
                genderField.text = genderItems[0]
            }
            cg.equals("f", ignoreCase = true) -> {
                genderField.text = genderItems[1]
            }
            else -> {
                genderField.text = ""
            }
        }
        genderField.setOnClickListener {
            hideKeyboard(this@SettingsActivity)
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setTitle("Pick gender")
            builder.setItems(genderItems) { _, item ->
                var gender = "m"
                if (item == 1) {
                    genderField.text = genderItems[1]
                    gender = "f"
                } else {
                    genderField.text = genderItems[0]
                }
                val params = Utilities.getBasicParams()
                try {
                    params.put("gender", gender)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val g = gender
                val req = SkylarksRequest(
                    context,
                    "updateGender",
                    params,
                    { response ->
                        val message = Utilities.getResponseString(response, "message")
                        if (message.compareTo("valid", ignoreCase = true) == 0) {
                            saveValue("gender", g)
                        } else {
                            Toast.makeText(context, getString(R.string.failed_update_gender), Toast.LENGTH_LONG).show()
                        }
                    }) { error ->
                    Log.e("error", error.toString() + " " + error.javaClass)
                    Toast.makeText(context, getString(R.string.failed_update_gender), Toast.LENGTH_LONG).show()
                }
                RestClient.getInstance(context).addToRequestQueue(req)
            }
            builder.create().show()
        }
    }

    private fun updateName() {
        val fullName = nameField.text.toString().trim { it <= ' ' }
        if (fullName.isNotEmpty() && fullName.compareTo(getStringValue("fullName")) != 0) {
            val params = Utilities.getBasicParams()
            try {
                params.put("fullName", fullName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val req = SkylarksRequest(
                context,
                "updateFullName",
                params,
                { response ->
                    val message = Utilities.getResponseString(response, "message")
                    val description = Utilities.getResponseString(response, "description")
                    if (message.compareTo("valid", ignoreCase = true) == 0) {
                        saveValue("fullName", description)
                    } else {
                        Toast.makeText(context, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                    }
                }) { error ->
                Log.e("error", error.toString() + " " + error.javaClass)
                Toast.makeText(context, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
            }
            RestClient.getInstance(context).addToRequestQueue(req)
        }
    }

    private fun updateEmail() {
        val email = emailField.text.toString().trim { it <= ' ' }
        if (email.isNotEmpty() && email.compareTo(getStringValue(email)) != 0) {
            val params = Utilities.getBasicParams()
            try {
                params.put("email", email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val req = SkylarksRequest(
                context,
                "updateEmail",
                params,
                { response ->
                    val message = Utilities.getResponseString(response, "message")
                    val description = Utilities.getResponseString(response, "description")
                    if (message.compareTo("valid", ignoreCase = true) == 0) {
                        saveValue("email", description)
                    } else {
                        if (description != null && description.isNotEmpty()) {
                            Toast.makeText(context, description, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { error ->
                Log.e("error", error.toString() + " " + error.javaClass)
                Toast.makeText(context, getString(R.string.try_again), Toast.LENGTH_SHORT).show()
            }
            RestClient.getInstance(context).addToRequestQueue(req)
        }
    }

    private fun hideKeyboard(activity: Activity?) {
        Utilities.hideKeyboard(activity)
        processDataInput()
    }

    private fun processDataInput() {
        when (activeField) {
            1 -> updateName()
            2 -> updateEmail()
        }
        activeField = 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Utilities.CAMERA_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this@SettingsActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@SettingsActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            Utilities.CAMERA_PERMISSION
                        )
                    } else {
                        startActivityForResult(Utilities.getIntentForCam(this@SettingsActivity), 0)
                    }
                }
            }
            Utilities.CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(Utilities.getIntentForCam(this@SettingsActivity), 0)
                }
            }
        }
    }

    class DatePickerFragment : DialogFragment(), OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c[Calendar.YEAR]
            val month = c[Calendar.MONTH]
            val day = c[Calendar.DAY_OF_MONTH]

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(activity!!, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val bd = day.toString() + "-" + (month + 1) + "-" + year
            val params = Utilities.getBasicParams()
            try {
                params.put("birthdate", bd)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val bdf = day.toString() + " / " + (month + 1) + " / " + year
            val context = context
            val req = SkylarksRequest(
                context,
                "updateBirthdate",
                params,
                { response ->
                    val message = Utilities.getResponseString(response, "message")
                    if (message.compareTo("valid", ignoreCase = true) == 0) {
                        Log.e("aa", "saved")
                        LocalStorage.instance().save(context!!, "birthdate", bdf)
                    } else {
                        Toast.makeText(context, getString(R.string.failed_update_birthday), Toast.LENGTH_LONG).show()
                    }
                }) { error ->
                Log.e("error", error.toString() + " " + error.javaClass)
                Toast.makeText(context, getString(R.string.failed_update_birthday), Toast.LENGTH_LONG).show()
            }
            RestClient.getInstance(context).addToRequestQueue(req)
            birthdateField.text = bdf
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var birthdateField: TextView
    }
}
