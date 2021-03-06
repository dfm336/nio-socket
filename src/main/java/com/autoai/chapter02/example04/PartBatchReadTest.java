package com.autoai.chapter02.example04;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: zhukaishengy
 * @Date: 2020/6/1 10:24
 * @Description:
 */
@Slf4j
public class PartBatchReadTest {

    /**
     * 验证long read(ByteBuffer[] dsts, int offset, int length)方法返回值的意义
     * 朱开生   朱开生
     */
    @Test
    public void test1() {
        try (
            FileInputStream fis = new FileInputStream("/Users/zhukaishengy/StudyWorkSpace/nio-socket/src/main/java/com/autoai/chapter02/file/a.txt");
            FileChannel channel = fis.getChannel()
        ){
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(3);
            ByteBuffer byteBuffer2 = ByteBuffer.allocate(3);
            ByteBuffer[] buffers = new ByteBuffer[]{byteBuffer1, byteBuffer2};
            long read1 = channel.read(buffers, 0, 2);
            log.info("read1:{}", read1);

            ByteBuffer byteBuffer3 = ByteBuffer.allocate(6);
            ByteBuffer byteBuffer4 = ByteBuffer.allocate(9);
            ByteBuffer[] buffers2 = new ByteBuffer[]{byteBuffer3, byteBuffer4};
            long read2 = channel.read(buffers2, 0, 2);
            log.info("read2:{}", read2);

            byteBuffer1.clear();
            byteBuffer2.clear();
            long read3 = channel.read(buffers, 0, 2);
            log.info("read1:{}", read3);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * 验证long read(ByteBuffer[] dsts, int offset, int length)方法是从通道的当前位置开始读取的
     */
    @Test
    public void test2() {
        try (
            FileInputStream fis = new FileInputStream("/Users/zhukaishengy/StudyWorkSpace/nio-socket/src/main/java/com/autoai/chapter02/file/a.txt");
            FileChannel channel = fis.getChannel()
        ){
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(3);
            ByteBuffer byteBuffer2 = ByteBuffer.allocate(6);
            ByteBuffer[] buffers = new ByteBuffer[]{byteBuffer1, byteBuffer2};
            channel.position(12);
            long read1 = channel.read(buffers, 0, 2);
            log.info("read:{}", read1);
            byte[] array1 = byteBuffer1.array();
            byte[] array2 = byteBuffer2.array();

            log.info("result_1:{}", new String(array1, Charset.forName("utf-8")));
            log.info("result_2:{}", new String(array2, Charset.forName("utf-8")));

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * 验证long read(ByteBuffer[] dsts, int offset, int length)方法将字节放入ByteBuffer当前位置
     */
    @Test
    public void test3() {
        try (
            FileInputStream fis = new FileInputStream("/Users/zhukaishengy/StudyWorkSpace/nio-socket/src/main/java/com/autoai/chapter02/file/a.txt");
            FileChannel channel = fis.getChannel()
        ){
            ByteBuffer byteBuffer1 = ByteBuffer.allocate(4);
            ByteBuffer byteBuffer2 = ByteBuffer.allocate(7);
            ByteBuffer[] buffers = new ByteBuffer[]{byteBuffer1, byteBuffer2};
            byteBuffer1.position(1);
            byteBuffer2.position(1);
            channel.position(12);
            long read1 = channel.read(buffers, 0, 2);
            log.info("read:{}", read1);
            byte[] array1 = byteBuffer1.array();
            byte[] array2 = byteBuffer2.array();

            log.info("result_1:{}", new String(array1, Charset.forName("utf-8")));
            log.info("result_2:{}", new String(array2, Charset.forName("utf-8")));

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Data
    class PartBatchReadCallable implements Callable<String> {

        private FileChannel fileChannel;
        private ByteBuffer[] byteBuffers;

        public PartBatchReadCallable(FileChannel fileChannel, ByteBuffer... byteBuffers) {
            this.fileChannel = fileChannel;
            this.byteBuffers = byteBuffers;
        }

        @Override
        public String call() throws Exception {

            fileChannel.read(byteBuffers, 0, 2);
            for (ByteBuffer byteBuffer : byteBuffers) {
                byteBuffer.clear();
                byte[] array = byteBuffer.array();
                log.info("result:{}", new String(array, Charset.forName("utf-8")));
            }
            return null;
        }
    }

    /**
     * 验证long read(ByteBuffer[] dsts, int offset, int length)方法具有同步特性
     */
    @Test
    public void test4() {
        try (
            FileInputStream fis = new FileInputStream("/Users/zhukaishengy/StudyWorkSpace/nio-socket/src/main/java/com/autoai/chapter02/file/h.txt");
            FileChannel channel = fis.getChannel()
        ){
            // 创建线程池
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<>(100), new ThreadFactory() {
                private AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Thread th = new Thread(r);
                    th.setName("zks-" + count.getAndIncrement());
                    return th;
                }
            }, (r, executor) -> log.error("queue is full"));

            List<Callable<String>> callables = new ArrayList<>();

            for (int i = 0; i < 20; i++) {
                ByteBuffer byteBuffer1 = ByteBuffer.allocate(3);
                ByteBuffer byteBuffer2 = ByteBuffer.allocate(3);
                callables.add(new PartBatchReadCallable(channel, byteBuffer1, byteBuffer2));
            }
            threadPoolExecutor.invokeAll(callables);
        } catch (Exception e) {
            log.error(e.toString());
        }
    }
}
