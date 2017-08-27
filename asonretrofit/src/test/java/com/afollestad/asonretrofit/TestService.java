package com.afollestad.asonretrofit;

import io.reactivex.Single;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.GET;

public interface TestService {

  @GET("/afollestad/ason/master/asonretrofit/test.json")
  Single<Response<TestResponse>> getTestObject();

  @GET("/afollestad/ason/master/asonretrofit/test2.json")
  Single<Response<List<TestPerson>>> getTestList();

  @GET("/afollestad/ason/master/asonretrofit/test2.json")
  Single<Response<TestPerson[]>> getTestArray();
}
