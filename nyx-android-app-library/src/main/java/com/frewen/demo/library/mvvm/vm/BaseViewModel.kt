package com.frewen.demo.library.mvvm.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.frewen.aura.framework.mvvm.vm.AbsViewModel
import com.frewen.demo.library.network.ResultState
import com.frewen.demo.library.network.paresException
import com.frewen.demo.library.network.paresResult
import com.frewen.network.response.AuraNetResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @filename: BaseListViewModel
 * @introduction:
 * @author: Frewen.Wong
 * @time: 2020/4/11 23:13
 * Copyright ©2020 Frewen.Wong. All Rights Reserved.
 */
abstract class BaseViewModel() : AbsViewModel() {

    private val eventString: MutableLiveData<String> = MutableLiveData<String>()

    fun <T> request(
            block: suspend () -> AuraNetResponse<T>,
            resultState: MutableLiveData<ResultState<T>>,
            isShowDialog: Boolean = false,
            loadingMessage: String = "请求网络中..."): Job {
        return viewModelScope.launch {
            runCatching {
                if (isShowDialog) {
                    eventString.postValue(loadingMessage)
                }
                //请求体
                block()
            }.onSuccess {
                resultState.paresResult(it)
            }.onFailure {
                resultState.paresException(it)
            }
        }
    }


}