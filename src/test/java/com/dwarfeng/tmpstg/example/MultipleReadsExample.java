package com.dwarfeng.tmpstg.example;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.util.ContentUtil;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

/**
 * 多次读取示例。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class MultipleReadsExample {

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
        System.out.println("该示例将会会生成一个随机内容, 您需要指定这个内容的长度");

        // 获取随机内容的长度。
        System.out.print("请指定生成内容的长度, 单位为字节, 默认值为 1024: ");
        int contentLength = scanner.nextInt();
        if (contentLength <= 0) {
            System.out.println("输入的长度不合法, 使用默认长度 1024");
            contentLength = 1024;
        }

        // 1. 生成随机内容，并计算其 MD5 值。
        System.out.println();
        System.out.println("1. 生成随机内容，并计算其 MD5 值...");
        byte[] content = ContentUtil.randomContent(contentLength);
        String contentMd5 = ContentUtil.md5Checksum(content);
        System.out.printf("生成的内容的 MD5 值为: %s%n", contentMd5);
        System.out.print("按回车键继续...");
        scanner.nextLine();

        // 2. 创建临时存储，并存储内容。
        System.out.println();
        System.out.println("2. 创建临时存储, 并存储内容...");
        String key = temporaryStorageHandler.create();
        try (OutputStream out = temporaryStorageHandler.openOutputStream(key, contentLength)) {
            out.write(content);
        }
        System.out.printf("内容已经存储在临时存储中, 存储的键为: %s%n", key);
        System.out.print("按回车键继续...");
        scanner.nextLine();

        // 3. 多次读取内容，计算并对比其 MD5 值。
        System.out.println();
        System.out.println("3. 多次读取内容, 计算并对比其 MD5 值...");
        for (int i = 0; i < 10; i++) {
            byte[] readContent = new byte[contentLength];
            try (InputStream in = temporaryStorageHandler.openInputStream(key)) {
                int ignored = in.read(readContent);
            }
            String readContentMd5 = ContentUtil.md5Checksum(readContent);
            System.out.printf("第 %d 次读取的内容的 MD5 值为: %s%n", i + 1, readContentMd5);
            if (contentMd5.equals(readContentMd5)) {
                System.out.println("两个 MD5 值相等!");
            } else {
                System.err.println("两个 MD5 值不相等!");
            }
        }
        System.out.print("按回车键继续...");
        scanner.nextLine();

        // 4. 清理临时存储。
        System.out.println();
        System.out.println("4. 清理临时存储...");
        temporaryStorageHandler.disposeAndRemove(key);
        System.out.println("示例演示完毕, 感谢您测试与使用!");

        ctx.stop();
        ctx.close();
        System.exit(0);
    }
}
