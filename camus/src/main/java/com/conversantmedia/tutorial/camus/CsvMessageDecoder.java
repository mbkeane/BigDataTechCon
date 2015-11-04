package com.conversantmedia.tutorial.camus;

import com.linkedin.camus.coders.CamusWrapper;
import com.linkedin.camus.coders.Message;
import com.linkedin.camus.coders.MessageDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.avro.io.DecoderFactory;
import org.apache.log4j.Logger;

/**
 * A simple decoder to parse out date, convert it to epoch and set the timestamp.
 * <p>
 * @author Mike Keane <mkeane@conversantmedia.com>
 */
public class CsvMessageDecoder extends MessageDecoder<Message, String> {

	private static final Logger log = Logger.getLogger(CsvMessageDecoder.class);
	protected DecoderFactory decoderFactory;
	// Property for format of timestamp in JSON timestamp field.
	private Calendar calendar;
	// Sample Data 10/23/2014 07:31:14 PM
	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

	@Override
	public void init(Properties props, String topicName) {
		super.init(props, topicName);
		calendar = Calendar.getInstance(TimeZone.getTimeZone("CST"));
		decoderFactory = DecoderFactory.get();
	}

	private long getTimeStamp(String dateStr) {

		long timestamp = System.currentTimeMillis();

		try {
			Date date = df.parse(dateStr);
			calendar.setTime(date);
			timestamp = calendar.getTimeInMillis();
		}
		catch (ParseException e) {
			log.info("Failed to parse timestamp");
		}
		return timestamp;
	}

	@Override
	public CamusWrapper<String> decode(Message message) {
		long timestamp = 0;
		String payloadString;

		payloadString = new String(message.getPayload());
		String[] array = payloadString.split(",");

		timestamp = getTimeStamp(array[0]);

		return new CamusWrapper<>(payloadString, timestamp);

	}
}

