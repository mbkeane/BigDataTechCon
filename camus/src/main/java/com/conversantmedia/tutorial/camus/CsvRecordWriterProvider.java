package com.conversantmedia.tutorial.camus;

import com.linkedin.camus.coders.CamusWrapper;
import com.linkedin.camus.etl.IEtlKey;
import com.linkedin.camus.etl.RecordWriterProvider;
import com.linkedin.camus.etl.kafka.mapred.EtlMultiOutputFormat;
import com.linkedin.camus.schemaregistry.SchemaRegistry;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;


/**
 *
 * @author Mike Keane <mkeane@conversantmedia.com>
 */
/**
 *
 *
 */
public class CsvRecordWriterProvider implements RecordWriterProvider {

	public static final String ETL_OUTPUT_RECORD_DELIMITER = "etl.output.record.delimiter";
	public static final String DEFAULT_RECORD_DELIMITER = "\n";
	protected String recordDelimiter = null;
	public final static String EXT = ".csv";
	private boolean isCompressed = false;
	private CompressionCodec codec = null;
	protected SchemaRegistry<Schema> registry;
	private Schema latestSchema;
	private String extension = "";

	public CsvRecordWriterProvider(TaskAttemptContext context) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Configuration conf = context.getConfiguration();

		if (recordDelimiter == null) {
			recordDelimiter = conf.get(ETL_OUTPUT_RECORD_DELIMITER, DEFAULT_RECORD_DELIMITER);
		}
//		isCompressed = FileOutputFormat.getCompressOutput(context);
//		if (isCompressed) {
//			Class<? extends CompressionCodec> codecClass = null;
//			if ("snappy".equals(EtlMultiOutputFormat.getEtlOutputCodec(context))) {
//				codecClass = SnappyCodec.class;
//			}
//			else if ("gzip".equals((EtlMultiOutputFormat.getEtlOutputCodec(context)))) {
//				codecClass = GzipCodec.class;
//			}
//			else {
//				codecClass = DefaultCodec.class;
//			}
//			codec = ReflectionUtils.newInstance(codecClass, conf);
//			extension = codec.getDefaultExtension();
//		}

	}

	@Override
	public String getFilenameExtension() {
		return extension;
	}

	@Override
	public RecordWriter<IEtlKey, CamusWrapper> getDataRecordWriter(TaskAttemptContext context, String fileName,
																   CamusWrapper data, FileOutputCommitter committer) throws IOException, InterruptedException {

		// If recordDelimiter hasn't been initialized, do so now
		if (recordDelimiter == null) {
			recordDelimiter = context.getConfiguration().get(ETL_OUTPUT_RECORD_DELIMITER, DEFAULT_RECORD_DELIMITER);
		}

		// Get the filename for this RecordWriter.
		Path path
				= new Path(committer.getWorkPath(), EtlMultiOutputFormat.getUniqueFile(context, fileName, getFilenameExtension()));

		//		final FSDataOutputStream writer = path.getFileSystem(context.getConfiguration()).create(path);

		FileSystem fs = path.getFileSystem(context.getConfiguration());
		DataOutputStream writer;// = fs.create(path, false);
		if (isCompressed) {
			return new ByteRecordWriter(new DataOutputStream(codec.createOutputStream(fs.create(path, false))), recordDelimiter);
		}
		else {
			return new ByteRecordWriter(fs.create(path, false), recordDelimiter);
		}

	}

	protected static class ByteRecordWriter extends RecordWriter<IEtlKey, CamusWrapper> {

		private DataOutputStream writer;
		private String recordDelimiter;

		public ByteRecordWriter(DataOutputStream out, String recordDelimiter) {
			this.writer = out;
			this.recordDelimiter = recordDelimiter;
		}

		@Override
		public void write(IEtlKey key, CamusWrapper data) throws IOException, InterruptedException {
			writer.write(((String)data.getRecord()).getBytes());
			writer.write(recordDelimiter.getBytes());
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException, InterruptedException {
			writer.close();
		}

	}

}
