package kr.hs.anu.snow.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.hs.anu.snow.R
import kr.hs.anu.snow.data.repository.ChzzkLiveStatusRepository
import kr.hs.anu.snow.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), ChzzkLiveStatusRepository.OnLiveStatusUpdate {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ChzzkLiveStatusRepository.startPolling(this)
    }

    override fun onUpdate(isLive: Boolean, title: String, viewers: Int) {
        activity?.runOnUiThread {
            val statusText = if (isLive) {
                "라이브 중: $title\n👥 시청자: $viewers"
            } else {
                "방송 종료됨"
            }
            binding.ivLiveDot.setImageResource(
                if (isLive) R.drawable.live_dot_on else R.drawable.live_dot_off
            )
            binding.tvLiveInfo.text = statusText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        ChzzkLiveStatusRepository.stopPolling()
    }
}
