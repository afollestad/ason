package com.afollestad.asonretrofit;

import java.io.IOException;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

  @Override
  public RequestBody convert(T value) throws IOException {
    return null;
  }
}
