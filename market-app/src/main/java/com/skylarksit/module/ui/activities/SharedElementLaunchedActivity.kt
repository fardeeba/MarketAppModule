package com.skylarksit.module.ui.activities

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import com.skylarksit.module.R
import com.skylarksit.module.ui.activities.hyperlocal.ProductActivity
import com.skylarksit.module.utils.U
import me.relex.photodraweeview.OnPhotoTapListener
import me.relex.photodraweeview.PhotoDraweeView

class SharedElementLaunchedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindowTransitions()
        setContentView(R.layout.activity_shared_element_launched)
        val draweeView = findViewById<PhotoDraweeView>(R.id.photo_drawee_view)
        ViewCompat.setTransitionName(draweeView, ProductActivity.SHARED_ELEMENT_NAME)
        val url = intent.getStringExtra("imageUrl")
        draweeView.setPhotoUri(U.parse(url))
        draweeView.onPhotoTapListener = OnPhotoTapListener { _: View?, _: Float, _: Float -> onBackPressed() }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initWindowTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            val transition = AutoTransition()
            window.sharedElementEnterTransition = transition
            window.sharedElementExitTransition = transition
            ActivityCompat.setEnterSharedElementCallback(this, object : SharedElementCallback() {
                override fun onSharedElementEnd(
                    sharedElementNames: List<String>,
                    sharedElements: List<View>, sharedElementSnapshots: List<View>
                ) {
                    for (view in sharedElements) {
                        if (view is PhotoDraweeView) {
                            view.setScale(1f, true)
                        }
                    }
                }
            })
        }
    }
}
