package com.haier.redis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ReadRedisConf {
	 public static String txt2String(File file){
	        StringBuilder result = new StringBuilder();
	        String[] temp = null;
	        try{
	            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
	            String s = null;
	            int lines = 0;
	            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
	            	lines++;
	            	if(lines==526)
	            		//System.out.println(s);// 打印输出信息 
	                result.append(s);
	            }
            	temp= result.toString().split(" ");
	            br.close();    
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return temp[1].toString();
	    }
	    
	    public static void main(String[] args){
	        File file = new File("F:/redis/redis.windows.conf");
	        System.out.println(txt2String(file));
	    }
	
}
