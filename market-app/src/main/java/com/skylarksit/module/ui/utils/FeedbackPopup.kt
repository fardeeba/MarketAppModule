package com.skylarksit.module.ui.utils

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.skylarksit.module.R
import com.skylarksit.module.ui.model.ServicesModel
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.Utilities

class FeedbackPopup : MyAppCompatActivity() {

    lateinit var image: ImageView
    lateinit var text: TextView
    lateinit var actionButton: Button
    lateinit var editText: EditText
    private lateinit var ratingBar: RatingBar
    private var rating: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feedback_layout)
        setFinishOnTouchOutside(false)
        image = findViewById(R.id.image)
        text = findViewById(R.id.text)
        actionButton = findViewById(R.id.actionButton)
        editText = findViewById(R.id.editText)
        ratingBar = findViewById(R.id.ratingBar)
        actionButton.setOnClickListener { submitFeedback() }
        rating = intent.getStringExtra("rating")
        val feedbackType = intent.getSerializableExtra("feedbackType") as FeedbackType?
        if (rating != null) {
            val index = rating!!.toInt()
            var feedbackText = ""
            val servicesModel = ServicesModel.instance()
            when (index) {
                1 -> feedbackText = servicesModel.feedbackText[0]
                2 -> feedbackText = servicesModel.feedbackText[1]
                3 -> feedbackText = servicesModel.feedbackText[2]
                4 -> feedbackText = servicesModel.feedbackText[3]
            }
            text.text = feedbackText
            if (feedbackType == FeedbackType.FEEDBACK_TYPE_DELIVERY) {
                ratingBar.visibility = View.GONE
                when (rating) {
                    "1" -> image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.s1))
                    "2" -> image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.s2))
                    "3" -> image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.s3))
                    "4" -> image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.s4))
                }
            } else {
                image.visibility = View.GONE
                ratingBar.rating = rating!!.toFloat()
            }
        }
    }

    private fun submitFeedback() {
        try {
            val rate = Integer.valueOf(rating!!)
            val text = editText.text.toString().trim { it <= ' ' }
            if (rate <= 2 && text.isEmpty()) {
                Utilities.Toast(getString(R.string.please_enter_some_comments))
                return
            }
            val data = Intent()
            data.putExtra("rating", rating)
            data.putExtra("feedbackMessage", text)
            setResult(RESULT_OK, data)
        } catch (ignored: Exception) {
        }
        finish()
    }
}
