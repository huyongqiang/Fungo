package com.fungo.sample.main.recycler

import com.fungo.baselib.base.recycler.BaseRecyclerContract
import com.fungo.baselib.base.recycler.BaseRecyclerFragment
import com.fungo.sample.main.FragmentFactory

/**
 * @author Pinger
 * @since 18-7-24 下午6:01
 */

class RecyclerFragment : BaseRecyclerFragment() {

    override fun getPresenter(): BaseRecyclerContract.Presenter {
        return RecyclerPresenter(this)
    }

    override fun getPageTitle(): String? {
        return arguments?.getString(FragmentFactory.FRAGMENT_KEY_TITLE)
    }

    override fun initRecyclerView() {
        // 注册holder
        register(RecyclerTextBean::class.java, RecyclerTextViewHolder())
        register(RecyclerAdBean::class.java, RecyclerAdViewHolder())
    }


}