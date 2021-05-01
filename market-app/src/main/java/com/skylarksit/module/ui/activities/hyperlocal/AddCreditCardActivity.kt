package com.skylarksit.module.ui.activities.hyperlocal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import co.paystack.android.Paystack.TransactionCallback
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.android.volley.Response
import com.skylarksit.module.R
import com.skylarksit.module.analytics.Segment.creditCardAdded
import com.skylarksit.module.lib.Rest
import com.skylarksit.module.pojos.services.CreditCardBean
import com.skylarksit.module.pojos.services.CreditCardParam
import com.skylarksit.module.ui.utils.LocalStorage
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities
import java.util.*

class AddCreditCardActivity : MyAppCompatActivity() {

    lateinit var cvvField: EditText
    lateinit var creditCardField: EditText
    lateinit var expiryField: EditText
    lateinit var addCreditCard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)
        cvvField = findViewById(R.id.cvv)
        creditCardField = findViewById(R.id.creditCardField)
        expiryField = findViewById(R.id.expiryField)
        addCreditCard = findViewById(R.id.addCreditCard)
        if (Utilities.checkoutProdKey == null) {
            return
        }
        expiryField.setOnKeyListener { _: View?, _: Int, _: KeyEvent? ->
            expiryField.setSelection(expiryField.text.toString().length)
            false
        }
        addCreditCard.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                addCard()
            }
        })
        expiryField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                if (count != 0) {
                    var text = expiryField.text.toString()
                    text = text.replace("[^0-9]+".toRegex(), "/")
                    val firstLetter = text.substring(0, 1)
                    if (!firstLetter.equals("0", ignoreCase = true) && !firstLetter.equals("1", ignoreCase = true)) {
                        text = try {
                            "0$firstLetter"
                        } catch (e: Exception) {
                            ""
                        }
                    }
                    if (text.length == 2 && !text.contains("/")) {
                        text = "$text/"
                    }
                    if (text.indexOf("/") == 1) {
                        text = "0$text"
                    }
                    if (text.length > 5) {
                        text = text.substring(0, 5)
                    }
                    if (text.length >= 2) {
                        val secondLetter = text.substring(1, 2)
                        if (firstLetter.equals("1", ignoreCase = true)) {
                            if (!secondLetter.equals("0", ignoreCase = true) && !secondLetter.equals(
                                    "1",
                                    ignoreCase = true
                                ) && !secondLetter.equals("2", ignoreCase = true)
                            ) {
                                text = text[0].toString() + "2/"
                            }
                        }
                    }
                    if (expiryField.text.toString().compareTo(text, ignoreCase = true) != 0) {
                        expiryField.setText(text)
                        expiryField.setSelection(text.length)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        creditCardField.setOnKeyListener { _: View?, _: Int, _: KeyEvent? ->
            creditCardField.setSelection(creditCardField.text.toString().length)
            false
        }
        creditCardField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (count != 0) {
                    var text = creditCardField.text.toString()
                    text = text.replace("[^0-9]+".toRegex(), " ")
                    when {
                        text.length == 5 -> {
                            text = text.substring(0, 4) + " " + text.substring(4)
                        }
                        text.length == 10 -> {
                            text = text.substring(0, 9) + " " + text.substring(9)
                        }
                        text.length == 15 -> {
                            text = text.substring(0, 14) + " " + text.substring(14)
                        }
                        text.length > 19 -> {
                            text = text.substring(0, 19)
                        }
                    }
                    if (creditCardField.text.toString().compareTo(text, ignoreCase = true) != 0) {
                        creditCardField.setText(text)
                        creditCardField.setSelection(text.length)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun addCard() {
        val email = getStringValue("email")
        var cardNumber = creditCardField.text.toString()
        var expiryDate = expiryField.text.toString()
        val cvv = cvvField.text.toString()
        cardNumber = cardNumber.replace(" ".toRegex(), "")
        cardNumber = cardNumber.replace("-".toRegex(), "")
        expiryDate = expiryDate.replace(" ".toRegex(), "/")
        expiryDate = expiryDate.replace("-".toRegex(), "/")
        if (expiryDate.length == 3) {
            expiryDate = "0$expiryDate"
        }
        if (expiryDate.length == 4) {
            expiryDate = expiryDate.substring(0, 2) + "/" + expiryDate.substring(2)
        }
        if (!isCardInfoValid(cardNumber, expiryDate, cvv)) {
            return
        }
        val expiryArray = expiryDate.split("/".toRegex()).toTypedArray()
        var month = expiryArray[0]
        if (month.length == 1) {
            month = "0$month"
        }
        var year = expiryArray[1]
        if (year.length == 4) {
            year = year.substring(2)
        }
        val param = CreditCardParam()
        param.name = Utilities.getMyName()
        param.email = email
        param.expiryMonth = Integer.valueOf(month)
        param.expiryYear = Integer.valueOf(year)
        val finalCardNumber = cardNumber
        Utilities.showSpinner("Validating credit card...")
        if ("Paystack".equals(model.paymentGateway, ignoreCase = true)) {
            param.cardNumber = finalCardNumber
            param.cvv = cvv
            chargeCreditCardToPaystack(param, finalCardNumber, cvv)
        }
    }

    private fun chargeCreditCardToPaystack(params: CreditCardParam, finalCardNumber: String, cvv: String) {
        PaystackSdk.setPublicKey(Utilities.checkoutProdKey)
        val email = LocalStorage.instance().getString("email")
        if (Utilities.isEmpty(email)) {
            Utilities.Error("Error", "Unable to save credit card. Please complete your profile first")
        }
        val paystackCard = Card(
            finalCardNumber,
            params.expiryMonth, params.expiryYear, cvv
        )
        if (paystackCard.isValid) {

            //create a Charge object
            val charge = Charge()
            charge.card = paystackCard //sets the card to charge
            charge.amount = 1000
            charge.email = email
            PaystackSdk.chargeCard(this, charge, object : TransactionCallback {
                override fun onSuccess(transaction: Transaction) {
                    // This is called only after transaction is deemed successful.
                    // Retrieve the transaction, and send its reference to your server
                    // for verification.
                    params.token = transaction.reference
                    saveCreditCardToEddressServer(params)
                }

                override fun beforeValidate(transaction: Transaction) {
                    // This is called only before requesting OTP.
                    // Save reference so you may send to server. If
                    // error occurs with OTP, you should still verify on server.
                }

                override fun onError(error: Throwable, transaction: Transaction) {
                    //handle error here
                    Utilities.hideSpinner()
                    Log.e("error", error.toString() + " " + error.javaClass)
                    Utilities.Error("Error", error.message)
                }
            })
        } else {
            //handle error here
            Utilities.Error("Error", "Unable to save credit card")
        }
    }

    fun saveCreditCardToEddressServer(param: CreditCardParam?) {
        Rest.request().uri("addCreditCard").params(param).showLoader("Saving Credit Card...")
            .response(CreditCardBean::class.java, Response.Listener { response: CreditCardBean ->
                if (Utilities.notEmpty(response.uid)) {
                    model.creditCards.add(response)
                    creditCardAdded()
                    finish()
                } else {
                    Utilities.Error("Error", "Unable to save credit card")
                }
            } as Response.Listener<CreditCardBean>).post()
    }

    private fun isCardInfoValid(cardNumber: String, expiryDate: String, cvv: String): Boolean {
        if (!cardNumber.matches("[0-9]+".toRegex())) {
            Toast.makeText(context, "Wrong card number", Toast.LENGTH_SHORT).show()
            return false
        }
        val expiryArray = expiryDate.split("/".toRegex()).toTypedArray()
        if (expiryArray.size != 2) {
            Toast.makeText(context, "Wrong expiry date", Toast.LENGTH_SHORT).show()
            return false
        }
        var month = expiryArray[0]
        if (month.length == 1) {
            month = "0$month"
        }
        if (!month.matches("[0-9]+".toRegex()) || month.length != 2) {
            Toast.makeText(context, "Wrong expiry month", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            val monthValue = month.toInt()
            if (monthValue < 1 || monthValue > 12) {
                Toast.makeText(context, "Wrong expiry month", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Wrong expiry month", Toast.LENGTH_SHORT).show()
            return false
        }
        var year = expiryArray[1]
        if (year.length == 4) {
            year = year.substring(2)
        }
        if (!year.matches("[0-9]+".toRegex()) || year.length != 2) {
            Toast.makeText(context, "Wrong expiry year", Toast.LENGTH_SHORT).show()
            return false
        }
        try {
            val yearValue = year.toInt()
            val minYear = Calendar.getInstance()[Calendar.YEAR] % 1000
            if (yearValue < minYear || yearValue > minYear + 10) {
                Toast.makeText(context, "Wrong expiry year", Toast.LENGTH_SHORT).show()
                return false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Wrong expiry year", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!cvv.matches("[0-9]+".toRegex()) || cvv.length < 3) {
            Toast.makeText(context, "Wrong CVV", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
