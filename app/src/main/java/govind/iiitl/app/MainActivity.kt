package govind.iiitl.app

import android.Manifest
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import govind.iiitl.app.BloggerAPI.service
import govind.iiitl.app.activities.AboutPageActivity
import govind.iiitl.app.activities.AnnouncementActivity
import govind.iiitl.app.activities.ArchiveActivity
import govind.iiitl.app.activities.AskDetailActivity
import govind.iiitl.app.activities.FacultyActivity
import govind.iiitl.app.activities.MessMenuActivity
import govind.iiitl.app.activities.signIn.LogOut
import govind.iiitl.app.adapter.PostAdapter
import govind.iiitl.app.models.PostList
import govind.iiitl.app.utils.openWebPage
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val network = isNetworkConnected
        retry_button.setOnClickListener { data }

        if (!network) {
            ll_no_network.visibility = View.VISIBLE
        } else {
            ll_no_network.visibility = View.GONE
        }

        postList.layoutManager = LinearLayoutManager(this)
        setUpToolbar()

        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        Permissions.check(
            this /*context*/,
            permissions,
            null /*rationale*/,
            null /*options*/,
            object : PermissionHandler() {
                override fun onGranted() {}
            })

        navigation_menu.itemIconTintList = null
        navigation_menu.setNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_TimeTable -> startActivity(
                    Intent(
                        this@MainActivity,
                        AskDetailActivity::class.java
                    )
                )
                R.id.nav_axios -> openWebPage(this, resources.getString(R.string.axios_website))
                R.id.nav_dsc -> openWebPage(this, resources.getString(R.string.dsc_website))
                R.id.nav_equinox -> openWebPage(this, resources.getString(R.string.equinox_website))
                R.id.nav_eduthon -> openWebPage(this, resources.getString(R.string.eduthon_website))
                R.id.nav_mess_menu -> {
                    val intent = Intent(this, MessMenuActivity::class.java)
                    intent.putExtra("ViewType", "assets")
                    startActivity(intent)
                }
                R.id.nav_ecell -> openWebPage(this, resources.getString(R.string.ecellwebsite))
                R.id.nav_logOut -> startActivity(Intent(this@MainActivity, LogOut::class.java))
                R.id.nav_faculty -> startActivity(
                    Intent(
                        this@MainActivity,
                        FacultyActivity::class.java
                    )
                )
                R.id.nav_announcement -> startActivity(
                    Intent(
                        this@MainActivity,
                        AnnouncementActivity::class.java
                    )
                )
                R.id.retry_button -> {
                    data
                }
                R.id.nav_archives -> startActivity(
                    Intent(
                        this@MainActivity,
                        ArchiveActivity::class.java
                    )
                )
                R.id.nav_about_us -> {
                    startActivity(Intent(this@MainActivity, AboutPageActivity::class.java))
                }
            }
            drawerLayout.closeDrawers()
            false
        }
        data
    }

    private val isNetworkConnected: Boolean
        get() {
            val cm = (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        }

    private fun setUpToolbar() {
        //To give backward Compatibility
        toolbar.title = resources.getString(R.string.posts)
        setSupportActionBar(toolbar)
        //To sync Drawer and Toolbar
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    private val data: Unit
        get() {
            val postsList = service!!.postList
            postsList!!.enqueue(object : Callback<PostList?> {
                override fun onResponse(call: Call<PostList?>, response: Response<PostList?>) {
                    val list = response.body()
                    val adapter = PostAdapter(this@MainActivity, list?.items!!)
                    postList.adapter = adapter

                    Handler().postDelayed({
                        adapter.showshmmer = false
                        adapter.notifyDataSetChanged()
                    }, 1000)

                    if (adapter.itemCount != 0) {
                        no_network_image_view.visibility = View.GONE
                        retry_button!!.visibility = View.GONE
                    }

                    swipe_refresh!!.setOnRefreshListener {
                        Handler().postDelayed({
                            adapter.showshmmer = true
                            swipe_refresh!!.isRefreshing = false
                            adapter.notifyDataSetChanged()
                            adapter.showshmmer = false
                        }, 1000)
                    }
                }

                override fun onFailure(call: Call<PostList?>, t: Throwable) {
                    Toast.makeText(
                        this@MainActivity,
                        "No Internet Connection Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
}