package com.afollestad.asonretrofit;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class ResponseTest {

  private TestService service;

  @Before
  public void setup() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(new AsonConverterFactory())
        .build();
    service = retrofit.create(TestService.class);
  }

  @Test
  public void test_response_converter_object() {
    TestResponse response = service.getTestObject()
        .blockingGet()
        .body();
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
  public void test_response_converter_array() {
    TestPerson[] response = service.getTestArray()
        .blockingGet()
        .body();
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
  public void test_response_converter_list() {
    List<TestPerson> response = service.getTestList()
        .blockingGet()
        .body();
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
