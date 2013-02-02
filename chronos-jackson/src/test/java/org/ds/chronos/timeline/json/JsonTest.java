package org.ds.chronos.timeline.json;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.ds.chronos.api.Chronos;
import org.ds.chronos.api.ChronosException;
import org.ds.chronos.chronicle.MemoryChronicle;
import org.ds.chronos.chronicle.PartitionPeriod;
import org.ds.chronos.timeline.Timeline;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class JsonTest {

  Timeline<JsonTestObject> timeline;

  private static JsonTimelineFactory factory;

  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void create() throws ChronosException {
    Chronos chronos = Mockito.mock(Chronos.class);
    Mockito.when(chronos.getChronicle(Mockito.anyString())).thenReturn(
        new MemoryChronicle());
    Mockito.when(
        chronos.getChronicle(Mockito.anyString(),
            Mockito.any(PartitionPeriod.class))).thenReturn(
        new MemoryChronicle());
    Mockito.when(
        chronos.getTimeline(Mockito.anyString(),
            Mockito.any(JsonTimelineEncoder.class),
            Mockito.any(JsonTimelineDecoder.class))).thenCallRealMethod();
    factory = new JsonTimelineFactory(chronos);
  }

  @Before
  public void setup() throws ChronosException {
    timeline = factory.createTimeline("test",
        new TypeReference<JsonTestObject>() {
        });
  }

  public JsonTestObject buildObject(long time) {
    JsonTestObject object = new JsonTestObject();
    object.setId(4);
    object.setName("Dan");
    object.setAge(28);
    object.setWeight(171.0);
    object.setTimestamp(time);
    return object;
  }

  @Test
  public void testCodec() {
    JsonTestObject object = buildObject(1000);
    timeline.add(buildObject(1000));
    JsonTestObject other = timeline.query(0, 1000, JsonTestObject.class)
        .first();
    Assert.assertNotNull(other);
    Assert.assertEquals(object.getId(), other.getId());
    Assert.assertEquals(object.getName(), other.getName());
    Assert.assertEquals(object.getAge(), other.getAge());
    Assert.assertEquals(object.getWeight(), other.getWeight(), 0.1);
    Assert.assertEquals(object.getTimestamp(), other.getTimestamp());
  }

  @Test
  public void testBatch() {
    List<JsonTestObject> list = new ArrayList<JsonTestObject>();
    for (int i = 0; i < 100; i++) {
      list.add(buildObject(i * 1000));
    }
    timeline.add(list);

    List<JsonTestObject> other = timeline.query(0, 1000 * 100,
        JsonTestObject.class).list();
    Assert.assertEquals(list.size(), other.size());
  }

}
