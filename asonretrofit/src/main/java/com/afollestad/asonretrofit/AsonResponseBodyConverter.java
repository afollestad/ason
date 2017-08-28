package com.afollestad.asonretrofit;

import com.afollestad.ason.Ason;
import com.afollestad.ason.AsonArray;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/** @author Aidan Follestad (afollestad) */
class AsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

  public enum Mode {
    Object,
    Array,
    List
  }

  private final Mode mode;
  private final Class<?> targetType;

  AsonResponseBodyConverter(Mode mode, Class<?> targetType) {
    this.mode = mode;
    this.targetType = targetType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T convert(ResponseBody value) throws IOException {
    String body = value.string();
    switch (mode) {
      case Array:
        return (T) new AsonArray(body).deserialize(targetType);
      case List:
        return (T) new AsonArray(body).deserializeList(targetType);
      default:
        return (T) new Ason(body).deserialize(targetType);
    }
  }
}
