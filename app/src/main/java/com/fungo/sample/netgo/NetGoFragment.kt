package com.fungo.sample.netgo

import com.fungo.baseuilib.recycler.BaseRecyclerContract
import com.fungo.baseuilib.recycler.BaseRecyclerFragment
import com.fungo.netgo.NetGo
import com.fungo.sample.R

/**
 * @author Pinger
 * @since 18-10-15 下午7:00
 * 网络类库[NetGo]的Demo页面
 */
class NetGoFragment : BaseRecyclerFragment() {

    override fun getPageTitle(): String? {
        return getString(R.string.main_netgo)
    }

    override fun getPresenter(): BaseRecyclerContract.Presenter {
        return NetGoPresenter(this)
    }

    override fun initPageView() {
        setPageTitle(getString(R.string.main_netgo))
        register(GankResults.Item::class.java, NetGoHolder())
    }

    override fun isShowBackIcon(): Boolean = true
}