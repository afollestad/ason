package com.afollestad.asonretrofit;

import io.reactivex.Single;
import java.util.List;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface TestService {

  @GET("/afollestad/ason/master/asonretrofit/test.json")
  Single<Response<TestData>> getTestObject();

  @GET("/afollestad/ason/master/asonretrofit/test2.json")
  Single<Response<List<TestPerson>>> getTestList();

  @GET("/afollestad/ason/master/asonretrofit/test2.json")
  Single<Response<TestPerson[]>> getTestArray();

  @PUT("/put")
  Single<Response<EchoObjectWrapper>> putTestObject(@Body TestData object);

  @PUT("/put")
  Single<Response<EchoArrayWrapper>> putTestArray(@Body TestPerson[] array);

  @PUT("/put")
  Single<Response<EchoListWrapper>> putTestList(@Body List<TestPerson> list);
}
