package org.sef4j.core.helpers.proptree.changes;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sef4j.core.api.proptree.PropTreeNode;
import org.sef4j.core.helpers.AsyncUtils;
import org.sef4j.core.helpers.proptree.DummyCount;
import org.sef4j.core.helpers.senders.InMemoryEventSender;


public class AsyncChangeCollectorSenderTest {

	private PropTreeNode rootNode = PropTreeNode.newRoot();
	private PropTreeNode fooNode = rootNode.getOrCreateChild("foo");
	private PropTreeNode fooBarNode = fooNode.getOrCreateChild("bar");

	private DummyCount fooCount = fooNode.getOrCreateProp("dummyCount", DummyCount.FACTORY);
	private DummyCount fooBarCount = fooBarNode.getOrCreateProp("dummyCount", DummyCount.FACTORY);

	private DummyCountChangeCollector changeCollector = new DummyCountChangeCollector(rootNode);
	private InMemoryEventSender<DummyCountChangesEvent> inMemoryEventSender = new InMemoryEventSender<DummyCountChangesEvent>();
	
	private AsyncChangeCollectorSender<DummyCount,DummyCountChangesEvent> sut = 
			new AsyncChangeCollectorSender<DummyCount,DummyCountChangesEvent>(
					AsyncUtils.defaultScheduledThreadPool(), 1, // period=1 second 
					changeCollector,
					DummyCountChangesEvent.FACTORY,
					inMemoryEventSender);
	
	@Test
	public void testStartStop() throws Exception {
		// Prepare
		// Perform
		sut.start();

		fooCount.incrCount1();
		fooBarCount.incrCount2();
		
		Thread.sleep(1500);
		sut.stop();
		// sut.flush();
		// Post-check
		List<DummyCountChangesEvent> events = inMemoryEventSender.clearAndGet();
		Assert.assertTrue(events.size() >= 1);
		DummyCountChangesEvent event0 = events.get(0);
		Map<String, DummyCount> changes = event0.getChanges();
		Assert.assertNotNull(changes);
		DummyCount fooChange = changes.get("foo");
		Assert.assertNotNull(fooChange);
		assertCount(1, 0, fooChange);
		DummyCount fooBarChange = changes.get("foo/bar");
		Assert.assertNotNull(fooBarChange);
		assertCount(0, 1, fooBarChange);
	}

	private static void assertCount(int expectedCount1, long expectedCount2, DummyCount actual) {
		Assert.assertEquals(expectedCount1, actual.getCount1());
		Assert.assertEquals(expectedCount2, actual.getCount2());
	}

}
