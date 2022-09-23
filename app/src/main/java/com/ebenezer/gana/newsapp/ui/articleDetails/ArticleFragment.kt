package com.ebenezer.gana.newsapp.ui.articleDetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.ebenezer.gana.newsapp.databinding.FragmentArticleBinding
import com.ebenezer.gana.newsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "ArticleFragment"

@AndroidEntryPoint
class ArticleFragment : Fragment() {

    private val viewModel: ArticleDetailsViewModel by viewModels()
    private var _binding: FragmentArticleBinding? = null

    private val binding get() = _binding!!

    private val args: ArticleFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ViewModel:$viewModel")
        val article = args.article
        binding.webView.apply {
            webViewClient = WebViewClient()
            article?.url.let {
                loadUrl(it!!)
            }
        }

        lifecycleScope.launch {
            viewModel.resultSharedFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    when (it) {
                        is Resource.Success -> Snackbar.make(
                            view,
                            "${it.data}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        is Resource.Error -> Snackbar.make(
                            view,
                            "${it.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        else -> {}
                    }
                }
        }
        binding.fab.setOnClickListener {
            viewModel.saveArticle(article)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}