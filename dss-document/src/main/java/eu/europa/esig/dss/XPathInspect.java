package eu.europa.esig.dss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathInspect {

  private static final Logger LOG = LoggerFactory.getLogger(XPathInspect.class);

  private static final Map<String, Duration> durationMap = new HashMap<>();

  public static Map<String, Duration> getDurationMap() {
    return durationMap;
  }

  public static <R> R stopWatch(String methodName, String xPathString, Callable<R> function) {
    Duration duration = durationMap.computeIfAbsent("methodName" + "$" + xPathString, key -> new XPathInspect.Duration(methodName, xPathString));
    long startTime = System.nanoTime();
    LOG.info("Start - method: {}, xPath: {}", methodName, xPathString);
    try {
      return function.call();
    } catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      } else {
        throw new RuntimeException(e);
      }
    }
    finally {
      long d = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
      duration.incCount();
      duration.addTime(d);
      LOG.info("End - method: {}, duration: {}, xPath: {}", methodName, d, xPathString);
    }
  }

  public static void dump() {
    List<Map.Entry<String, Duration>> list = new ArrayList<>(durationMap.entrySet());
    list.sort((e1, e2) -> (int) (e2.getValue().getTime() - e1.getValue().getTime()));
    list.forEach(e -> LOG.info("{}", e.getValue().toString()));
  }

  public static class Duration {

    public String method;
    public String xpath;
    public long count;
    public long time;

    public Duration(String method, String xpath) {
      this.method = method;
      this.xpath = xpath;
      this.count = 0;
      this.time = 0;
    }

    public String getMethod() {
      return method;
    }

    public String getXpath() {
      return xpath;
    }

    public long getCount() {
      return count;
    }

    public void incCount() {
      count++;
    }

    public long getTime() {
      return time;
    }

    public void addTime(long time) {
      this.time += time;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      Duration that = (Duration) o;
      return count == that.count && time == that.time && Objects.equals(method, that.method) && Objects.equals(xpath, that.xpath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(method, xpath, count, time);
    }

    @Override
    public String toString() {
      return method + ";" + count + ";" + time + ";" + xpath;
    }

  }
}
