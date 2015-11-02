package com.conversantmedia.tutorial;

import com.google.common.base.Charsets;
import junit.framework.TestCase;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.interceptor.InterceptorBuilderFactory;
import org.junit.Assert;


/**
 * Created by mkeane on 11/1/15.
 */
public class ColumnFilterInterceptorTest extends TestCase {

	public void testIntercept() throws Exception {
		Context ctx = new Context();
		ctx.put("type", "com.conversantmedia.tutorial.ColumnFilterInterceptor$Builder");
		ctx.put("columns", "1,4,7");
		Interceptor.Builder builder = InterceptorBuilderFactory.newInstance("com.conversantmedia.tutorial.ColumnFilterInterceptor$Builder");
		builder.configure(ctx);
		Interceptor interceptor = builder.build();

		Event event = EventBuilder.withBody("HX479554,10/23/2014 07:31:14 PM,077XX S VERNON AVE,2027,NARCOTICS,POSS: CRACK,RESIDENCE,Y,N,624,6,18,1180552,1853493,,,", Charsets.UTF_8);
		event = interceptor.intercept(event);
		String val = new String(event.getBody(), "UTF-8");
		String expectedVal="10/23/2014 07:31:14 PM,NARCOTICS,Y";
		Assert.assertNotNull(val);
		Assert.assertEquals("Column Filter failed", expectedVal, val);
	}
}