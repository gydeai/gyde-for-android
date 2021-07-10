package com.gyde.mylibrary.network.retrofit

import com.gyde.mylibrary.network.response.walkthroughlist.WalkthroughsListResponse
import com.gyde.mylibrary.network.response.walkthroughsteps.WalkthroughStepsResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface WalkthroughListInterface {
    @FormUrlEncoded
    @POST("android/getContentList")
    fun getWalkthroughList(
        @Field("appId") appId: String
    ): Call<WalkthroughsListResponse>

    @FormUrlEncoded
    @POST("android/getFlowJsonForBtn")
    fun getWalkthroughSteps(
        @Field("appId") appId: String,
        @Field("flowId") flowId: String
    ): Call<WalkthroughStepsResponse>

    @FormUrlEncoded
    @POST("int/saveLog")
    fun saveUserLog(
        @Field("clientId") appId: String,
        @Field("flowId") flowID: String,
        @Field("timestamp") timestamp: Long,
        @Field("type") type: String,
        @Field("userKey") uuid: String,
        @Field("url") url: String,
        @Field("source") source: String,
        @Field("activityName") activityName: String
    ): Call<ResponseBody>
}