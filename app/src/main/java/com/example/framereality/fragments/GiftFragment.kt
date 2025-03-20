import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.adapter.GiftAdapter
import com.example.framereality.assets.Helper
import com.example.framereality.databinding.FragmentGiftBinding

class GiftFragment : Fragment() {
    private var binding: FragmentGiftBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGiftBinding.inflate(inflater, container, false)
        val view: View = binding!!.getRoot()

        val recyclerView: RecyclerView = binding!!.giftRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val context=requireContext()
        val giftAdapter = GiftAdapter(context,Helper.getGifts())
        recyclerView.adapter = giftAdapter
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}