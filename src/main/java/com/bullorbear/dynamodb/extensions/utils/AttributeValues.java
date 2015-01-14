package com.bullorbear.dynamodb.extensions.utils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.document.internal.InternalUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class AttributeValues {

  /**
   * Converts a simple value into the low-level <code><AttributeValue/code>
   * representation.
   *
   * @param value
   *          the given value which can be one of the followings: <ul>
   *          <li>String</li> <li>Set&lt;String></li> <li>Number (including any
   *          subtypes and primitive types)</li> <li>Set&lt;Number></li>
   *          <li>byte[]</li> <li>Set&lt;byte[]></li> <li>ByteBuffer</li>
   *          <li>Set&lt;ByteBuffer></li> <li>Boolean or boolean</li>
   *          <li>null</li> <li>Map&lt;String,T>, where T can be any type on
   *          this list but must not induce any circular reference</li>
   *          <li>List&lt;T>, where T can be any type on this list but must not
   *          induce any circular reference</li> </ul>
   * @return a non-null low level representation of the input object value
   *
   * @throws UnsupportedOperationException
   *           if the input object type is not supported
   */
  public static AttributeValue toAttributeValue(Object value) {
    AttributeValue result = new AttributeValue();
    if (value == null) {
      return result.withNULL(Boolean.TRUE);
    } else if (value instanceof Boolean) {
      return result.withBOOL((Boolean) value);
    } else if (value instanceof String) {
      return result.withS((String) value);
    } else if (value instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal) value;
      return result.withN(bd.toPlainString());
    } else if (value instanceof Number) {
      return result.withN(value.toString());
    } else if (value instanceof byte[]) {
      return result.withB(ByteBuffer.wrap((byte[]) value));
    } else if (value instanceof ByteBuffer) {
      return result.withB((ByteBuffer) value);
    } else if (value instanceof Set) {
      // default to an empty string set if there is no element
      @SuppressWarnings("unchecked")
      Set<Object> set = (Set<Object>) value;
      if (set.size() == 0) {
        result.setSS(new LinkedHashSet<String>());
        return result;
      }
      Object element = set.iterator().next();
      if (element instanceof String) {
        @SuppressWarnings("unchecked")
        Set<String> ss = (Set<String>) value;
        result.setSS(new ArrayList<String>(ss));
      } else if (element instanceof Number) {
        @SuppressWarnings("unchecked")
        Set<Number> in = (Set<Number>) value;
        List<String> out = new ArrayList<String>(set.size());
        for (Number n : in) {
          BigDecimal bd = InternalUtils.toBigDecimal(n);
          out.add(bd.toPlainString());
        }
        result.setNS(out);
      } else if (element instanceof byte[]) {
        @SuppressWarnings("unchecked")
        Set<byte[]> in = (Set<byte[]>) value;
        List<ByteBuffer> out = new ArrayList<ByteBuffer>(set.size());
        for (byte[] buf : in) {
          out.add(ByteBuffer.wrap(buf));
        }
        result.setBS(out);
      } else if (element instanceof ByteBuffer) {
        @SuppressWarnings("unchecked")
        Set<ByteBuffer> bs = (Set<ByteBuffer>) value;
        result.setBS(bs);
      } else {
        throw new UnsupportedOperationException("element type: " + element.getClass());
      }
    } else if (value instanceof List) {
      @SuppressWarnings("unchecked")
      List<Object> in = (List<Object>) value;
      List<AttributeValue> out = new ArrayList<AttributeValue>();
      for (Object v : in) {
        out.add(toAttributeValue(v));
      }
      result.setL(out);
    } else if (value instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> in = (Map<String, Object>) value;
      if (in.size() > 0) {
        for (Map.Entry<String, Object> e : in.entrySet()) {
          result.addMEntry(e.getKey(), toAttributeValue(e.getValue()));
        }
      } else { // empty map
        result.setM(new LinkedHashMap<String, AttributeValue>());
      }
    } else {
      throw new UnsupportedOperationException("value type: " + value.getClass());
    }
    return result;
  }

}
