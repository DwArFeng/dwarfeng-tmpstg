package com.dwarfeng.tmpstg.example;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.util.ContentUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * 多次写入示例。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class MultipleWritesExample {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:spring/application-context*.xml"
        );
        ctx.registerShutdownHook();
        ctx.start();

        TemporaryStorageHandler temporaryStorageHandler = ctx.getBean(TemporaryStorageHandler.class);

        Scanner scanner = new Scanner(System.in);

        // 显示欢迎信息。
        System.out.println("开发者您好!");
        System.out.println("这是一个示例, 用于演示 dwarfeng-tmpstg 的功能");
        System.out.println("该示例将会会生成一个随机内容, 您需要指定这个内容的长度, 格式为: min-max, 单位为字节");

        // 获取随机内容的长度。
        System.out.println("请指定单个任务生成的随机内容的长度, 默认值为 1024-4096");
        int contentLengthMin;
        int contentLengthMax;
        String contentLengthString = scanner.nextLine();
        try {
            contentLengthString = StringUtils.trim(contentLengthString);
            String[] split = StringUtils.split(contentLengthString, "-");
            contentLengthMin = Integer.parseInt(split[0]);
            contentLengthMax = Integer.parseInt(split[1]);
            if (contentLengthMin <= 0 || contentLengthMax <= 0 || contentLengthMin > contentLengthMax) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("输入的单个任务生成的随机内容的长度不合法, 使用默认值 1024-4096");
            contentLengthMin = 1024;
            contentLengthMax = 4096;
        }

        // 1. 多次写入内容，写入后读取并对比其 MD5 值。
        System.out.println();
        System.out.println("1. 多次写入内容, 写入后读取并对比其 MD5 值...");
        String key = temporaryStorageHandler.create();
        for (int i = 0; i < 10; i++) {
            int contentLength = RandomUtils.nextInt(contentLengthMin, contentLengthMax);
            byte[] content = ContentUtil.randomContent(contentLength);
            String contentMd5 = ContentUtil.md5Checksum(content);
            System.out.printf("第 %d 次生成的内容的 MD5 值为: %s%n", i + 1, contentMd5);

            try (OutputStream out = temporaryStorageHandler.openOutputStream(key, contentLength)) {
                out.write(content);
            }

            byte[] readContent = new byte[contentLength];
            try (InputStream in = temporaryStorageHandler.openInputStream(key)) {
                int ignored = in.read(readContent);
            }
            String readContentMd5 = ContentUtil.md5Checksum(readContent);
            if (contentMd5.equals(readContentMd5)) {
                System.out.printf("第 %d 次读取的内容的 MD5 值为 %s, 两个 MD5 值相等!%n", i + 1, readContentMd5);
            } else {
                System.err.printf("第 %d 次读取的内容的 MD5 值为 %s, 两个 MD5 值不相等!%n", i + 1, readContentMd5);
            }
        }
        System.out.print("请按回车键继续...");
        scanner.nextLine();

        // 2. 清理临时存储。
        System.out.println();
        System.out.println("2. 清理临时存储...");
        temporaryStorageHandler.disposeAndRemove(key);
        System.out.println("示例演示完毕, 感谢您测试与使用!");

        ctx.stop();
        ctx.close();
        System.exit(0);
    }
}
