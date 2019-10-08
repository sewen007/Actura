package com.zkc.commandmcu;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface PostData {
    //Specify the request type and pass the relative URL//

//    @POST("/?code")
    @POST("/scan/tagging/")
    Call<ResponseBody> tagUser(@Body RequestBody requestBody);

    @POST("/scan/release")
    Call<ResponseBody> releaseUser(@Body RequestBody requestBody);

    @POST("/scan/utilisation")
    Call<ResponseBody> utilizeUser(@Body RequestBody requestBody);

    @POST("/scan/verify-tag")
    Call<ResponseBody> verifyUser(@Body RequestBody requestBody);

//Wrap the response in a Call object with the type of the expected result//


}
