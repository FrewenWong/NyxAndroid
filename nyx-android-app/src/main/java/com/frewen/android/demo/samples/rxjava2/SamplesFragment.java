package com.frewen.android.demo.samples.rxjava2;

import android.os.Bundle;

import com.frewen.android.demo.R;
import com.frewen.aura.framework.fragment.BaseFragment;

/**
 * @filename: OperatorsFragment
 * @introduction: RxAndroid2和RxJava2的操作符相关
 * @author: Frewen.Wong
 * @time: 2020/9/29 13:40
 *         Copyright ©2020 Frewen.Wong. All Rights Reserved.
 */
public class SamplesFragment extends BaseFragment {

    /**
     *
     */
    public static SamplesFragment newInstance() {
        Bundle args = new Bundle();
        SamplesFragment fragment = new SamplesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.layout_common_recyclerview;
    }
}
