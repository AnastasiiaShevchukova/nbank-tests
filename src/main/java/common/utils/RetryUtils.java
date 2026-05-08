package common.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Принимаем на вход общего ретрая:
 * 1) что повторяем
 * 2) условие выхода
 * 3) максимальное количество попыток
 * 4) задержка между каждой попыткой
 */
public class RetryUtils {

    public static <T> T retry (
            Supplier<T> action,
            Predicate<T> condition,
            int maxAttempts,
            long delayMills) {
        Throwable lastError = null;

        for (int attempts = 1; attempts <= maxAttempts; attempts++) {

            try {
                T result = action.get();

                if (condition.test(result)) {
                    return result;
                }

            } catch (Throwable e) {
                lastError = e;
            }

            try {
                Thread.sleep(delayMills);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException(
                "Retry failed after " + maxAttempts + " attempts",
                lastError
        );
    }
}
