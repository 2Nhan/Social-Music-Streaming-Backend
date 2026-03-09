package com.tunhan.micsu.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync // Bật tính năng chạy ngầm cho toàn bộ project
public class AsyncConfig {

    @Bean(name = "hlsTaskExecutor")
    public Executor hlsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. Số luồng chạy thường trực (Nên set bằng số nhân CPU của Server)
        // Ví dụ server có 4 cores -> set là 2 hoặc 4
        executor.setCorePoolSize(2);

        // 2. Số luồng TỐI ĐA được phép chạy cùng lúc nếu hàng đợi đầy
        // Xử lý FFmpeg rất nặng CPU, không nên set quá cao (chỉ nên từ 4 - 8)
        executor.setMaxPoolSize(4);

        // 3. Sức chứa của hàng đợi (Queue)
        // Nếu 4 luồng trên đều đang bận chạy FFmpeg, người thứ 5 upload sẽ được đưa vào hàng đợi này.
        // Hàng đợi chứa được tối đa 50 bài hát đang chờ.
        executor.setQueueCapacity(50);

        // 4. Đặt tên tiền tố cho luồng để dễ dò lỗi trong file log
        executor.setThreadNamePrefix("HlsEncoder-");

        // 5. Chính sách xử lý khi hệ thống quá tải (Quá 4 luồng bận + Hàng đợi 50 cũng đầy)
        // CallerRunsPolicy: Bắt luồng chính (luồng HTTP) tự đi mà chạy, giúp giảm tốc độ nhận request mới.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 6. Cho phép luồng tự động tắt nếu để không quá lâu (tiết kiệm RAM)
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }
}