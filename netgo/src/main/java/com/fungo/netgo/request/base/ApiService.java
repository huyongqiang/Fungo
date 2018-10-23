package com.fungo.netgo.request.base;

import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @author Pinger
 * @since 18-10-18 下午3:20
 * <p>
 * Retrofit的Service的方法上不能带有泛型，就是不能有未知类型，这里将原来的泛型改成了ResponseBody,自己主动去处理数据
 */
public interface ApiService {


    /**
     * post请求
     *
     * @param url     服务器接口
     * @param headers 请求头
     * @param params  参数
     */
    @POST()
    @FormUrlEncoded
    Flowable<ResponseBody> post(
            @Url() String url,
            @HeaderMap Map<String, String> headers,
            @FieldMap Map<String, Object> params);

    /**
     * post请求
     *
     * @param url    服务器接口
     * @param object 请求体，为任意对象
     */
    @POST()
    Flowable<ResponseBody> postBody(
            @Url String url,
            @Body Object object);


    /**
     * post请求
     *
     * @param url  服务器接口
     * @param body 请求体，为RequestBody对象
     */
    @POST()
    Flowable<ResponseBody> postRequestBody(
            @Url() String url,
            @Body RequestBody body);


    /**
     * get请求
     *
     * @param url     服务器接口
     * @param headers 请求头
     * @param params  参数
     */
    @GET()
    Flowable<ResponseBody> get(
            @Url String url,
            @HeaderMap Map<String, String> headers,
            @QueryMap Map<String, Object> params);


    /**
     * 上传图片
     *
     * @param url         服务器接口
     * @param requestBody 请求体
     */
    @Multipart
    @POST()
    Flowable<ResponseBody> uploadImage(
            @Url() String url,
            @Part("image\"; filename=\"image.jpg") RequestBody requestBody);


    /**
     * 上传文件
     *
     * @param url         服务器接口
     * @param requestBody 请求体
     * @param file        要上传的文件
     */
    @Multipart
    @POST()
    Flowable<ResponseBody> uploadFile(
            @Url String url,
            @Part("description") RequestBody requestBody,
            @Part("image\"; filename=\"image.jpg") MultipartBody.Part file);


    /**
     * 下载
     *
     * @param url 服务器接口
     */
    @Streaming
    @GET
    Flowable<ResponseBody> downloadFile(@Url String url);

}