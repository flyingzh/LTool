package xiaoliang.ltool.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import xiaoliang.ltool.constant.Constant;

/**
 * 网络数据获取子线程
 * @author LiuJ
 *
 */
public class HttpTaskRunnable implements Runnable {

	/**
	 * 回调函数
	 */
	private CallBack callBack;
	/**
	 * 请求参数集合
	 */
	private RequestParameters parameters;
	/**
	 * 文件大小
	 */
	private int fileSize = 0;
	/**
	 * 下载文件大小
	 */
	private int downLoadFileSize = 0;
	/**
	 * 文件名
	 */
	private String filename = "";

	public HttpTaskRunnable(CallBack callBack, RequestParameters parameters) {
		super();
		this.callBack = callBack;
		this.parameters = parameters;
	}
	@Override
	public void run() {
		try {
			String success;
			switch (parameters.getAccessType()) {
				case RequestParameters.ACCESS_TYPE_DOWNLOAD://下载
					if(parameters.getDownloadFileName()==null||parameters.getDownloadFileName().equals("")){
						downFile(parameters.getUrl(), parameters.getDownloadPath());
					}else{
						downFile(parameters.getUrl(), parameters.getDownloadPath(),parameters.getDownloadFileName());
					}
					break;
				case RequestParameters.ACCESS_TYPE_OBJECT://返回对象
//					String json = HttpUtil.doPost(parameters.getParameters(), parameters.getUrl());
//					success = JsonUtils.fromJson(json, parameters.getCla());
//					callBack.success(success);
//					break;
				case RequestParameters.ACCESS_TYPE_STRING://返回字符串
					if(parameters.getParameters()!=null&&parameters.getParameters().size()>0){
						success = HttpUtil.doPost(parameters.getParameters(), parameters.getUrl());
					}else{
						success = HttpUtil.doGet(parameters.getUrl());
					}
					callBack.success(callBack.str2Obj(success));
					break;
				case RequestParameters.ACCESS_TYPE_UPLOAD://上传
					//TODO
//					callBack.error(Constant.HttpTaskError, "上传功能尚未编写");
					break;
				default:
					break;
			}
		} catch (Exception e) {
			if(callBack!=null){
				callBack.error(Constant.HttpTaskError, e.toString());
			}
		}
	}

	private void downFile(String url, String path) throws IOException {
		// 下载函数
		filename = url.substring(url.lastIndexOf("/") + 1);
		downFile(url,path,filename);
	}

	private void downFile(String url, String path,String name) throws IOException {
		InputStream is = null;
		FileOutputStream fos = null;
		Log.d("downFile","url:"+url+",path:"+path+",name:"+name);
		try {
			File filePath = new File(path);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			// 下载函数
			filePath = new File(path,name);
			if (!filePath.exists()) {
				filePath.createNewFile();
			}
			// 获取文件名
			URL myURL = new URL(url);
			URLConnection conn = myURL.openConnection();
			conn.connect();
			is = conn.getInputStream();
			fileSize = conn.getContentLength();// 根据响应获取文件大小
			if (fileSize <= 0){
				parameters.onLoadError(new RuntimeException("无法获取文件大小"), Constant.DownLoadSizeNotFind);
				if(callBack!=null)
					callBack.error(Constant.DownLoadSizeNotFind, "无法获取文件大小");
				return;
			}
			if (is == null){
				parameters.onLoadError(new RuntimeException("下载链接为空"), Constant.DownLoadStreamIsNull);
				if(callBack!=null)
					callBack.error(Constant.DownLoadStreamIsNull, "下载链接为空");
				return;
			}
			fos = new FileOutputStream(filePath);
			// 把数据存入路径+文件名
			byte buf[] = new byte[1024];
			downLoadFileSize = 0;
			do {
				// 循环读取
				int numread = is.read(buf);
				if (numread == -1) {
					break;
				}
				fos.write(buf, 0, numread);
				downLoadFileSize += numread;
				if(fileSize>0)
					parameters.setProgress(downLoadFileSize/fileSize);
			} while (true);
			parameters.onLoadSeccess(filePath.getPath());
			if(callBack!=null)
				callBack.success("success");
		} catch (Exception ex) {
			Log.d("downFile",ex.getMessage());
			parameters.onLoadError(ex, Constant.DownLoadError);
			if(callBack!=null)
				callBack.error(Constant.DownLoadError, "下载失败");
		}finally{
			if(is!=null)
				is.close();
			if(fos!=null)
				fos.close();
		}
	}

	/**
	 * 数据获取回调接口
	 * @author LiuJ
	 */
	public interface CallBack<T>{
		public void success(T object);
		public void error(int code, String msg);
		public T str2Obj(String str);
	}

	public class SimpleCallBack<T> implements CallBack<T>{

		@Override
		public void success(T object) {
		}
		@Override
		public void error(int code, String msg) {
		}

		@Override
		public T str2Obj(String str) {
			return null;
		}
	}

}
