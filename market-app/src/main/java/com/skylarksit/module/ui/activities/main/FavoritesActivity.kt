package com.skylarksit.module.ui.activities.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.skylarksit.module.R
import com.skylarksit.module.ui.activities.hyperlocal.FavoritesPageObjectFragment
import com.skylarksit.module.utils.MyAppCompatActivity
import com.skylarksit.module.utils.SlidingTabLayout
import com.skylarksit.module.utils.SlidingTabPageAdapter
import com.skylarksit.module.utils.Utilities

class FavoritesActivity : MyAppCompatActivity() {

    lateinit var favoritesPager: ViewPager
    lateinit var favoritesTabs: SlidingTabLayout
    var favoritesCurrentPage = 0
    lateinit var favoritesEmpty: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.favorites_layout)
        initFavoritesView()
        refreshFavorites()
    }

    private fun initFavoritesView() {
        favoritesTabs = findViewById(R.id.sliding_tabs)
        favoritesPager = findViewById(R.id.favorites_pager)
        favoritesEmpty = findViewById(R.id.favoritesEmpty)
        updateCart()
        favoritesPagerAdapter = FavoritesPagerAdapter(supportFragmentManager)
        favoritesPager.adapter = favoritesPagerAdapter
        favoritesPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                favoritesCurrentPage = position
                favoritesPagerAdapter!!.updateFragmentAtIndex(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        favoritesTabs.setCustomTabView(R.layout.sliding_tab_view_images, R.id.textView, R.id.imageView)
        favoritesTabs.setTabImageViewHidden(true)
        favoritesTabs.setViewPager(favoritesPager)
    }

    var favoritesPagerAdapter: FavoritesPagerAdapter? = null

    inner class FavoritesPagerAdapter internal constructor(fm: FragmentManager?) : SlidingTabPageAdapter(fm) {
        override fun getItem(i: Int): Fragment {
            val fragment: Fragment = FavoritesPageObjectFragment()
            val args = Bundle()
            args.putInt(FavoritesPageObjectFragment.ARG_OBJECT, i)
            val service = model.favorites[i]
            args.putString("activeService", service.slug)
            fragment.arguments = args
            return fragment
        }

        override fun getCount(): Int {
            return model.favorites.size
        }

        override fun getImageUrl(position: Int): String? {
            return null
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val service = model.favorites[position]
            return service.name
        }

        fun updateFragmentAtIndex(index: Int) {
            val fragment = getItem(index) as FavoritesPageObjectFragment
            fragment.refreshView()
        }
    }

    private fun refreshFavorites() {
        model.buildFavorites()
        favoritesPagerAdapter!!.notifyDataSetChanged()
        if (model.favoritesTabsModified) {
            favoritesPager.adapter = favoritesPagerAdapter
            favoritesTabs.setViewPager(favoritesPager)
            if (favoritesCurrentPage < favoritesPagerAdapter!!.count) {
                favoritesPager.currentItem = favoritesCurrentPage
            }
        } else if (model.favoritesViewModified) {
            for (i in 0 until favoritesPagerAdapter!!.count) {
                (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + i) as FavoritesPageObjectFragment?)?.refreshView()
            }
        }
        model.favoritesTabsModified = false
        model.favoritesViewModified = false
        favoritesEmpty.visibility = if (model.favorites.size == 0) View.VISIBLE else View.GONE
        if (Utilities.isMarketplace()) {
            favoritesTabs.visibility = View.VISIBLE
        } else {
            favoritesTabs.visibility = View.GONE
        }
    }

    override fun onResume() {
        updateCart()
        refreshFavorites()
        super.onResume()
    }

    fun updateCart() {

    }
}
