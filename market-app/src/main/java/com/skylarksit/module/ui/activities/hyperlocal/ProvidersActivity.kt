package com.skylarksit.module.ui.activities.hyperlocal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.skylarksit.module.INavBarActivity
import com.skylarksit.module.R
import com.skylarksit.module.pojos.CollectionData
import com.skylarksit.module.pojos.services.ServiceObject
import com.skylarksit.module.ui.components.NavigationBar
import com.skylarksit.module.ui.lists.hyperlocal.adapters.ProviderEntryAdapter
import com.skylarksit.module.ui.model.IListItem
import com.skylarksit.module.ui.model.ViewRouter
import com.skylarksit.module.ui.utils.ItemClickListener
import com.skylarksit.module.utils.MyAppCompatActivity
import java.util.*

class ProvidersActivity : MyAppCompatActivity(), INavBarActivity {
    lateinit var title: TextView
    lateinit var recyclerView: RecyclerView
    lateinit var searchLayout: View
    lateinit var navigationBar: NavigationBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_providers)
        title = findViewById(R.id.title)
        recyclerView = findViewById(R.id.recyclerView)
        searchLayout = findViewById(R.id.searchLayout)
        navigationBar = findViewById(R.id.navigationBar)
        val intent = intent
        val collectionDataString = intent.getStringExtra("collectionData")
        val collectionData = Gson().fromJson(collectionDataString, CollectionData::class.java)
        val array = intent.extras!!["services"] as Array<*>?
        val services = Arrays.copyOf(array, array!!.size, Array<String>::class.java)
        if (services.isEmpty()) return
        val providers: MutableList<IListItem> = ArrayList()
        for (serviceSlug in services) {
            val serviceObject = model.findProviderByName(serviceSlug)
            if (serviceObject != null && !providers.contains(serviceObject)) {
                providers.add(serviceObject)
            }
        }
        val label = intent.getStringExtra("label")
        title.text = label
        val adapter = ProviderEntryAdapter(this, providers)
        adapter.setClickListener(object : ItemClickListener<ServiceObject?>(adapter) {
            override fun onClick(item: ServiceObject?, position: Int) {
                ViewRouter.instance()
                    .goToServiceProvider(this@ProvidersActivity, item!!.slug, null, null, collectionData)
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        navigationBar.init(this, null)
    }

    override fun onResume() {
        super.onResume()
        if (getBooleanValue("continueFinish")) {
            finish()
        }
        navigationBar.refreshView()
    }

    override fun getSearchView(): View {
        return searchLayout
    }
}
