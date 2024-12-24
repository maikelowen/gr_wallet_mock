package com.goldenrace.wallet.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.goldenrace.wallet.server")
public class WalletApiServerApplication {

    public static void main(String[] args) {
        System.setProperty("user.timezone", "GMT");
        SpringApplication.run(WalletApiServerApplication.class, args);
    }
}
