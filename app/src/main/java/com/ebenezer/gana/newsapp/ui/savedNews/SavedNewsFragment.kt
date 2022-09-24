package com.ebenezer.gana.newsapp.ui.savedNews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ebenezer.gana.newsapp.databinding.FragmentSavedNewsBinding
import com.ebenezer.gana.newsapp.ui.adapters.NewsAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SavedNewsFragment"

@AndroidEntryPoint
class SavedNewsFragment : Fragment() {

    private val viewModel: SavedNewsViewModel by viewModels()
    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentSavedNewsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ViewModel:$viewModel")

        setupRecyclerView()
        lifecycleScope.launch {
            viewModel.getSavedNews()
                .catch { it.printStackTrace() }
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { articles ->
                    newsAdapter.submitList(articles)
                }
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.currentList[position]
                viewModel.deleteArticle(article)

                lifecycleScope.launch {
                    viewModel.resultSharedFlow
                        .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                        .collectLatest { result ->
                            Snackbar.make(
                                view,
                                "${result.asString(requireContext())}",
                                Snackbar.LENGTH_LONG
                            ).apply {
                                setAction("Undo") {
                                    viewModel.saveArticle(article)
                                }
                                show()

                            }
                        }
                }


            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }
    }


    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(requireContext()) {
            val action = SavedNewsFragmentDirections.actionSavedNewsFragmentToArticleFragment(it)
            findNavController().navigate(action)
        }
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}