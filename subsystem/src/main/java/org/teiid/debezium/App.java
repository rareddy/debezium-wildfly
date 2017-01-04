package org.teiid.debezium;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;

/**
 * Hello world!
 *
 */
public class App {
	private final static Logger logger = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) throws Exception{
		Configuration config = Configuration.create()
				.with("connector.class", "io.debezium.connector.mysql.MySqlConnector")
				.with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
				.with("offset.storage.file.filename", "/home/rareddy/testing/debezium/storage/offset.dat")
				.with("offset.flush.interval.ms", 60000).with("name", "my-sql-connector")
				.with("database.hostname", "localhost").with("database.port", 3306).with("database.user", "debezium")
				.with("database.password", "dbz").with("server.id", 85744).with("database.server.name", "teiid-connector")
				.with("database.history", "io.debezium.relational.history.FileDatabaseHistory")
				.with("database.history.file.filename", "/home/rareddy/testing/debezium/storage/dbhistory.dat")
				.with("", "").build();

		Consumer<SourceRecord> c = new Consumer<SourceRecord>() {
			public void accept(SourceRecord t) {
				System.out.println("record = " + ((Struct)t.key()).getInt32("id"));
			}
		};

		EmbeddedEngine engine = EmbeddedEngine.create()
				.using(config)
				.notifying(c)
				.build();

		ExecutorService executor = Executors.newFixedThreadPool(10);
		executor.execute(engine);

		try {
		    while (!engine.await(30, TimeUnit.SECONDS)) {
		        logger.info("Wating another 30 seconds for the embedded enging to shut down");
		    }
		} catch ( InterruptedException e ) {
		    Thread.interrupted();
		}
	}

}
