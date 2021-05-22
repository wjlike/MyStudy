package com.like.study.flink.wc;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 流处理文件  无界流
 */
public class  StreamWordCount {
    public static void main(String[] args) throws Exception {
        //创建流处理执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        //设置并行处理数据线程
        env.setParallelism(1);

//        String filePath = "/Users/like/ideaPrjoect/my/study/MyStudy/flink/src/main/resources/hello.txt";
//        //从文件中读取数据
//        DataStream<String> dataStream = env.readTextFile(filePath);

        //从parameter tool 工具从程序启动参数中提取配置项
        ParameterTool parameterTool = ParameterTool.fromArgs(args);

//        String host = parameterTool.get("host");
//        int port = parameterTool.getInt("port");


        //从socket 文本流读取数据
        DataStream<String> inputStream = env.socketTextStream("localhost",7777);
        //基于数据流进行计算
        DataStream<Tuple2<String, Integer>> dataStream1 = inputStream.flatMap(new WordCount.MyflatMapper())
                .keyBy(0)
                .sum(1);
        dataStream1.print();

        env.execute();
    }
}
