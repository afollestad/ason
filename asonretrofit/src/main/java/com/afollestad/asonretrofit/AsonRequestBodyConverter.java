package com.afollestad.asonretrofit;

import com.afollestad.ason.Ason;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

/** @author Aidan Follestad (afollestad) */
public class AsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

  private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  @Override
  public RequestBody convert(T value) throws IOException {
    String body;
    if (value instanceof List) {
      body = Ason.serializeList((List) value).toString();
    } else if (value.getClass().isArray()) {
      body = Ason.serializeArray(value).toString();
    } else {
      body = Ason.serialize(value).toString();
    }
    if (body == null) {
      return null;
    }
    Buffer buffer = new Buffer();
    Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
    writer.write(body);
    writer.close();
    return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
  }
}
