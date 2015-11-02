package com.conversantmedia.tutorial;

import java.util.List;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mkeane on 11/1/15.
 */
public class DeleteTopicHeaderInterceptor implements Interceptor {

	private static final Logger logger
			= LoggerFactory.getLogger(DeleteTopicHeaderInterceptor.class);

	public void initialize() {
		// no-op
	}

	public Event intercept(Event event) {
		event.getHeaders().remove("topic");
		return event;
	}

	/**
	 * Read java doc for this method - if you return null things go bad...
	 * @param events
	 * @return
	 */
	public List<Event> intercept(List<Event> events) {
		for (Event event : events) {
			intercept(event);
		}
		return events;
	}

	public void close() {
		// no-op
	}

	/**
	 * Builder which builds new instance of the ChannelBifurcationInterceptor.
	 */
	public static class Builder implements Interceptor.Builder {

		public void configure(Context context) {
		}

		@Override
		public Interceptor build() {
			logger.info("Creating DeleteTopicHeaderInterceptor");
			return new DeleteTopicHeaderInterceptor();
		}
	}

}
