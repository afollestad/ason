package com.afollestad.asonretrofit;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RequestTest {

  private TestService service;

  @Before
  public void setup() {
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://postman-echo.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(new AsonConverterFactory())
            .build();
    service = retrofit.create(TestService.class);
  }

  @Test
  public void test_request_converter_object() {
    TestData request = new TestData();
    request.count = 2;
    request.people =
        new TestPerson[] {new TestPerson(1, "Aidan", 22), new TestPerson(2, "Nina", 22)};

    EchoObjectWrapper responseWrapper = service.putTestObject(request).blockingGet().body();
    assertThat(responseWrapper).isNotNull();
    TestData response = responseWrapper.data;
    assertThat(response).isNotNull();
    assertThat(response.count).isEqualTo(2);
    assertThat(response.people).isNotNull();
    assertThat(response.people.length).isEqualTo(response.count);
    assertThat(response.people[0].id).isEqualTo(1);
    assertThat(response.people[0].name).isEqualTo("Aidan");
    assertThat(response.people[0].age).isEqualTo(22);
    assertThat(response.people[1].id).isEqualTo(2);
    assertThat(response.people[1].name).isEqualTo("Nina");
    assertThat(response.people[1].age).isEqualTo(22);
  }

  @Test
  public void test_request_converter_array() {
    TestPerson[] request =
        new TestPerson[] {new TestPerson(1, "Aidan", 22), new TestPerson(2, "Nina", 22)};
    EchoArrayWrapper responseWrapper = service.putTestArray(request).blockingGet().body();
    assertThat(responseWrapper).isNotNull();

    TestPerson[] response = responseWrapper.data;
    assertThat(response).isNotNull();
    assertThat(response.length).isEqualTo(2);
    assertThat(response[0].id).isEqualTo(1);
    assertThat(response[0].name).isEqualTo("Aidan");
    assertThat(response[0].age).isEqualTo(22);
    assertThat(response[1].id).isEqualTo(2);
    assertThat(response[1].name).isEqualTo("Nina");
    assertThat(response[1].age).isEqualTo(22);
  }

  @Test
  public void test_request_converter_list() {
    List<TestPerson> request = new ArrayList<>(2);
    request.add(new TestPerson(1, "Aidan", 22));
    request.add(new TestPerson(2, "Nina", 22));
    EchoListWrapper responseWrapper = service.putTestList(request).blockingGet().body();
    assertThat(responseWrapper).isNotNull();

    List<TestPerson> response = responseWrapper.data;
    assertThat(response).isNotNull();
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.get(0).id).isEqualTo(1);
    assertThat(response.get(0).name).isEqualTo("Aidan");
    assertThat(response.get(0).age).isEqualTo(22);
    assertThat(response.get(1).id).isEqualTo(2);
    assertThat(response.get(1).name).isEqualTo("Nina");
    assertThat(response.get(1).age).isEqualTo(22);
  }
}
