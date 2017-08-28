package com.afollestad.asonretrofit;

import com.afollestad.asonretrofit.AsonResponseBodyConverter.Mode;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public final class AsonConverterFactory extends Converter.Factory {

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(
      Type type, Annotation[] annotations, Retrofit retrofit) {
    if (type instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) type;
      if (List.class == paramType.getRawType()) {
        return new AsonResponseBodyConverter<>(
            Mode.List, (Class<?>) paramType.getActualTypeArguments()[0]);
      }
    } else if (type instanceof Class) {
      Class<?> cls = (Class<?>) type;
      if (cls.isArray()) {
        return new AsonResponseBodyConverter<>(Mode.Array, cls);
      } else {
        return new AsonResponseBodyConverter<>(Mode.Object, cls);
      }
    }
    return null;
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(
      Type type,
      Annotation[] parameterAnnotations,
      Annotation[] methodAnnotations,
      Retrofit retrofit) {
    return new AsonRequestBodyConverter<>();
  }
}
