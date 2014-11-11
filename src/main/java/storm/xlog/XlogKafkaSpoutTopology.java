package storm.xlog;

import storm.kafka.*;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;

import backtype.storm.task.TopologyContext;
import backtype.storm.task.OutputCollector;

import backtype.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;


import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone; 
import java.util.Locale;
import java.util.Date;
import java.util.Hashtable;  
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.text.ParseException;
import java.util.Vector;
import java.util.Properties;

import java.lang.Long.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;

public  class XlogKafkaSpoutTopology {
    public static final Logger LOG = LoggerFactory.getLogger(XlogKafkaSpoutTopology.class);


    public static class XlogBolt extends BaseBasicBolt {
    	private long intervalTime = 60;
    	private long totalThreshold = 30;
	private long scopeThreshold = 10;
    	private String topic = "";
    	private String mysqlUrl = "";
    	private String mysqlUser = "";
    	private String mysqlPassword = "";
	private boolean isStatic = true;
	private int start_datetime = 0;
	private int start_mm = 0;
	private int total = 0,statics = 0, dynamics = 0;
	Hashtable<String, Object> hashIp = new Hashtable<String, Object>(1000, 0.5F);
        Hashtable<String, Object> hashIpUrl = new Hashtable<String, Object>(1000, 0.5F);
	HashMap<String, Integer> ipWhitelist = new HashMap<String, Integer>(); 
	public void  prepare(Map stormConf, TopologyContext context) {
   		topic = (String) stormConf.get("xlog.kafka.topic.name");
   		totalThreshold = Long.parseLong((String) stormConf.get("insert.into.mysql.min.total"), 10);
   		scopeThreshold = Long.parseLong((String) stormConf.get("insert.into.mysql.max.scope"), 10);
   	        intervalTime   = Long.parseLong((String) stormConf.get("xlog.interval.time"), 10);
   		mysqlUrl       = (String) stormConf.get("mysql.url");
   		mysqlUser      = (String) stormConf.get("mysql.user");
   		mysqlPassword  = (String) stormConf.get("mysql.password");
	}

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }

        @Override
        public void execute(Tuple tuple, BasicOutputCollector collector) {
		String line = tuple.getString(0);
		String regex = "([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})\\s(.+)\\s\\-\\s\\[(.+)\\s\\+0800\\]\\s\"(.+)\\s(.+)\\s(.+)\"\\s(\\d+)\\s(\\d+)\\s\"(.+)\"\\s\"(.+)\"\\s\"(.+)\"\\s(\\d+\\.\\d+|\\d+|\\-)\\s(\\d+\\.\\d+|\\d+|\\-)";
		String ip = "";  
		String host = "-";
		String datetime = "";
		String method = "";
		String url = "";
		String code = "";
		String size = "";
                int re_time = 0;
                int mm = 0;
		boolean inWhitelist = false;
	        Pattern pattern = Pattern.compile(regex);  
	        Matcher matcher = pattern.matcher(line);  
	        while (matcher.find()) {  
	    		ip = matcher.group(1);//只取第一组  
	    		host = matcher.group(2);//只取第一组 
	    		datetime = matcher.group(3);
			method = matcher.group(4);
	    		url = matcher.group(5);
			code = matcher.group(7);
			size = matcher.group(8);
	    		total ++;

			//从日志标记提取时间
                        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss", Locale.US);
                        Date d;
                        try {
                                d = sdf.parse(datetime);
                                long l = d.getTime();
                                String str = String.valueOf(l);
                                re_time = Integer.parseInt(str.substring(0, 10));
                        } catch (ParseException e) {
                                e.printStackTrace();
                        }
                        String re_StrTime = null;
                        SimpleDateFormat sdf_1 = new SimpleDateFormat("yyyyMMddHHmm",  Locale.CHINA);
                        SimpleDateFormat sdf_mm = new SimpleDateFormat("mm",  Locale.CHINA);
                        long lcc_time = Long.valueOf(re_time);
                        re_StrTime = sdf_1.format(new Date(lcc_time * 1000L));
                        mm = Integer.parseInt(sdf_mm.format(new Date(lcc_time * 1000L)));
		
			//开始时间标记	
                        if ( start_datetime == 0) {
                                start_datetime = re_time;
                                start_mm = mm;
                        }

		        //已经在白名单的不再继续统计	
			if (ip.equals('-')) {
				continue;
			}

			inWhitelist = ipWhitelist.containsKey(ip);
 			if ( inWhitelist ) {
				continue;
			}

			//访问广度是否达到白名单阀值，是的话添加到白名单列表，并清空hashIpUrl记录以节省内存
	    		boolean urlMapIsExist = hashIpUrl.containsKey(ip);
	    		HashMap<String, Integer> mapUrl = null;
	    		mapUrl = urlMapIsExist ? (HashMap<String, Integer>) hashIpUrl.get(ip) : new HashMap<String, Integer>();
	    		Integer mapUrlSize = mapUrl.size();
	    		if ( mapUrlSize >= scopeThreshold ) {
	    			hashIpUrl.remove(ip);
	    			ipWhitelist.put(ip, 1);
	    			inWhitelist = true;
	    		}

			
	    		//是否为静态记录
	    		if (url.toLowerCase().matches(".+(\\.jpg|\\.png|\\.js|\\.gif|\\.css|\\.ico|\\.swf|\\.jpeg|\\.txt|\\.html|\\.htm){1}.*")) {
	    			statics++;
	    			isStatic = true;
	    		} else {
	    			dynamics++;
	    			isStatic = false;
	    		}


			if ( !inWhitelist ) {
				String   newUrl = "";
				if (isStatic) {
					newUrl = url;
				} else {
				    	String[] urlArr = StringUtils.split(url,"/");
				    	Integer count = urlArr.length;
				    	if (count >= 3) {
				    		newUrl =  "/"+urlArr[0]+"/"+urlArr[1];
				    	} else if (count <= 0) {
				    		newUrl = "/";
				    	} else {
				    		//Integer indexof1 = url.indexOf('.');
				    		Integer indexof2 = url.indexOf('?');
				    		Integer indexof3 = url.indexOf('=');
				    		if (indexof3 != -1 && indexof2 != -1) {
					    		newUrl =  url.substring(0, indexof3);
				    		} else {
				    			newUrl =  urlArr[0];
				    		}
				    	}	
				}
		    		mapUrl.put(newUrl, 1);
		    		hashIpUrl.put(ip, mapUrl);

			}

	    		HashMap<String, Integer> map = null;
			boolean ipMapIsExist = hashIp.containsKey(ip);
			map = ipMapIsExist ? (HashMap<String, Integer>) hashIp.get(ip) : new HashMap<String, Integer>();
			if (inWhitelist && ipMapIsExist) {
				 hashIp.remove(ip);
				 continue;
			}
			
			Integer codeFirst = Integer.parseInt(code.substring(0, 1));
			String fieldName = "";

			if ( ipMapIsExist ) {
				map.put("total", map.get("total") + 1);
				if ( isStatic ) {
					map.put("statics", map.get("statics") + 1);
				} else {
					map.put("dynamics", map.get("dynamics") + 1);	
				}
				
				if (method.equals("GET")) {
					map.put("get", map.get("get") + 1);
				} else if (method.equals("POST")) {
					map.put("post", map.get("post") + 1);
				} else if (method.equals("HEAD")) {
					map.put("head", map.get("head") + 1);
				} else {
					map.put("other", map.get("other") + 1);
				}
				
				for(int c = 2; c<=5; c++){
					fieldName = c + "xx";
					if ( codeFirst == c ) {
						map.put(fieldName, map.get(fieldName) + 1);	
					}
				}			    			
			} else {
				map.put("total", 1);
				if ( isStatic ) {
					map.put("statics", 1);
					map.put("dynamics", 0);
				} else {
					map.put("statics", 0);
					map.put("dynamics", 1);	
				}
				
		    		map.put("get", 0);
		    		map.put("post", 0);
		    		map.put("head", 0);
		    		map.put("other", 0);
				if (method.equals("GET")) {
					map.put("get", 1);
				} else if (method.equals("POST")) {
					map.put("post", 1);
				} else if (method.equals("HEAD")) {
					map.put("head", 1);
				} else {
					map.put("other", 1);
				}
				
				for(int c = 2; c<=5; c++){
					fieldName = c + "xx";
					if ( codeFirst == c ) {
						map.put(fieldName, 1);	
					} else {
						map.put(fieldName, 0);	
					}
				}		
			}
			
			if ( !inWhitelist && ipMapIsExist && mapUrlSize + 1 >= scopeThreshold) {
				mapUrlSize = mapUrl.size();
				if (  mapUrlSize >= scopeThreshold ) {
					ipWhitelist.put(ip, 1);
					hashIpUrl.remove(ip);
                                	hashIp.remove(ip);
					continue;
				}
			}
			hashIp.put(ip, map);
				
			/*System.out.println("总数：" + total + ",静态：" + statics + ",动态：" + dynamics);
	    		System.out.println("#####################################");*/
	        }

		if ( ( mm - start_mm > 0  && re_time - start_datetime >  intervalTime ) || (  mm - start_mm < 0 && re_time - start_datetime >  intervalTime ) ) {
			System.out.println(line);
			System.out.println(topic + " totals -> "+ total +" # ip totals ->" + hashIp.size() + " # whitelist totals ->" + ipWhitelist.size());
		        String data = "";
			ArrayList ipList = new ArrayList();
			long totalIp = 0;
			long valid = 0;
			long scopeSize = 0;
			for ( Iterator<String> it = hashIp.keySet().iterator(); it.hasNext(); )   { 
	        		String   key   =   (String)it.next(); 
	        		HashMap<String, Integer> value   =  (HashMap<String, Integer>)  hashIp.get(key);
				HashMap<String, Integer> hashUrl = (HashMap<String, Integer>) hashIpUrl.get(key);
			    	totalIp = value.get("total");
				scopeSize = hashUrl.size();
			    	if ( totalIp > totalThreshold && scopeSize < scopeThreshold ) {
					valid++;
			    		ipList.add("'"+topic+"','"+ key +"','"+start_datetime+"','"+re_time+"','"+value.get("total")+"','"+value.get("statics")+"','"+value.get("dynamics")+"','"+value.get("2xx")+"','"+value.get("3xx")+"','"+value.get("4xx")+"','"+value.get("5xx")+"','"+value.get("get")+"','"+value.get("post")+"','"+value.get("head")+"','"+value.get("other")+"','"+scopeSize+"'");
			    	}
				value   = null;
				hashUrl = null;
					
		        }
                        data = StringUtils.join(ipList.toArray(), "), ("); 
			try {
			 	Class.forName("com.mysql.jdbc.Driver").newInstance();
			 	Connection conn = DriverManager.getConnection(mysqlUrl, mysqlUser, mysqlPassword);
			 	Statement stmt = conn.createStatement();//创建语句对象，用以执行sql语言
			 	if ( valid > 0) {
					String sql = "INSERT INTO  `ips` (`topic`,`ip` ,`time_start` ,`time_end` ,`total` ,`statics` ,`dynamics` ,`2xx` ,`3xx` ,`4xx` ,`5xx` ,`get` ,`post` ,`head` ,`other`, `scope`) VALUES (" + data + ");";
		            		//System.out.println(sql);
			 		stmt.execute(sql);
					data = "";
					sql  = "";
  			 	}
			 	conn.close();
			} catch (Exception ex) {
			    	System.out.println("Error : " + ex.toString());
			}

                        total = 0;statics = 0;dynamics = 0;
                        hashIp = new Hashtable<String, Object>(1000, 0.5F);
			hashIpUrl = new Hashtable<String, Object>(1000, 0.5F);
			ipWhitelist = new HashMap<String, Integer>();
                        isStatic = true;
                        start_datetime = 0;
                        start_mm = 0;
		}
        }

    }

    private final BrokerHosts brokerHosts;

    public XlogKafkaSpoutTopology(String kafkaZookeeper) {
        brokerHosts = new ZkHosts(kafkaZookeeper);
    }

    public StormTopology buildTopology(String topic) {
        SpoutConfig kafkaConfig = new SpoutConfig(brokerHosts, topic, "", "xlog_storm_"+topic);
        kafkaConfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("KafkaSpout", new KafkaSpout(kafkaConfig));
        builder.setBolt("XlogBolt", new XlogBolt()).shuffleGrouping("KafkaSpout");
        return builder.createTopology();
    }

    public static void main(String[] args) throws Exception {
        if ( args == null || args.length != 1 ) {
		System.out.println("Usage:storm jar target/storm-xlog-***-jar-with-dependencies.jar  storm.xlog.XlogKafkaSpoutTopology configure_file_path");
		System.exit(0);
	}
        File file = new File(args[0]);
	if( !file.exists() ) {
		System.out.println("configure file " + args[0] + "do not exist!");
		System.exit(0);
		
	}   

 	InputStream is = new FileInputStream(file); 
        Properties prop = new Properties(); 
        prop.load(is); 
        Config config = new Config();
        for (Object key : prop.keySet()) { 
		config.put((String) key, prop.get(key));
        }
	is.close();
        String kafkaZk = (String) config.get("xlog.zookeeper.server");
        String nimbusIp = (String) config.get("xlog.nimbus.host");
	String topic = (String) config.get("xlog.kafka.topic.name");
	String debug = (String) config.get("xlog.debug");
        config.put(Config.TOPOLOGY_DEBUG, debug.toLowerCase().equals("true") ? true : false);
        config.put(Config.TOPOLOGY_TRIDENT_BATCH_EMIT_INTERVAL_MILLIS, 50);
	XlogKafkaSpoutTopology XlogkafkaSpoutTopology = new XlogKafkaSpoutTopology(kafkaZk);
        StormTopology stormTopology = XlogkafkaSpoutTopology.buildTopology(topic);
        config.setNumWorkers(1);
        config.setMaxTaskParallelism(1);
        config.setMaxSpoutPending(10000);
        config.put(Config.NIMBUS_HOST, nimbusIp);
        config.put(Config.NIMBUS_THRIFT_PORT, 6627);
        config.put(Config.STORM_ZOOKEEPER_PORT, 2181);
        config.put(Config.STORM_ZOOKEEPER_SERVERS, Arrays.asList(kafkaZk));
        StormSubmitter.submitTopology(topic, config, stormTopology);
    }
}
