package com.lujiatao.mock.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 多线程工具
 *
 * @param <T> 执行可执行对象后的返回值类型
 * @author 卢家涛
 */
public class MultiThreadUtil<T> {

    private Callable<T> callable;

    private int executeCount = 0;

    private Collection<? extends Callable<T>> callables = new ArrayList<>();

    /**
     * 执行无参数的可执行对象
     *
     * @param callable     待执行的可执行对象
     * @param executeCount 可执行对象的执行次数
     */
    public MultiThreadUtil(Callable<T> callable, int executeCount) {
        this.callable = callable;
        this.executeCount = executeCount;
    }

    /**
     * 执行有参数的可执行对象
     *
     * @param callables 待执行的可执行对象集合
     */
    public MultiThreadUtil(Collection<? extends Callable<T>> callables) {
        this.callables = callables;
    }

    /**
     * 多线程执行
     *
     * @param miniPoolSize 最小线程数
     * @return 执行可执行对象后的返回值列表
     */
    public List<T> execute(int miniPoolSize) {
        try {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(miniPoolSize, miniPoolSize * 10, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(miniPoolSize * 100));
            List<Future<T>> futures = new ArrayList<>();
            // 执行无参数的可执行对象
            if (this.callables.isEmpty()) {
                for (int i = 0; i < executeCount; i++) {
                    Future<T> future = threadPoolExecutor.submit(callable);
                    futures.add(future);
                }
            }
            // 执行有参数的可执行对象
            else {
                futures = threadPoolExecutor.invokeAll(this.callables);
            }
            threadPoolExecutor.shutdown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get());
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
