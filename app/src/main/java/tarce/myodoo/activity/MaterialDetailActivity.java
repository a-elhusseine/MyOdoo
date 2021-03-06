package tarce.myodoo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.SearchView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit2.Call;
import retrofit2.Response;
import tarce.api.MyCallback;
import tarce.api.RetrofitClient;
import tarce.api.api.InventoryApi;
import tarce.model.inventory.MainMdBean;
import tarce.model.inventory.MaterialDetailBean;
import tarce.model.inventory.PickingDetailBean;
import tarce.myodoo.R;
import tarce.myodoo.activity.salesout.NewSaleoutActivity;
import tarce.myodoo.adapter.processproduct.PrepareMdAdapter;
import tarce.myodoo.uiutil.RecyclerFooterView;
import tarce.myodoo.uiutil.RecyclerHeaderView;
import tarce.myodoo.uiutil.TipDialog;
import tarce.myodoo.utils.DateTool;
import tarce.myodoo.utils.StringUtils;
import tarce.support.ViewUtils;

/**
 * Created by rose.zou on 2017/5/24.
 * 生产备料-》延误-》详细子页面
 */

public class MaterialDetailActivity extends BaseActivity {


    @InjectView(R.id.swipe_target)
    RecyclerView recyclerMaterdetail;
    @InjectView(R.id.swipe_refresh_header)
    RecyclerHeaderView swipeRefreshHeader;
    @InjectView(R.id.swipe_load_more_footer)
    RecyclerFooterView swipeLoadMoreFooter;
    @InjectView(R.id.swipeToLoad)
    SwipeToLoadLayout swipeToLoad;
    @InjectView(R.id.search_mo)
    SearchView searchMo;
    private InventoryApi inventoryApi;
    private String state;
    private int process_id;
    private int limit;
    private PrepareMdAdapter adapter;
    private List<PickingDetailBean.ResultBean.ResDataBean> dataBeanList;
    private List<PickingDetailBean.ResultBean.ResDataBean> allListLike;
    private List<MainMdBean> mainMdBeen;
    private List<MainMdBean> mainMdBeenQuery = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_detail);
        ButterKnife.inject(this);
        setRecyclerview(recyclerMaterdetail);

        initView();
    }

    /**
     * 搜索
     * */
    private void serach() {
        searchMo.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (StringUtils.isNullOrEmpty(newText)) {
                    ViewUtils.collapseSoftInputMethod(MaterialDetailActivity.this, searchMo);
                    mainMdBeen = mainMdBeenQuery;
                } else {
                    try {
                        List<MainMdBean> filterDateList = new ArrayList<>();
                        mainMdBeenQuery.remove(0);
                        filterDateList.add(new MainMdBean(true, ""));
                        for (MainMdBean bean : mainMdBeenQuery) {
                            if (bean.t.getDisplay_name().contains(newText)) {
                                filterDateList.add(bean);
                            }
                        }
                        mainMdBeenQuery.add(0,new MainMdBean(true, ""));
                        mainMdBeen = filterDateList;
                    }catch (Exception e){
                        Log.e("zws", e.toString());
                    }
                }
//                adapter.setData(mainMdBeen);
//                adapter.notifyDataSetChanged();
                adapter = new PrepareMdAdapter(R.layout.adapter_mater_detail, R.layout.adapter_head_md, mainMdBeen);
                recyclerMaterdetail.setAdapter(adapter);
                initListener();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        if (dataBeanList == null) {
            adapter.notifyDataSetChanged();
            swipeToLoad.setRefreshing(true);
        }
        super.onResume();
    }

    private void initView() {
        showDefultProgressDialog();
        swipeRefreshHeader.setGravity(Gravity.CENTER);
        swipeLoadMoreFooter.setGravity(Gravity.CENTER);
        swipeToLoad.setRefreshHeaderView(swipeRefreshHeader);
        swipeToLoad.setLoadMoreFooterView(swipeLoadMoreFooter);
        swipeToLoad.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
                swipeToLoad.setRefreshing(false);
            }
        });

        swipeToLoad.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                try {
                    Thread.sleep(1000);
                    swipeToLoad.setLoadingMore(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Intent intent = getIntent();
        state = intent.getStringExtra("state");
        process_id = intent.getIntExtra("process_id", 1);
        limit = intent.getIntExtra("limit", 1);
        initData();
    }

    private void initData() {
        HashMap<Object, Object> hashMap = new HashMap<>();
        hashMap.put("offset", 0);
        hashMap.put("limit", limit);
        hashMap.put("process_id", process_id);
        switch (state) {
            case "延误":
                hashMap.put("date", "delay");
                break;
            case "今天":
                hashMap.put("date", DateTool.getDate());
                break;
            case "明天":
                hashMap.put("date", DateTool.getDateTime(DateTool.addDays(1), DateTool.DEFAULT_DATE_FORMAT));
                break;
            case "后天":
                hashMap.put("date", DateTool.getDateTime(DateTool.addDays(2), DateTool.DEFAULT_DATE_FORMAT));
            case "所有":
                hashMap.put("date", "all");
                break;
        }
        dataBeanList = new ArrayList<>();
        mainMdBeen = new ArrayList<>();
        inventoryApi = RetrofitClient.getInstance(MaterialDetailActivity.this).create(InventoryApi.class);
        final Call<PickingDetailBean> recentOr = inventoryApi.getRecentOr(hashMap);
        recentOr.enqueue(new MyCallback<PickingDetailBean>() {
            @Override
            public void onResponse(Call<PickingDetailBean> call, Response<PickingDetailBean> response) {
                dismissDefultProgressDialog();
                if (response.body() == null) return;
                if (response.body().getError() != null) {
                    new TipDialog(MaterialDetailActivity.this, R.style.MyDialogStyle, response.body().getError().getData().getMessage())
                            .show();
                    return;
                }
                if (response.body().getResult().getRes_data() != null && response.body().getResult().getRes_code() == 1) {
                    dataBeanList = response.body().getResult().getRes_data();
                    allListLike = dataBeanList;
                    mainMdBeen.add(new MainMdBean(true, ""));
                    if (dataBeanList != null) {
                        for (int i = 0; i < dataBeanList.size(); i++) {
                            mainMdBeen.add(new MainMdBean(dataBeanList.get(i)));
                        }
                    }
                    mainMdBeenQuery = mainMdBeen;
                    adapter = new PrepareMdAdapter(R.layout.adapter_mater_detail, R.layout.adapter_head_md, mainMdBeen);
                    recyclerMaterdetail.setAdapter(adapter);
                    if (dataBeanList != null) {
                        serach();
                        initListener();
                    }
                }
            }

            @Override
            public void onFailure(Call<PickingDetailBean> call, Throwable t) {
                dismissDefultProgressDialog();
                // ToastUtils.showCommonToast(MaterialDetailActivity.this, t.toString());
            }
        });
    }

    /**
     * item点击
     */
    private void initListener() {

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                if (position > (mainMdBeen.size() - 1)) {
                    return;
                }
                if (mainMdBeen == null || position == 0) {
                    return;
                }
                int order_id = mainMdBeen.get(position).t.getOrder_id();
                Intent intent = new Intent(MaterialDetailActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_name", mainMdBeen.get(position).t.getDisplay_name());
                intent.putExtra("order_id", order_id);
                intent.putExtra("state", mainMdBeen.get(position).t.getState());
                intent.putExtra("delay_state", state);
                intent.putExtra("limit", limit);
                intent.putExtra("process_id", process_id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        if (dataBeanList != null) {
            dataBeanList = null;
        }
        super.onPause();
    }
}
