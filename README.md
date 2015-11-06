# Flume, Kafka + Camus Tutorial

### The Motivation Behind this Tutorial
Part of my teams responsiblity at [Conversant](https://conversantmedia.com) is maintain Flume flows which collect 100 billion log lines per day.  In the spring of 2015 our current Flume agents were writing to (or 'sinking' in Flume parlance) a Hadoop cluster using the  [MapR](https://mapr.com) distribution.  A new [Cloudera](https://conversantmedia.comhttp://www.cloudera.com/content/www/en-us.html) hadoop cluster had been stood up and I was tasked with the "trivial" task of migrating the Flume flow end point from the MapR cluster to the Cloudera cluster.  Beyond the distributions differenced the work load difference was vastly different. The MapR cluster was owned by my team.  All jobs run on this cluster were map reduce jobs owned by my team.  The Cloudera cluster was shared by multiple teams including an army of decision scientists learning how to write [Spark](http://spark.apache.org) jobs.  My trivial task became my spring 2015 nightmare.  I had heard stories of Flume not playing well with Cloudera and [Hortonworks](http://hortonworks.com) hadoop distributions.  Rarely had we seen a problem on the MapR cluster.  Yes the Flume agents sinking to the MapR cluster would backup when the cluster was under heavy load, particulary during the "copy" phase of large mapreduce jobs but ultimately the copy phase completed and the Flume backup drained with just a delay in data arrival.  On the new Cloudera cluster the results were not the same.  Resource demands were much higher.  Flume has no YARN integration and would consistently fail to open, close or rename files when writing to the cluster.  Thousands of timeout exceptions were written to the namenode and datanode logs.  Untimately there was data loss and data corruption.

The other half of the data team at [Conversant](https://conversantmedia.com) was working with [Kafka](http://kafka.apache.org) and [Storm](http://storm.apache.org) and having great results.  [Flafka](http://blog.cloudera.com/blog/2014/11/flafka-apache-flume-meets-apache-kafka-for-event-processing) or Flume integration with Kafka was well underway and scheduled to be included in the next [Flume](http://flume.apache.org) release, 1.6.  Fluming into Kafka would decouple Flume from the Cloudera cluster and colleagues of mine had heard good things about [LinkedIn's](https://www.linkedin.com) open source Kafka to HDFS pipeline, [Camus](https://github.com/linkedin/camus).  Camus is a map reduce framework runs map reduce jobs with Kafka as an source and HDFS as an destination.  As a map reduce job it could run in its own queue with resource management by YARN.  Could this solve our problem.  The answer was a resounding yes.  In less than 6 weeks we were live and today we are "Camusing" 100 billion log lines from Kafka to HDFS daily.

Based on the success we had integrating Flume, Kafka and Camus into our log processing a colleague of mine suggested I put together tutorial for [BigDataTechCon](http://www.bigdatatechcon.com/).  This is the output of that effort.


The integration of Flume + Kafka + Camus does require implementing a couple Camus interfaces.  At Conversant we have created quite a few Flume pluggins as well.  My goal with this tutorial is to get users up and running with Flume, Kafka and Camus in a development environment as quickly as possible.  While implementing two Camus interfaces is all it would take to demonstrate a full data collection pipeline I have included two maven projects in this repo, "flume" and "camus".  An interesting "Flafka" caveat prompted me to also make a simple flume pluggin (Interceptor interface implementation).  More on this later in the tutorial. Also, I wanted to get users up and running in an environment where they could step through their code in a debuggger.  Kafka is used straight out of the box.  Flume will from within your IDE.  Camus jobs being map reduce job must be submitted to hadoop, The Camus job will run in local mode with a demonstration of running in local mode with remote debugging enabled.

### Requirements
- [x]  Linux environment.  All the work in here has been done on a laptop running Ubuntu in IntelliJ IDE.
- [x]  Maven - both project are maven projects.
- [ ]  Camus - To my knowledge Camus is not in an public Maven repository.  Conversant added Camus to their internal repository and if your organization uses Camus you'll probably want to do the same.
- [ ] Kafka - To really demonstrate Flume's Kafka integration we will use Kafka sources, channels and sinks for Flume.
- [ ] Hadoop - you need an Hadoop client to run Camus.

In this tutorial I do all the work in /tmp/,  I simply extract hadoop and kafka from /tmp/.  Since /tmp/ is removed each time I reboot nothing is left cluttering up my disk drive when I reboot.  If you are going to continue working with this over time you will want to replace /tmp/ with a more permanent location.  All configurations and paths in this tutorial give explicit full paths (ex /tmp/BigDataTechCon/config/log4j.properties).  If you are running out of someplace other than /tmp/ you  will need to update your paths accordingly.

### Downloads
- [x]  Clone and build the Camus repository
```
cd /tmp/
git clone https://github.com/linkedin/camus.git
cd camus
mvn install
```
- [x]  Clone and build the tutorial repository
```
cd /tmp/
git clone https://github.com/mbkeane/BigDataTechCon.git
cd /tmp/BigDataTechCon/flume
mvn install
cd /tmp/BigDataTechCon/camus
mvn install
```

- [x]  Download Kafka from  `https://www.apache.org/dyn/closer.cgi?path=/kafka/0.8.2.2/kafka_2.10-0.8.2.2.tgz`
```
cd /tmp
tar -xzvf ~/Downloads/kafka_2.10-0.8.2.2.tgz
cd kafka_2.10-0.8.2.2
```

- [x]  Download Hadoop from `http://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-2.7.1/hadoop-2.7.1.tar.gz`
```
cd /tmp
tar -xzvf ~/Downloads/hadoop-2.7.1.tar.gz
```

*NOTE* this tutorial is not meant to be an indepth study of Flume, Kafka or Camus.  For a better understanding of Flume and its use cases I recommend Steve Hoffman's [Apache Flume: Distributed Log Collection for Hadoop - Second Edition](http://www.amazon.com/Apache-Flume-Distributed-Collection-Hadoop/dp/1784392170/ref=sr_1_2?s=books&ie=UTF8&qid=1446338183&sr=1-2&keywords=flume).

### The basic anatomy of a Flume Agent and a Flume Flow


Exercise 1  Flume Spooling Agent sink to Kafka Topic.

A common use case
