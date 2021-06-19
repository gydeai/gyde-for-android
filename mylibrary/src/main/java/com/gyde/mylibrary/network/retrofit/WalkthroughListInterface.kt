package com.gyde.library.network.retrofit

import com.gyde.mylibrary.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.mylibrary.network.response.walkthroughsteps.WalkthroughStepsResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface WalkthroughListInterface {
    @FormUrlEncoded
    @POST("getContentList")
    fun getWalkthroughList(
        @Field("appId") appId: String
    ): Call<WalkthroughsListResponse>

    @FormUrlEncoded
    @POST("getFlowJsonForBtn")
    fun getWalkthroughSteps(
        @Field("appId") appId: String,
        @Field("flowId") flowId: String
    ): Call<WalkthroughStepsResponse>
}