package com.haier.redis;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;


public class ChangeRedis {
	public static final Jedis jedis = new Jedis("127.0.0.1",6379);
	public static final Jedis jedis2 = new Jedis("127.0.0.1",7001);
	static Long[] used_memory = new Long[3];	
	
	/*
	 * redis操作Map，定义添加数据的方法
	 */
	public void addMachineData(){
		Date date=new Date();
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		//将设备名作为哈希数组名，采集点+时间戳作为key，value是采集值
		Map<String, String> map = new HashMap<String, String>();
		StringBuilder temper = new StringBuilder();
		temper.append(simpleDateFormat.format(date).toString()).append("temperature");
		//System.out.println(temper);
		StringBuilder pressure = new StringBuilder();
		pressure.append(simpleDateFormat.format(date).toString()).append("pressure");
		//System.out.println(pressure);
		StringBuilder power = new StringBuilder();
		pressure.append(simpleDateFormat.format(date).toString()).append("power");
		System.out.println(power);
		long startTime = System.currentTimeMillis();
		for(int i=0;i<100000;i++){
			Date date1=new Date();
			DateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String timeValue = simpleDateFormat1.format(date1);
			String temperValue = String.valueOf(55);
			String pressureValue = String.valueOf(130);
			String powerValue = String.valueOf(100+i);
			
			map.put("time", timeValue);
			map.put("temper", temperValue);	
			map.put("pressure", pressureValue);
			map.put("power", powerValue);
			
			jedis.hmset("machine01", map);
			List<String> rsmap = jedis.hmget("machine01", "time","temper","pressure","power");
			
			//try {
				//Thread.sleep(1000);
				
				
				System.out.println("machine01的运行参数："+" "+"时间"+rsmap.get(0)+" "+"温度"+rsmap.get(1)+" "
									+"压力"+rsmap.get(2)+" "
									+"总功率"+rsmap.get(3));
				
				
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}
		long endTime = System.currentTimeMillis();
		System.out.println("程序执行的时间，耗时："+(endTime-startTime)+"ms");
	}
	
	/*
	 * 操纵命令，打印输出used_memory内容，关注点在于内存使用率(内存使用率)
	 */
	public long redisCmd(){
		String[] cmd = new String[] { "cmd.exe", "/C", "F:\\redis\\redis-cli info memory" }; 
	    
		//Long[] used_memory = new Long[3];
	    Runtime run = Runtime.getRuntime();//返回与当前 Java 应用程序相关的运行时对象  
	    StringBuilder result = new StringBuilder();
	    String[] temp = null;
	        try {  
	            Process p = run.exec(cmd);// 启动另一个进程来执行命令  
	            BufferedInputStream in = new BufferedInputStream(p.getInputStream());  
	            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));  
	            String s = null; 
	            int lines = 0;
	            while ((s = inBr.readLine()) != null){
	            	//获得命令执行后在控制台的输出信息  
	                lines++;
//	                if(lines==3){
//	                	System.out.println(s);// 打印输出信息 
//	                	String[] sourceStrArray = s.split(":");
//	                	used_memory[0] = Long.valueOf(sourceStrArray[1]);
//	                }  
	                if(lines==3)
	                result.append(s);
	            }
	            temp= result.toString().split(":");
	            //检查命令是否执行失败。  
	            if (p.waitFor() != 0) {  
	                if (p.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束  
	                    System.err.println("命令执行失败!");  
	            }  
	            inBr.close();  
	            in.close();  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }
	        return Long.parseLong(temp[1]);
	}
	
	/*
	 * 设置持久化策略
	 * 将redis中的数据存入mysql中
	 * 持久化的时间要低于数据写入的时间
	 */
	
	public static void batchInsert(Connection conn) {  
        try {  
              String sql = "insert into machine01(time,temper,pressure,power)"  
                    + " values (?,?,?,?)"; // 插入数据的sql语句  
              PreparedStatement pstmt=conn.prepareStatement(sql);
              long startTime=System.currentTimeMillis();
              for(int i=0;i<100000;i++){
            	Date date2=new Date();
      			DateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                pstmt.setString(1, simpleDateFormat2.format(date2));
                pstmt.setString(2, String.valueOf(10+i));
                pstmt.setString(3, String.valueOf(45+i));
                pstmt.setString(4, String.valueOf(100+i));
                pstmt.addBatch();//添加到批量处理
              }
              int[] result=pstmt.executeBatch();
              System.out.println("总共耗时："+(System.currentTimeMillis() - startTime)+"ms");
              pstmt.close();   //关闭数据库连接  
         } catch (SQLException e) {  
            e.printStackTrace(); 
        } 
    }

	 //数据库连接
	public static Connection getConnection(String user, String pass) {
	          Connection conn = null;//声明连接对象
	          String driver = "com.mysql.jdbc.Driver";// 驱动程序类名
	          String url = "jdbc:mysql://127.0.0.1:3306/db_history?" // 数据库URL
	                     + "useUnicode=true&characterEncoding=UTF8";// 防止乱码
	   try {
	        Class.forName(driver);// 注册(加载)驱动程序
	        conn = DriverManager.getConnection(url, user, pass);// 获取数据库连接
	       } catch (Exception e) {
	         e.printStackTrace();
	      }
	        return conn;
	}
	
	//释放数据库连接
	public static void releaseConnection(Connection conn) {
	     try {
	          if (conn != null)
	               conn.close();
	         } catch (Exception e) {
	           e.printStackTrace();
	         }
	 }
	
	public static void main(String[] args) {
		System.out.println("连接成功");
        System.out.println("开始写入数据...");
        Date date3 = new Date();
    	DateFormat simpleDateFormat3 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	Connection conn = getConnection("root", "123456");    // 获取数据库连接
        //往redis 1中写入数据
        ChangeRedis cr = new ChangeRedis();
        cr.addMachineData();
        //同时往mysql中存数据
        batchInsert(conn);
        //插入数据时，在时间周期内查看redis的内存使用率 maxmemory3221225472
        
        System.out.println(cr.redisCmd());
        //监控redis的内存使用量，如果超出maxmemory，就切换到另外一台数据库
        File file = new File("F:/redis/redis.windows.conf");
        long usedmemory=cr.redisCmd();
        long maxmemory = Long.parseLong(ReadRedisConf.txt2String(file));
        String key = "tu";
        if(usedmemory>maxmemory){
        	System.out.println("Redis1内存使用量达到阈值");
        	System.out.println(simpleDateFormat3.format(date3)+"------->切换Redis......");
        	System.out.println(simpleDateFormat3.format(date3)+"------->切换成功......");
        	System.out.println("开始向第二个Redis中写入数据，同时将第一个Redis中的数据持久化到数据库中");
        	
        	System.out.println("开始写入数据...");
        	for(int j=1000001;j<1000050;j++){
				jedis2.set(key+j,"tuzki-demo"+j);		//存数据
			      //Thread.sleep((long) (0.2*1000));
			      // 获取存储的数据并输出
			      System.out.println("Redis2 存储的数据为: "+ jedis2.get(key+j));
			} 
        	System.out.println("数据写入成功");
        }else{
        	System.out.println("Redis1中内存使用量没有达到阈值");
        	System.out.println("继续写入数据");
        	
        }
        
        
	}
}
