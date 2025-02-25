package com.example.framereality.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.framereality.MyUtils
import com.example.framereality.R
import com.example.framereality.databinding.ActivityMainBinding
import com.example.framereality.fragment.HomeFragment
import com.example.framereality.fragments.ChatsListFragment
import com.example.framereality.fragments.FavouriteListFragment
import com.example.framereality.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        //Check if user is logged in
        if(firebaseAuth.currentUser == null){
            startLoginOptionsActivity()
        }

        showHomeFragment()

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId

            when(itemId) {
                R.id.item_home -> {
                    showHomeFragment()
                    return@setOnItemSelectedListener true
                }

                R.id.item_chats -> {


                    if (firebaseAuth.currentUser == null) {
                        MyUtils.toast(this, "Login Required...")
                        return@setOnItemSelectedListener false
                    } else {
                        showChatsListFragment()
                        return@setOnItemSelectedListener true
                    }
                }

                R.id.item_favourite -> {

                    if (firebaseAuth.currentUser == null) {
                        MyUtils.toast(this, "Login Required...")
                        return@setOnItemSelectedListener false
                    } else {
                        showFavouriteListFragment()
                        return@setOnItemSelectedListener true
                    }
                }

                R.id.item_profile -> {

                    if (firebaseAuth.currentUser == null) {
                        MyUtils.toast(this, "Login Required...")
                        return@setOnItemSelectedListener false
                    } else {
                        showProfileFragment()
                        return@setOnItemSelectedListener true
                    }
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

    private fun showHomeFragment(){
        binding.toolbarTitleTv.text = "Home"
        val homeFragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,homeFragment,"Home")
        fragmentTransaction.commit()
    }

    private fun showChatsListFragment(){
        binding.toolbarTitleTv.text = "Chats"
        val chatsListFragment = ChatsListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,chatsListFragment,"ChatsList")
        fragmentTransaction.commit()
    }

    private fun showFavouriteListFragment(){
        binding.toolbarTitleTv.text = "Favourites"
        val favouriteListFragment = FavouriteListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,favouriteListFragment,"FavouriteList")
        fragmentTransaction.commit()
    }

    private fun showProfileFragment(){
        binding.toolbarTitleTv.text = "Profile"
        val profileFragment = ProfileFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,profileFragment,"Profile")
        fragmentTransaction.commit()
    }

    private fun startLoginOptionsActivity(){
        startActivity(Intent(this,LoginOptionsActivity::class.java))
    }
}