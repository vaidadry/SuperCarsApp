package vaida.dryzaite.supercarsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import vaida.dryzaite.supercarsapp.R
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.fragmentFactory = fragmentFactory

        setContentView(R.layout.activity_main)
        Navigation.findNavController(this, R.id.nav_host_fragment)
    }
}