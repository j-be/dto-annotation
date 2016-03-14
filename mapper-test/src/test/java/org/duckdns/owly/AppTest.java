package org.duckdns.owly;

import org.duckdns.owly.domain.SourceClass;
import org.duckdns.owly.domain.TargetClass;
import org.duckdns.owly.domain.mapper.TargetClassToSourceClassMapper;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testMapper() {
		SourceClass src = new SourceClass();
		TargetClass trg = null;

		src.setId(128);
		trg = TargetClassToSourceClassMapper.toDto(src);
		Assert.assertEquals(src.getId(), trg.getId());
	}
}
