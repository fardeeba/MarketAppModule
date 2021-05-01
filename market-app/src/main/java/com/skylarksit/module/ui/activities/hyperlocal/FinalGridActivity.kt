@file:Suppress("DEPRECATION")

package com.skylarksit.module.ui.activities.hyperlocal

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.facebook.drawee.view.SimpleDraweeView
import com.skylarksit.module.INavBarActivity
import com.skylarksit.module.R
import com.skylarksit.module.libs.alertdialog.NokListAlertDialog
import com.skylarksit.module.libs.alertdialog.ProviderDialog
import com.skylarksit.module.pojos.CategoryViewType
import com.skylarksit.module.pojos.services.HomePageCategoryBean
import com.skylarksit.module.pojos.services.MenuCategoryObject
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.components.NavigationBar
import com.skylarksit.module.ui.lists.main.adapters.SubcategoriesAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.HFRecyclerView
import com.skylarksit.module.ui.utils.IClickListener
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.ui.utils.OnOneOffClickListener
import com.skylarksit.module.utils.*
import java.text.NumberFormat
import java.util.*
import android.util.DisplayMetrics
import androidx.core.widget.NestedScrollView


class FinalGridActivity : MyAppCompatActivity(), INavBarActivity {

    lateinit var tabs: SlidingTabLayout
    lateinit var subcategoriesList: RecyclerView
    var subcategoryList: MutableList<MenuCategoryObject> = ArrayList()
    var activeMenuCategory: MenuCategoryObject? = null
    var initHeaderY = 0f
    private var totalHeaderCal = 0
    private lateinit var openingHoursText: TextView
    private lateinit var etaLabel: TextView
    private lateinit var imageBox: RelativeLayout
    private lateinit var categoriesImage: SimpleDraweeView
    var subCatListener: IClickListener<MenuCategoryObject>? = null
    var pagerList = ArrayList<MenuCategoryObject>()
    var categoriesHaveImages = false
    var fixedHeader = false
    var stickyHeaderHeight = 0f
    private lateinit var pager: ViewPager
    lateinit var headerView: RelativeLayout
    lateinit var header: RelativeLayout
    private lateinit var titleBar: RelativeLayout
    private lateinit var titleText: TextView
    private lateinit var titleTextLayout: View
    lateinit var navigationBar: NavigationBar
    private lateinit var formatter: NumberFormat
    private lateinit var searchLayout: View
    private lateinit var subcategoriesButton: RelativeLayout
    var activeService: ServiceObject? = null
    var subcategoriesAdapter: SubcategoriesAdapter? = null
    private var searchFragment: MainSearchFragment? = null
    var previousPage = 0
    var pagerAdapter: PagerAdapter? = null
    private var headerHeight = 0
    private var subcategoriesHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_grid)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setLightStatusBar(this)
        }


        tabs = findViewById(R.id.sliding_tabs)
        subcategoriesList = findViewById(R.id.subcategoriesList)
        pager = findViewById(R.id.pager)
        headerView = findViewById(R.id.headerView)
        header = findViewById(R.id.header)
        titleBar = findViewById(R.id.titleBar)
        titleText = findViewById(R.id.titleText)
        titleTextLayout = findViewById(R.id.titleTextLayout)
        navigationBar = findViewById(R.id.navigationBar)
        searchLayout = findViewById(R.id.searchLayout)
        subcategoriesButton = findViewById(R.id.subcategoriesButton)

        subcategoriesButton.setOnClickListener { displaySubCategoriesPopup() }

        categoriesHaveImages = context.resources.getBoolean(R.bool.categoriesHaveImages)
        val showCategories = context.resources.getBoolean(R.bool.showCategories)
        pagerList.clear()
        header.visibility = if (Utilities.isMarketplace()) View.VISIBLE else View.GONE
        val slug = intent.getStringExtra("activeService")
        activeService = model.findProviderByName(slug)
        if (activeService == null) {
            return
        }
        if (model.singleStore) {
            if (showCategories) {
                fixedHeader = false
                titleBar.visibility = View.GONE
                pagerList.addAll(activeService!!.categories)
                subcategoriesList.visibility = View.VISIBLE
                tabs.visibility = if (activeService!!.hasCategories()) View.VISIBLE else View.GONE
            } else {
                fixedHeader = true
                titleBar.visibility = View.VISIBLE
                titleText.text = model.activeCategory.getLabel()
                titleTextLayout.setOnClickListener { displayCategoriesPopup() }
                if (activeService!!.categoryViewType === CategoryViewType.SCROLL) {
                    tabs.visibility = View.GONE
                    subcategoriesList.visibility = View.VISIBLE
                    pagerList.add(model.activeCategory)
                } else if (activeService!!.categoryViewType === CategoryViewType.SWIPE) {
                    tabs.visibility = View.VISIBLE
                    subcategoriesList.visibility = View.GONE
                    pagerList.addAll(model.activeCategory.subcategories)
                }
            }
        } else {
            fixedHeader = false
            titleBar.visibility = View.GONE
            pagerList.addAll(activeService!!.categories)
            subcategoriesList.visibility = View.VISIBLE
            tabs.visibility = if (activeService!!.hasCategories()) View.VISIBLE else View.GONE
        }
        formatter = Utilities.getCurrencyFormatter(activeService!!.currency)
        if (categoriesHaveImages) {
            tabs.layoutParams.height = Utilities.dpToPx(92)
        } else {
            tabs.layoutParams.height = Utilities.dpToPx(52)
        }
        initShortcutList()
        initializeHeader()
        initPromo()
        updateCart()
        headerView.post {
            initHeaderY = headerView.y
            headerHeight = headerView.height
            subcategoriesHeight = subcategoriesList.height
            val tabsHeight = tabs.height
            totalHeaderCal = headerHeight - subcategoriesHeight - tabsHeight
            stickyHeaderHeight =
                if (categoriesHaveImages) (totalHeaderCal + Utilities.dpToPx(50)).toFloat() else totalHeaderCal.toFloat()
            createGridView()
        }
        val dimen = resources.getDimension(R.dimen.toolbarElevation)
        headerView.elevation = if (model.singleStore) dimen else 0f
        navigationBar.init(this, activeService)
    }

    private fun getCurrentRecyclerView(): RecyclerView? {
        return if (getCurrentView() == null) null else getCurrentView()!!.findViewById(R.id.recyclerGridView)
    }

    private fun displayCategoriesPopup() {
        val alertDialog = NokListAlertDialog<MenuCategoryObject>(this@FinalGridActivity, R.layout.list_category_item)
        alertDialog.setCancelable(true)
        val items: MutableList<IListItem> = ArrayList()
        for (`object` in activeService!!.getCategories()) {
            if (Utilities.isEmpty(`object`.getImageUrl())) `object`.setImageUrl(
                model.getCategoryImageFromHomePageItems(
                    `object`.label
                )
            )
            `object`.viewType = HFRecyclerView.TYPE_ITEM
            items.add(`object`)
        }
        items.sortWith { t1: IListItem, t2: IListItem -> t1.label.compareTo(t2.label) }
        alertDialog.setItems(items)
        alertDialog.setItemClickListener { item: MenuCategoryObject ->
            alertDialog.hide()
            ViewRouter.instance()
                .goToServiceProvider(this@FinalGridActivity, activeService!!.slug, item.label, null, null)
            finish()
        }
        alertDialog.show()
    }

    private fun displaySubCategoriesPopup() {
        val alertDialog = NokListAlertDialog<MenuCategoryObject>(this@FinalGridActivity, R.layout.list_subcategory_item)
        alertDialog.setCancelable(true)
        val items: MutableList<IListItem> = ArrayList()
        for (`object` in subcategoryList) {
            if (!activeService!!.hideImages) {
                val firstObject = `object`.getItems()[0]
                var imageUrl = firstObject.getThumbnailUrl()
                if (Utilities.isEmpty(imageUrl)) imageUrl = firstObject.getImageUrl()
                `object`.setImageUrl(imageUrl)
            }
            `object`.viewType = HFRecyclerView.TYPE_ITEM
            items.add(`object`)
        }
        items.sortWith { t1: IListItem, t2: IListItem -> t1.label.compareTo(t2.label) }
        alertDialog.setItems(items)
        alertDialog.setItemClickListener { obj: MenuCategoryObject ->
            alertDialog.hide()
            val currentView = getCurrentView()
            val recyclerView: RecyclerView = currentView!!.findViewById(R.id.recyclerGridView)
            val adapter = recyclerView.adapter as HFRecyclerView<*>?
            val firstItem = obj.getItems()[0]
            assert(adapter != null)
            val index = adapter!!.data.indexOf(firstItem)
            if (subCatListener != null) {
                subCatListener!!.click(obj)
            }
            recyclerView.smoothScrollToPosition(index - 1)
        }
        alertDialog.show()
    }

    private fun initializeHeader() {
        etaLabel = findViewById(R.id.etaLabel)
        openingHoursText = findViewById(R.id.openingHours)
        imageBox = findViewById(R.id.imageBox)
        categoriesImage = findViewById(R.id.serviceImage)
        val textBox = findViewById<View>(R.id.textBox)
        textBox.setOnClickListener(object : OnOneOffClickListener() {
            override fun onSingleClick(v: View) {
                ProviderDialog<Any?>(this@FinalGridActivity, activeService).show()
            }
        })
        if (model.closeMerchants || model.isClosed) {
            openingHoursText.visibility = View.GONE
            etaLabel.text = getString(R.string.store_is_currently_closed)
        } else {
            openingHoursText.text = activeService!!.getOpeningHours()
            etaLabel.text = activeService!!.etaText
        }
        categoriesImage.setImageURI(U.parse(activeService!!.backgroundImageUrl))
    }

    fun updateCart() {
        navigationBar.refreshView()

        if (searchFragment != null) {
            searchFragment!!.searchAdapter.notifyDataSetChanged()
        }
    }

    private fun initPromo() {
        val promoCode = intent.getStringExtra("promoCode")
        if (promoCode != null && promoCode.isNotEmpty()) {
            model.applyPromo(this, promoCode, activeService!!.serviceProviderUid, true)
        }
    }

    fun getCurrentView(): View? {
        return pager.findViewWithTag(pager.currentItem)
    }

    private fun initShortcutList() {
        activeMenuCategory = pagerList[0]
        subcategoryList.clear()
        subcategoriesAdapter = SubcategoriesAdapter(context, subcategoryList)
        subcategoriesAdapter!!.setClickListener(object : ItemClickListener<MenuCategoryObject>(subcategoriesAdapter!!) {
            override fun onClick(item: MenuCategoryObject, position: Int) {
                val currentView = getCurrentView()
                val recyclerView: RecyclerView = currentView!!.findViewById(R.id.recyclerGridView)
                val adapter = recyclerView.adapter as HFRecyclerView<*>?
                val firstItem = item.getItems()[0]
                val index = adapter!!.data.indexOf(firstItem)
                if (subCatListener != null) {
                    subCatListener!!.click(item)
                }

                //recyclerView.smoothScrollToPosition(index - 1)
                recyclerView.betterSmoothScrollToPosition(index-1)
            }
        })
        if (activeMenuCategory!!.getSubcategories() != null) {
            subcategoryList.addAll(activeMenuCategory!!.getSubcategories())
            subcategoriesList.adapter = subcategoriesAdapter
            subcategoriesList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }
    }


    fun RecyclerView.betterSmoothScrollToPosition(targetItem: Int) {
        layoutManager?.apply {
            val maxScroll = 10
            when (this) {
                is LinearLayoutManager -> {
                    val topItem = findFirstVisibleItemPosition()
                    val distance = topItem - targetItem
                    val anchorItem = when {
                        distance > maxScroll -> targetItem + maxScroll
                        distance < -maxScroll -> targetItem - maxScroll
                        else -> topItem
                    }
                    if (anchorItem != topItem) scrollToPosition(anchorItem)
                    post {
                        smoothScrollToPosition(targetItem)
                    }
                }
                else -> smoothScrollToPosition(targetItem)
            }
        }
    }

    //    @OnClick(R2.id.bottomBar)
    //    public void onNextButtonClick(View view) {
    //
    //        Utilities.hideKeyboard(this);
    //
    //        if (model.subtotalPrice > 0) {
    //            ViewRouter.instance().goToBasket(FinalGridActivity.this, activeService, null);
    //        } else {
    //            Toast.makeText(context, "Please pick at least one item", Toast.LENGTH_SHORT).show();
    //        }
    //
    //    }
    override fun onBackPressed() {
        if (!isFinishing) {
            if (navigationBar.onClearSearch()) {
                return
            }
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        if (getBooleanValue("continueFinish")) {
            finish()
        }
        updateCart()
    }

    private fun createGridView() {
        previousPage = 0
        pagerAdapter = PagerAdapter(
            supportFragmentManager
        )
        pager.adapter = pagerAdapter
        pager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                activeMenuCategory = pagerList[position]
                if (!fixedHeader) {
                    for (i in 0 until pagerAdapter!!.count) {
                        (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + i) as PageObjectFragment?)?.removeScrollListeners()
                    }
                    val currentPage =
                        supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + position) as PageObjectFragment?
                    val totalHeight = (-headerView.y).toInt()
                    Objects.requireNonNull(currentPage)!!.removeScrollListeners()
                    if (currentPage!!.overallScrollY < stickyHeaderHeight) {
                        currentPage.recyclerView.post {
                            val scrollBy = totalHeight - currentPage.overallScrollY
                            currentPage.recyclerView.scrollBy(0, scrollBy)
                            currentPage.overallScrollY += scrollBy
                            currentPage.addScrollListeners()
                        }
                    } else {
                        currentPage.recyclerView.post { currentPage.addScrollListeners() }
                    }
                    previousPage = position
                    subcategoryList.clear()
                    if (activeMenuCategory!!.getSubcategories() != null) {
                        subcategoryList.addAll(activeMenuCategory!!.getSubcategories())
                        subcategoriesAdapter!!.notifyDataSetChanged()
                        if (activeMenuCategory!!.selectedSubcategory == null && subcategoryList.size > 0) activeMenuCategory!!.selectedSubcategory =
                            subcategoryList[0].label
                        currentPage.updateSubcategoryButton(activeMenuCategory!!.selectedSubcategory, 0)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        tabs.setCustomTabView(
            if (categoriesHaveImages) R.layout.sliding_tab_view_images else R.layout.sliding_tab_view,
            R.id.textView,
            R.id.imageView
        )
        tabs.setTabImageViewHidden(!categoriesHaveImages)
        tabs.setViewPager(pager)
        val pPage =
            (supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.pager + ":" + 0) as PageObjectFragment?)!!
        pPage.addScrollListeners()
        pager.post {
            if (model.goToItem != null) {
                @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING") val index =
                    pagerList.indexOf(model.goToItem.category)
                pager.setCurrentItem(index, false)
                val recyclerView = getCurrentRecyclerView()
                val adapter = recyclerView!!.adapter as HFRecyclerView<*>?
                val itemIndex = adapter!!.data.indexOf(model.goToItem)
                if (itemIndex > -1) {
                    recyclerView.post { recyclerView.smoothScrollToPosition(itemIndex) }
                }
                model.goToItem = null
            } else if (model.goToCategory != null) {
                var myCat: MenuCategoryObject? = null
                for (c in pagerList) {
                    if (c.getLabel().equals(model.goToCategory, ignoreCase = true)) {
                        myCat = c
                        break
                    }
                }
                if (myCat == null) return@post
                val index = pagerList.indexOf(myCat)
                pager.setCurrentItem(index, false)
                model.goToCategory = null
            }
        }
    }

    //    @OnClick(R2.id.homeIcon)
    //    public void backButtonClick(){
    //        saveValue("returnToMainView", true);
    //        finish();
    //    }
    //    public void hideCart() {
    //        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bottomBar.getLayoutParams();
    //        int fabBottomMargin = lp.bottomMargin;
    //        bottomBar.animate().translationY(bottomBar.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    //    }
    //    public void showCart() {
    //
    //        if (model.isCartUsed()) {
    //            bottomBar.setVisibility(View.VISIBLE);
    //            bottomBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    //        }
    //
    //    }
    override fun getSearchView(): View {
        return searchLayout
    }

    inner class PagerAdapter internal constructor(fm: FragmentManager?) : SlidingTabPageAdapter(fm) {
        override fun getItem(i: Int): Fragment {
            val fragment = PageObjectFragment()
            val args = Bundle()
            args.putInt(PageObjectFragment.ARG_OBJECT, i)
            args.putString("activeService", activeService!!.slug)
            fragment.arguments = args
            return fragment
        }

        override fun getCount(): Int {
            return pagerList.size
        }

        override fun getImageUrl(position: Int): String? {
            if (!categoriesHaveImages) {
                return null
            }
            val categoryService = pagerList[position]
            if (!Utilities.isEmpty(categoryService.imageUrl)) {
                return categoryService.imageUrl
            }
            var image = categoryService.items[0].imageUrl
            if (image == null) {
                for (item in model.homePageItems) {
                    if (item is HomePageCategoryBean) {
                        if (HomePageCategoryBean.Type.BLOCKS == item.type) {
                            for (itemBean in item.items) {
                                if (categoryService.label == itemBean.serviceCategory) {
                                    categoryService.imageUrl = itemBean.imageUrl
                                    return categoryService.imageUrl
                                }
                            }
                        }
                    }
                }
                image = categoryService.imageUrl
            }
            return image
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return pagerList[position].label
        }
    }
}
