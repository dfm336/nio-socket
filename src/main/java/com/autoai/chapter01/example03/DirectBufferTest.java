package com.autoai.chapter01.example03;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * @Author: zhukaishengy
 * @Date: 2020/4/28 14:37
 * @Description:
 */
@Slf4j
public class DirectBufferTest {

    public static void main(String[] args) {

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10);
        log.info("direct:{}", byteBuffer.isDirect());
    }
}
