package com.dwarfeng.tmpstg.example;

import com.dwarfeng.tmpstg.handler.TemporaryStorageHandler;
import com.dwarfeng.tmpstg.util.ContentUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;

/**
 * 多线程操作示例。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ConcurrentOperationExample {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:spring/application-context*.xml"
        );
        ctx.registerShutdownHook();
        ctx.start();

        TemporaryStorageHandler temporaryStorageHandler = ctx.getBean(TemporaryStorageHandler.class);

        ThreadPoolTaskExecutor executor = ctx.getBean(ThreadPoolTaskExecutor.class);

        Scanner scanner = new Scanner(System.in);

        // 显示欢迎信息。
        System.out.println("开发者您好!");
        System.out.println("这是一个示例, 用于演示 dwarfeng-tmpstg 的功能");
        System.out.println("该示例将会会生成多个任务同步执行, 每个任务写入一个随机内容后, 等待一段时间, 随后读取内容, " +
                "并对比两者的 MD5 值");
        System.out.println("您需要指定生成任务的数量, 单个任务生成的随机内容的长度范围, 单个任务的等待时间的长度范围");
        System.out.println("单个任务生成的随机内容的长度范围格式为: min-max, 单位为字节");
        System.out.println("单个任务的等待时间的长度范围格式为: min-max, 单位为毫秒");

        // 获取参数。
        System.out.println("请指定任务的数量, 默认值为 500: ");
        int taskCount;
        String taskCountString = scanner.nextLine();
        try {
            taskCount = Integer.parseInt(taskCountString);
            if (taskCount <= 0) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("输入的任务的数量不合法, 使用默认值 500");
            taskCount = 500;
        }
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
        System.out.println("请指定单个任务的等待时间的长度, 默认值为 1000-5000");
        long taskSleepDurationMin;
        long taskSleepDurationMax;
        String taskSleepDurationString = scanner.nextLine();
        try {
            taskSleepDurationString = StringUtils.trim(taskSleepDurationString);
            String[] split = StringUtils.split(taskSleepDurationString, "-");
            taskSleepDurationMin = Long.parseLong(split[0]);
            taskSleepDurationMax = Long.parseLong(split[1]);
            if (taskSleepDurationMin <= 0 || taskSleepDurationMax <= 0 || taskSleepDurationMin > taskSleepDurationMax) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("输入的单个任务的等待时间的长度不合法, 使用默认值 1000-5000");
            taskSleepDurationMin = 1000;
            taskSleepDurationMax = 5000;
        }

        // 1. 生成任务，并同步执行。
        System.out.println();
        System.out.println("1. 生成任务, 并同步执行...");
        List<Future<?>> futureList = new ArrayList<>(taskCount);
        for (int i = 0; i < taskCount; i++) {
            Task task = new Task(
                    temporaryStorageHandler, i,
                    contentLengthMin, contentLengthMax, taskSleepDurationMin, taskSleepDurationMax
            );
            Future<?> future = executor.submit(task);
            futureList.add(future);
        }
        for (Future<?> future : futureList) {
            future.get();
        }
        System.out.println("所有任务执行完毕!");
        System.out.println("示例演示完毕, 感谢您测试与使用!");

        ctx.stop();
        ctx.close();
        System.exit(0);
    }

    private static class Task implements Runnable {

        private final TemporaryStorageHandler temporaryStorageHandler;

        private final int index;

        private final int contentLengthMin;
        private final int contentLengthMax;
        private final long taskSleepDurationMin;
        private final long taskSleepDurationMax;

        public Task(
                TemporaryStorageHandler temporaryStorageHandler, int index,
                int contentLengthMin, int contentLengthMax, long taskSleepDurationMin, long taskSleepDurationMax
        ) {
            this.temporaryStorageHandler = temporaryStorageHandler;
            this.index = index;
            this.contentLengthMin = contentLengthMin;
            this.contentLengthMax = contentLengthMax;
            this.taskSleepDurationMin = taskSleepDurationMin;
            this.taskSleepDurationMax = taskSleepDurationMax;
        }

        @SuppressWarnings("CallToPrintStackTrace")
        @Override
        public void run() {
            try {
                int contentLength = RandomUtils.nextInt(contentLengthMin, contentLengthMax);
                byte[] content = ContentUtil.randomContent(contentLength);
                String contentMd5 = ContentUtil.md5Checksum(content);
                System.out.printf("任务 %d: 生成的内容的 MD5 值为 %s%n", index, contentMd5);
                String key = temporaryStorageHandler.create();
                try (OutputStream out = temporaryStorageHandler.openOutputStream(key, contentLength)) {
                    out.write(content);
                }
                long taskSleepDuration = RandomUtils.nextLong(taskSleepDurationMin, taskSleepDurationMax);
                System.out.printf("任务 %d: 休眠 %d 毫秒...%n", index, taskSleepDuration);
                byte[] readContent = new byte[contentLength];
                try (InputStream in = temporaryStorageHandler.openInputStream(key)) {
                    int ignored = in.read(readContent);
                }
                String readContentMd5 = ContentUtil.md5Checksum(readContent);
                if (contentMd5.equals(readContentMd5)) {
                    System.out.printf("任务 %d: 读取内容并比对 MD5值, 相等!%n", index);
                } else {
                    System.out.printf("任务 %d: 读取内容并比对 MD5值, 不相等!%n", index);
                }
                System.out.printf("任务 %d: 清理临时存储...%n", index);
                temporaryStorageHandler.disposeAndRemove(key);
            } catch (Exception e) {
                System.err.printf("任务 %d: 执行时发生异常, 异常信息如下: ", index);
                e.printStackTrace();
            }
        }
    }
}
