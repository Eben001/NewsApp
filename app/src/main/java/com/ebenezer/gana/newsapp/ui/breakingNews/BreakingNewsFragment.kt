package com.ebenezer.gana.newsapp.ui.breakingNews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ebenezer.gana.newsapp.adapters.NewsAdapter
import com.ebenezer.gana.newsapp.databinding.FragmentBreakingNewsBinding
import com.ebenezer.gana.newsapp.util.Constants.Companion.CODE_NIGERIA
import com.ebenezer.gana.newsapp.util.Constants.Companion.QUERY_PAGE_SIZE
import com.ebenezer.gana.newsapp.util.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BreakingNewsFragment : Fragment() {

    private var _binding: FragmentBreakingNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BreakingNewsViewModel by viewModels()

    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerView()
        subscribeToObservables()
        setOnClickListeners()
        addOnScrollListener()
    }

    private fun setupAdapter() {
        newsAdapter = NewsAdapter(requireContext()) {
            val action =
                BreakingNewsFragmentDirections.actionBreakingNewsFragmentToArticleFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun subscribeToObservables() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.breakingNewsResult
                        .collectLatest { response ->
                            when (response) {
                                is Result.Loading -> {
                                    startRefreshing()
                                }
                                is Result.Success -> {
                                    stopRefreshing()
                                    response.data.let { newsResponse ->
                                        newsAdapter.submitList(newsResponse?.articles?.toList())

                                        val totalPages =
                                            newsResponse!!.totalResults / QUERY_PAGE_SIZE + 2
                                        isLastPage = viewModel.breakingNewsPage == totalPages
                                        if (isLastPage) {
                                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                                        }

                                    }

                                }
                                else -> {
                                    stopRefreshing()
                                    response?.asString(requireContext())?.let { message ->
                                        Toast.makeText(
                                            requireContext(),
                                            "An Error occurred: $message",
                                            Toast.LENGTH_SHORT
                                        ).show()


                                    }

                                }

                            }
                        }
                }

            }

        }
    }
    private fun stopRefreshing() {
        if (binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = false
            isLoading = false

        }
    }
    private fun startRefreshing(){
        binding.swipeRefresh.isRefreshing = true
        isLoading = true
    }

    private fun setOnClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                viewModel.getBreakingNews(CODE_NIGERIA)
                binding.swipeRefresh.isRefreshing = true
            }
        }
    }

    private fun addOnScrollListener() {
        binding.rvBreakingNews.addOnScrollListener(scrollListener)
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true

            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewModel.getBreakingNews(CODE_NIGERIA)
                isScrolling = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}