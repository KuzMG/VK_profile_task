package com.example.myapplication.ui.image

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.myapplication.R
import com.squareup.picasso.Picasso


class ImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var url: String
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments?.getString(URL_KEY)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        imageView = view.findViewById(R.id.image_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load(url).into(imageView)
    }

    companion object {
        private const val URL_KEY="URL"
        fun newInstance(url: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(URL_KEY, url)
                }
            }
    }
}