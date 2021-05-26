package com.like.test.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.baidu.aip.ocr.AipOcr;

public class Test {

    public static final String APP_ID = "23552975";
    public static final String API_KEY = "CS6wkhbrmjhiu9KUx2f6E51t";
    public static final String SECRET_KEY = "useckvAI6kfMLhBDGOjwfAVrpgSNNI5o";
    private static final String dir = "/Users/like/Desktop/work/";

    public static void main(String[] args) throws IOException {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 6, 5L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(4));

        // 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<>();
        options.put("detect_direction", "true");
        options.put("detect_risk", "false");

        List<String> strings = FileUtils.readLines(new File("/Users/like/Desktop/test.txt"), "UTF-8");
        System.out.println("总共需处理："+strings.size()+"个文件");
        for (int i = 0; i < strings.size(); i++) {
            String[] strs = strings.get(i).split("\\|");
            String url = strs[1];
            String id = strs[0];
            String filename = System.currentTimeMillis()+".jpg";
            ImgDownload.downloadHttpUrl(url,dir,filename);
            threadPoolExecutor.execute(new OCRTask(dir+filename,id,client));
            System.out.println("正在处理第："+(i+1)+"个 ");
        }
        System.out.println("文件读取完毕！等待识别···");
        while (true){
            if (threadPoolExecutor.getActiveCount() == 0) {
                System.out.println("文件全部识别完毕！！");
                threadPoolExecutor.shutdown();
                break;
            }
        }

    }
}
