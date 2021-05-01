@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.core

import android.content.*
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.gson.Gson
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.activationCompleted
import com.skylarksit.module.analytics.Segment.activationStarted
import com.skylarksit.module.analytics.Segment.identify
import com.skylarksit.module.analytics.Segment.register
import com.skylarksit.module.lib.ErrorResource
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.libs.SkylarksUtil
import com.skylarksit.module.libs.Json
import com.skylarksit.module.libs.alertdialog.NokAlertDialog
import com.skylarksit.module.pojos.ActivateUserResponseBean
import com.skylarksit.module.pojos.CountryBean
import com.skylarksit.module.pojos.RequestActivationResponseBean
import com.skylarksit.module.pojos.ResponseBean
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Services
import com.skylarksit.module.utils.Services.ServiceResult
import com.skylarksit.module.utils.Utilities
import java.text.NumberFormat
import java.util.regex.Pattern

class LoginActivity : MyAppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    lateinit var countryButton: TextView
    lateinit var phoneNumberField: EditText
    lateinit var titleText: TextView
    lateinit var bottomText: TextView
    lateinit var descriptionText: TextView
    lateinit var emailLayout: RelativeLayout
    lateinit var emailField: EditText
    lateinit var nameLayout: RelativeLayout
    lateinit var fullNameField: EditText
    lateinit var nextButton: Button
    lateinit var activationCodeField: EditText
    lateinit var phoneNumberLayout: RelativeLayout
    private var currentState: String? = null
    private var supportEmail: String? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    public override fun onStart() {
        super.onStart()
        active = true
    }

    public override fun onStop() {
        super.onStop()
        active = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)
        countryButton = findViewById(R.id.countryCodeButton)
        phoneNumberField = findViewById(R.id.phoneNumberField)
        titleText = findViewById(R.id.titleText)
        bottomText = findViewById(R.id.bottomText)
        descriptionText = findViewById(R.id.descriptionText)
        emailLayout = findViewById(R.id.emailLayout)
        emailField = findViewById(R.id.emailField)
        nameLayout = findViewById(R.id.nameLayout)
        fullNameField = findViewById(R.id.fullNameField)
        nextButton = findViewById(R.id.nextButton)
        phoneNumberLayout = findViewById(R.id.phoneNumberLayout)
        activationCodeField = findViewById(R.id.activationCodeField)


        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addApi(Auth.CREDENTIALS_API)
            .build()


        bottomText.setOnClickListener { bottomTextClick() }
        nextButton.setOnClickListener { nextButton() }

        countryButton.setOnClickListener {
            val builder = AlertDialog.Builder(this@LoginActivity)
            builder.setTitle(getString(R.string.title_country_code))
            builder.setItems(countriesItems) { _: DialogInterface?, item: Int ->
                try {
                    Utilities.pickedCountry = countries[item]
                    countryButton.text = NumberFormat.getInstance().format(Utilities.pickedCountry.phoneCode)
                } catch (ignored: Exception) {
                }
            }
            builder.create().show()
        }
        val idCountry = LocalStorage.instance().getString("idCountry")
        if (Utilities.notEmpty(idCountry)) {
            val countryId = idCountry.toInt()
            loadCountries(countryId)
        } else {
            loadCountries(null)
        }
        val phoneNumber = LocalStorage.instance().getString("phoneNumberOnly")
        if (Utilities.notEmpty(phoneNumber)) {
            phoneNumberField.setText(phoneNumber)
        }
        val fullName = LocalStorage.instance().getString("fullName")
        if (Utilities.notEmpty(fullName)) fullNameField.setText(fullName)
        val email = LocalStorage.instance().getString("email")
        if (Utilities.notEmpty(email)) emailField.setText(email)
        val state: String
        val loggedIn = LocalStorage.instance().getBoolean("login")
        state = if (!loggedIn) {
            "login"
        } else {
            "profile"
        }
        changeState(state)
        Utilities.showKeyboard(this)
        val client = SmsRetriever.getClient(this /* context */)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener { }
        task.addOnFailureListener { }
    }

    private fun requestHint() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val intent = Auth.CredentialsApi.getHintPickerIntent(
            mGoogleApiClient, hintRequest
        )
        try {
            startIntentSenderForResult(
                intent.intentSender,
                RESOLVE_HINT, null, 0, 0, 0
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    // Obtain the phone number from the result
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                val credential: Credential = data!!.getParcelableExtra(Credential.EXTRA_KEY)!!
                phoneNumberField.setText(credential.id)
                if (Utilities.notEmpty(credential.id)) requestCode()
            }
        }
    }

    private fun loadCountries(selectedCountryId: Int?) {
        try {
            val telephonyManager = (context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager)
            var iso = telephonyManager.simCountryIso
            if (iso == null || iso.isEmpty()) {
                iso = telephonyManager.networkCountryIso
            }
            val jsonFile = SkylarksUtil.assetJSONFile("countries.json", context)
            countries = Gson().fromJson(jsonFile, Array<CountryBean>::class.java)
            countriesItems = arrayOfNulls(countries.size)
            for (i in countries.indices) {
                val country = countries[i]
                countriesItems[i] = country.niceName + " +" + country.phoneCode
                if (selectedCountryId != null) {
                    if (country.id == selectedCountryId) {
                        Utilities.pickedCountry = country
                        countryButton.text = NumberFormat.getInstance().format(country.phoneCode)
                    }
                } else if (country.iso != null && country.iso.equals(iso, ignoreCase = true)) {
                    Utilities.pickedCountry = country
                    countryButton.text = NumberFormat.getInstance().format(country.phoneCode)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private lateinit var countries: Array<CountryBean>
    private lateinit var countriesItems: Array<CharSequence?>
    fun requestCode() {
        if (Utilities.pickedCountry == null) {
            Utilities.Toast(getString(R.string.msg_select_country_code))
            return
        }
        val phoneNumber = phoneNumberField.text.toString().trim { it <= ' ' }
        if (phoneNumber.length < 3) {
            Toast.makeText(this@LoginActivity, getString(R.string.error_phone_number_missing), Toast.LENGTH_LONG).show()
            return
        }
        activationStarted(phoneNumber)
        startSmsReceiver()
        val params = Json.param()
        params.p("idCountry", Utilities.pickedCountry.id)
        params.p("phoneNumber", phoneNumber)
        params.p("appName", getString(R.string.application))
        params.p("os", "android")
        Rest.request().uri("requestActivation/").userApi().showLoader(getString(R.string.please_wait))
            .params(params.toJson()).response(
                RequestActivationResponseBean::class.java,
                Response.Listener { response: RequestActivationResponseBean ->
                    LocalStorage.instance().save(context, "phoneNumber", response.e164)
                    LocalStorage.instance().save(context, "phoneNumberOnly", phoneNumber)
                    LocalStorage.instance().save(context, "idUser", response.idUser.toString())
                    LocalStorage.instance().save(context, "uid", response.uid)
                    supportEmail = response.supportEmail
                    if (response.idCountry != null) LocalStorage.instance()
                        .save(context, "idCountry", response.idCountry.toString())
                    if (response.verificationType == RequestActivationResponseBean.VerificationType.SMS) {
                        changeState("activate")
                    } else {
                        activateEddressUsingCode("-1")
                    }
                } as Response.Listener<RequestActivationResponseBean>).post()
    }

    private fun bottomTextClick() {
        when (currentState) {
            "login" -> {
                if (phoneNumberField.text.toString().isEmpty()) {
                    Utilities.hideKeyboard(this@LoginActivity)
                    Utilities.Toast(getString(R.string.msg_enter_phone_number))
                    return
                }
                changeState("activate")
            }
            "activate" -> sendMailNotReceivingCode()
            "profile" -> {
            }
            else -> {
            }
        }
    }

    private fun sendMailNotReceivingCode() {
        Utilities.hideKeyboard(this@LoginActivity)
        runOnUiThread {
            val alert = NokAlertDialog(this@LoginActivity, NokAlertDialog.SUCCESS)
            alert.setTitle(getString(R.string.title_code_not_received))
            alert.setMessage(getString(R.string.msg_delay_issue))
            alert.setConfirmText(getString(R.string.text_request_by_mail))
            alert.setCancelText(getString(R.string.i_will_wait))
            alert.setCancelable(true)
            alert.setConfirmClickListener {
                val i = Intent(Intent.ACTION_SEND)
                i.type = "message/rfc822"
                i.putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_i_didnot_receive_code))
                i.putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.prefix_phone) + Utilities.pickedCountry.phoneCode + phoneNumberField.text.toString()
                )
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.title_chooser_send_mail)))
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(this@LoginActivity, getString(R.string.msg_no_mail_installed), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            alert.show()
        }
    }

    fun changeState(state: String?) {
        var state1 = state
        if (state1 == null) state1 = "login"

        activationCodeField.visibility = View.GONE
        phoneNumberLayout.visibility = View.GONE
        when (state1) {
            "login" -> {
                titleText.text = getString(R.string.title_sign_in)
                descriptionText.text = getString(R.string.error_enter_phone_to_receive_code)
                phoneNumberLayout.visibility = View.VISIBLE
                phoneNumberLayout.requestFocus()
                emailLayout.visibility = View.GONE
                nameLayout.visibility = View.GONE
                bottomText.visibility = View.GONE
                bottomText.text = getString(R.string.already_have_code)
            }
            "activate" -> {
                titleText.text = getString(R.string.activation_code)
                activationCodeField.visibility = View.VISIBLE
                activationCodeField.requestFocus()
                descriptionText.text = getString(R.string.msg_enter_activation_code)
                bottomText.visibility = View.VISIBLE
                emailLayout.visibility = View.GONE
                nameLayout.visibility = View.GONE
                bottomText.text = getString(R.string.msg_i_didnot_receive_code)
            }
            "profile" -> {
                descriptionText.text = getString(R.string.msg_enter_details)
                titleText.text = getString(R.string.title_details)
                emailLayout.visibility = View.VISIBLE
                nameLayout.visibility = View.VISIBLE
                fullNameField.requestFocus()
                activationCodeField.visibility = View.GONE
                phoneNumberLayout.visibility = View.GONE
                bottomText.visibility = View.GONE
            }
            else -> {
            }
        }
        currentState = state1
    }

    override fun onBackPressed() {
        when (currentState) {
            "login" -> {
            }
            "activate" -> {
                changeState("login")
                return
            }
            "profile" -> {
            }
        }
        super.onBackPressed()
    }

    fun nextButton() {
        when (currentState) {
            "login" -> requestCode()
            "activate" -> activateEddressUsingCode(activationCodeField.text.toString().trim { it <= ' ' })
            "profile" -> updateProfileInfoToServer()
        }
    }

    fun activateEddressUsingCode(activationCode: String) {
        val idCountry = LocalStorage.instance().getString("idCountry")
        val phoneNumber = LocalStorage.instance().getString("phoneNumber")
        val idUser = LocalStorage.instance().getString("idUser")

//        String activationCode = activationCodeField.getText().toString().trim();
        if (activationCode.isNotEmpty()) {
            val params = Json.param()
            params.p("idCountry", idCountry)
            params.p("deviceType", "1")
            params.p("e164", phoneNumber)
            params.p("idUser", idUser)
            params.p("pushToken", LocalStorage.instance().getString("token"))
            params.p("isDriver", "0")
            params.p("activationCode", activationCode)
            params.p("os", "android")
            params.p("appName", getString(R.string.application))
            if (model.differedLink != null) {
                /* DifferedLink link = model.differedLink;

                if (Utilities.notEmpty(link.branchIdentifier)) {
                    params.p("branchIdentifier", link.branchIdentifier);
                }

                if (Utilities.notEmpty(link.promoCode)) {
                    params.p("promoCode", link.promoCode);
                }*/
                if (Utilities.notEmpty(getStringValue("referringUserUid"))) {
                    params.p("referringUserUid", getStringValue("referringUserUid"))
                }
                params.p("referringBonus", getStringValue("referringBonus"))


                /* if (Utilities.notEmpty(getStringValue("bannerUid"))) {
                    params.p("bannerUid", getStringValue("bannerUid"));
                }*/

//                params.p("channel", link.channel);
//                params.p("linkTitle", link.linkTitle);
//                params.p("tags", link.tags);
            }
            Rest.request().uri("activateUser/").userApi().params(params.toJson())
                .showLoader(getString(R.string.activateing_account)).response(
                    ActivateUserResponseBean::class.java, Response.Listener { response: ActivateUserResponseBean ->
                        activationCompleted()
                        LocalStorage.instance().save(context, "id", response.idUser.toString())
                        LocalStorage.instance().save(context, "uid", response.uid)
                        LocalStorage.instance().save(context, "phoneNumber", response.phoneNumber)
                        LocalStorage.instance().save(context, "e164", response.phoneNumber)
                        LocalStorage.instance().save(context, "login", true)
                        LocalStorage.instance().save(context, "fullName", response.fullName)
                        LocalStorage.instance().save(context, "profilePic", response.imageUrl)
                        LocalStorage.instance().save(context, "gender", response.gender)
                        LocalStorage.instance().save(context, "birthdate", response.birthdate)
                        LocalStorage.instance().save(context, "email", response.email)
                        LocalStorage.instance().save(context, "idCountry", idCountry)
                        LocalStorage.instance().save(context, "eddressRequestNotification", true)
                        LocalStorage.instance().save(context, "contactJoinedNotification", true)
                        LocalStorage.instance().save(context, "showTutorial", true)
                        LocalStorage.instance().save(context, "jwtToken", response.jwtToken)
                        LocalStorage.instance().clear(context, "referringUserUid")
                        LocalStorage.instance().clear(context, "referringBonus")
                        LocalStorage.instance().clear(context, "bannerUid")
                        Utilities.jwtToken = response.jwtToken
                        if (Utilities.notEmpty(response.fullName) && Utilities.notEmpty(response.email)) {
                            fullNameField.setText(response.fullName)
                            emailField.setText(response.email)
                            identify(response.uid, response.phoneNumber!!, response.fullName!!, response.email!!)
                            activateQuickly()
                        } else {
                            register(response.uid!!, response.phoneNumber!!)
                            changeState("profile")
                        }
                        Utilities.loginCrashlytics(context)
                    } as Response.Listener<ActivateUserResponseBean>).post()
        }
    }

    private fun activateQuickly() {
        Utilities.hideKeyboard(this)
        saveValue("finishedWelcome", true)
        Services.call(this@LoginActivity).result(object : ServiceResult<Any?> {
            override fun onResult(result: Any?) {
                if (model.currentAddress == null) {
                    ViewRouter.instance().changeYourLocation(this@LoginActivity, true)
                }
                finish()
            }

            override fun onError(errorResource: ErrorResource) {}
        }).checkCredibility(context)
    }

    private fun updateProfileInfoToServer() {
        val fullName = fullNameField.text.toString().trim { it <= ' ' }
        val email = emailField.text.toString().trim { it <= ' ' }
        val shake = AnimationUtils.loadAnimation(this@LoginActivity, R.anim.shake)
        Utilities.hideKeyboard(this)
        if (fullName.isEmpty()) {
            fullNameField.startAnimation(shake)
            Toast.makeText(context, getString(R.string.enter_full_name), Toast.LENGTH_LONG).show()
            return
        }
        if (email.isEmpty()) {
            emailField.startAnimation(shake)
            Toast.makeText(context, getString(R.string.enter_your_email), Toast.LENGTH_LONG).show()
            return
        }
        if (!Utilities.isValidEmail(email)) {
            emailField.startAnimation(shake)
            Toast.makeText(context, getString(R.string.enter_valid_email), Toast.LENGTH_LONG).show()
            return
        }
        Rest.request().uri("updateFullName/").params(Json.param().p("fullName", fullName).p("email", email).toJson())
            .showLoader(
                getString(
                    R.string.updating
                )
            ).response(
                ResponseBean::class.java, Response.Listener { _: ResponseBean? ->
                    LocalStorage.instance().save(context, "finishedWelcome", true)
                    LocalStorage.instance().save(context, "fullName", fullName)
                    LocalStorage.instance().save(context, "email", email)
                    identify(
                        LocalStorage.instance().getString("uid"),
                        LocalStorage.instance().getString("phoneNumber"),
                        fullName,
                        email
                    )
                    activateQuickly()
                }).post()
    }

    private var smsReceiverStarted = false
    private var receiver: MySMSBroadcastReceiver? = null
    private fun startSmsReceiver() {
        if (smsReceiverStarted) return
        smsReceiverStarted = true
        receiver = MySMSBroadcastReceiver()
        registerReceiver(receiver, IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION))
        val client = SmsRetriever.getClient(this /* context */)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener { }
        task.addOnFailureListener { }
    }

    override fun onConnected(bundle: Bundle?) {
        if (currentState == "login" && phoneNumberField.text.toString().isEmpty()) requestHint()
    }

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    override fun onDestroy() {
        super.onDestroy()
        if (mGoogleApiClient?.isConnected == true) mGoogleApiClient?.disconnect()
        mGoogleApiClient = null
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    /**
     * BroadcastReceiver to wait for SMS messages. This can be registered either
     * in the AndroidManifest or at runtime.  Should filter Intents on
     * SmsRetriever.SMS_RETRIEVED_ACTION.
     */
    inner class MySMSBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras!!
                val status = (extras[SmsRetriever.EXTRA_STATUS] as Status?)!!
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get SMS message contents
                        val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE]!! as String
                        val matcher = Pattern.compile("\\d+").matcher(message)
                        matcher.find()
                        val code = Integer.valueOf(matcher.group())
                        if (code > 0) {
                            activationCodeField.setText(code.toString())
                            activationCodeField.isEnabled = false
                            activateEddressUsingCode(code.toString())
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                    }
                }
            }
        }
    }

    companion object {
        @JvmField
        var active = false
        private const val RESOLVE_HINT = 1345
    }
}
