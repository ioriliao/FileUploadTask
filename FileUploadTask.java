package com.scott.crash;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
public class FileUploadTask extends AsyncTask<Object, Integer, String> {  
	private String ActionUrl;
    private String FilePath;
    private String FormName;
    private HashMap<String, String> FormData=null;
    private String MyCookie;
    private long TotalSize;
    private HttpURLConnection Connection = null;  
    private DataOutputStream OutputStream = null;
    private OnMyProgressUpdateListener onProgressUpdateListener;
    private OnMyPreExecuteListener onMyPreExecuteListener;
    private OnMyPostExecuteListener onMyPostExecuteListener;
    public interface OnMyProgressUpdateListener{
		public void OnProgressUpdate(Integer... progress); 
	}
    
    public interface OnMyPreExecuteListener{
		public void OnPreExecute(long fileSize); 
	}
    
    public interface OnMyPostExecuteListener{
		public void OnPostExecute(String result); 
	}
    
    public FileUploadTask(HashMap<String,String> fileUploadConfig, 
    		final HashMap<String,Object> postData){
    	FileUploadTaskInit(fileUploadConfig,postData);
    }
    
    public FileUploadTask(HashMap<String,String> fileUploadConfig, 
    		final HashMap<String,Object> postData, 
    		OnMyPreExecuteListener onMyPreExecuteListener){
    	
    	FileUploadTaskInit(fileUploadConfig,postData);
    	this.onMyPreExecuteListener=onMyPreExecuteListener;
    }
    
    public FileUploadTask(HashMap<String,String> fileUploadConfig, 
    		final HashMap<String,Object> postData, 
    		OnMyPreExecuteListener onMyPreExecuteListener,
    		OnMyProgressUpdateListener onMyProgressUpdateListener){
    	
    	FileUploadTaskInit(fileUploadConfig,postData);
    	this.onMyPreExecuteListener=onMyPreExecuteListener;
		this.onProgressUpdateListener=onMyProgressUpdateListener;
    }
    
    public FileUploadTask(HashMap<String,String> fileUploadConfig, 
    		final HashMap<String,Object> postData, 
    		OnMyPreExecuteListener onMyPreExecuteListener,
    		OnMyProgressUpdateListener onMyProgressUpdateListener, 
    		OnMyPostExecuteListener onMyPostExecuteListener){
    	
    	FileUploadTaskInit(fileUploadConfig,postData);
    	this.onMyPreExecuteListener=onMyPreExecuteListener;
		this.onProgressUpdateListener=onMyProgressUpdateListener;
		this.onMyPostExecuteListener=onMyPostExecuteListener;
    }
    
    
	public void FileUploadTaskInit(HashMap<String,String> fileUploadConfig, 
    		final HashMap<String,Object> postData){
    	this.FilePath=fileUploadConfig.get("file_path");
    	this.ActionUrl=fileUploadConfig.get("action_url");
    	this.FormName=fileUploadConfig.get("form_name");
    	File uploadFile = new File(this.FilePath);  
    	this.TotalSize = uploadFile.length();
    	if(fileUploadConfig.containsKey("cookie")){
    		this.MyCookie=fileUploadConfig.get("cookie");
    	}else{
    		this.MyCookie="";
    	}
    	
    	if(postData!=null){
    		String pData="{}";
    		if(postData.get("data")!=null){
    			
    			pData=JSONObject.toJSONString(postData.get("data"));//JSON.toJSONString(postData.get("data"));
    		} 
	    	this.FormData=new HashMap<String,String>();
	    	this.FormData.put("data", pData);
    	}else{
    		this.FormData=null;
    	}
    	
    	//SharedPreferences userInfo =context.getSharedPreferences("user_info", 0);  
    	//MyCookie = userInfo.getString("cookie", "");
    }
    
    //写入表单数据 
    protected DataOutputStream writeFormData(HashMap<String,String> map,DataOutputStream targetStream){
    	String end="\r\n";				//回车换行符 
    	String boundary ="*****";		//分隔符  
    	String twoHyphens ="--";		//分隔符前后缀  
    	try { 
	        if(map != null){  
	            Set<String> keys = map.keySet();  
	            for (Iterator<String> it = keys.iterator(); it.hasNext();) {  
	                String key = it.next();  
	                String value = map.get(key);  
	                targetStream.writeBytes(twoHyphens + boundary + end);  
	                targetStream.writeBytes("Content-Disposition: form-data; "+ "name=\""+key+"\""+end);  
	                targetStream.writeBytes(end);  
	                targetStream.writeBytes(value);  
	                targetStream.writeBytes(end);  
	            }  
	        }
    	} catch (Exception ex) {  
        	Log.e("com.scott.crash.FileUploadTask",ex.getMessage());
        }  
    	return targetStream;
    }
    
    //写入上传的文件数据
    protected DataOutputStream writeFileData(String filePath,String formName, DataOutputStream targetStream){
    	String end="\r\n";			//回车换行符 
    	String boundary ="*****";	//分隔符  
    	String twoHyphens ="--";	//分隔符前后缀  
    	long length = 0;  
        int progress;  
        int bytesRead, bytesAvailable, bufferSize;  
        byte[] buffer;  
        int maxBufferSize = 10 * 1024;// 10KB  
        File uFile = new File(filePath);  
    	long uTotalSize = uFile.length();
    	try { 
    		targetStream.writeBytes(twoHyphens + boundary + end);  
    		targetStream.writeBytes("Content-Disposition: form-data; "+"name=\""+formName+"\";filename=\";"+uFile.getName() +"\""+ end);  
    		targetStream.writeBytes(end); 
            FileInputStream fileInputStream = new FileInputStream(uFile);  
            bytesAvailable = fileInputStream.available();  
            bufferSize = Math.min(bytesAvailable, maxBufferSize);//设置每次写入的大小  
            buffer = new byte[bufferSize]; 
            
            bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
            while (bytesRead > 0) {  
            	targetStream.write(buffer, 0, bufferSize);  
                length += bufferSize; 
                //Thread.sleep(500);
                progress = (int) ((length * 100) / uTotalSize);  
                publishProgress(progress,(int)length);  
                bytesAvailable = fileInputStream.available();  
                bufferSize = Math.min(bytesAvailable, maxBufferSize);  
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
            }  
            targetStream.writeBytes(end);  
            targetStream.writeBytes(twoHyphens + boundary + twoHyphens + end);  
            fileInputStream.close();
            publishProgress(100,(int)length); 
    	} catch (Exception ex) {  
        	Log.e("com.scott.crash.FileUploadTask",ex.getMessage());
        }  
    	return targetStream;
    }
    
    protected String getHttpResponseText(HttpURLConnection connection){
    	String result="";
    	try{
	    	InputStream is = connection.getInputStream();  
	        int ch;  
	        StringBuffer b =new StringBuffer();  
	        while((ch = is.read()) != -1){  
	        	b.append((char)ch);  
	        }  
	        result = b.toString().trim();     
	        is.close();  
    	}catch(Exception ex){
    		Log.e("com.scott.crash.FileUploadTask",ex.getMessage());
    	}
        return result;
    }
    
    @Override  
    protected void onPreExecute() {  
    	if(onMyPreExecuteListener!=null){
    		onMyPreExecuteListener.OnPreExecute(this.TotalSize);
    	}
    }  

    @Override  
    protected String doInBackground(Object... arg0) {  
        String result = null; 
        String boundary ="*****";		//分隔符 
        try {  
            URL url = new URL(this.ActionUrl);  
            Connection = (HttpURLConnection) url.openConnection();  
            Connection.setChunkedStreamingMode(128 * 1024);// 128KB
            Connection.setDoInput(true);  
            Connection.setDoOutput(true);  
            Connection.setUseCaches(false);
            Connection.setRequestMethod("POST");  
            Connection.setRequestProperty("Connection", "Keep-Alive");  
            Connection.setRequestProperty("Cookie", MyCookie); 
            Connection.setRequestProperty("Charset", "UTF-8");  
            Connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
             
            OutputStream = new DataOutputStream(Connection.getOutputStream());
            OutputStream=writeFormData(this.FormData,OutputStream);
            OutputStream=writeFileData(this.FilePath,this.FormName,OutputStream);
            
            result=getHttpResponseText(Connection);  
            OutputStream.flush();  
            OutputStream.close();  
        } catch (Exception ex) {  
        	Log.e("com.scott.crash.FileUploadTask",ex.getMessage());
        }  
        return result;  
    }

    @Override  
    protected void onProgressUpdate(Integer... progress) {  
    	if(this.onProgressUpdateListener!=null){
    		this.onProgressUpdateListener.OnProgressUpdate(progress[0],progress[1]);
    	}
    }  

    protected void onPostExecute(String result) {
    	if(onMyPostExecuteListener!=null){
    		onMyPostExecuteListener.OnPostExecute(result);
    	}
    }  
}    
