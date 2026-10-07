package com.edstem.sample.support;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public final class Concurrently {

  private Concurrently() {}

  public static <T> List<Outcome<T>> run(int callers, IntFunction<Callable<T>> task)
      throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(callers);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Outcome<T>>> futures =
          IntStream.range(0, callers)
              .mapToObj(index -> executor.submit(() -> awaitThenCall(start, task.apply(index))))
              .toList();
      start.countDown();
      return futures.stream().map(Concurrently::join).toList();
    } finally {
      executor.shutdownNow();
    }
  }

  private static <T> Outcome<T> awaitThenCall(CountDownLatch start, Callable<T> call)
      throws InterruptedException {
    start.await();
    try {
      return Outcome.success(call.call());
    } catch (Exception e) {
      return Outcome.failure(e);
    }
  }

  private static <T> Outcome<T> join(Future<Outcome<T>> future) {
    try {
      return future.get();
    } catch (Exception e) {
      return Outcome.failure(e);
    }
  }

  public record Outcome<T>(T value, Exception error) {

    static <T> Outcome<T> success(T value) {
      return new Outcome<>(value, null);
    }

    static <T> Outcome<T> failure(Exception error) {
      return new Outcome<>(null, error);
    }

    public boolean succeeded() {
      return error == null;
    }
  }
}
