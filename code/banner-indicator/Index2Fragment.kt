package com.sunmoonblog.igamelite.p2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sunmoonblog.igamelite.R
import kotlinx.android.synthetic.main.fragment_index_2.view.*

class Index2Fragment : Fragment() {
    companion object {
        fun newInstance(): Index2Fragment {
            return Index2Fragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_index_2, container, false)
        view.viewPager.adapter = B()
        view.tabLayout.setupWithViewPager(view.viewPager)
        return view
    }

    class B : PagerAdapter() {

        val array = listOf("1", "2", "3", "4")

        override fun isViewFromObject(p0: View, p1: Any) = p0 == p1
        override fun getCount() = array.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val textView = TextView(container.context)
            textView.text = array[position]
            container.addView(textView)
            return textView
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }
}