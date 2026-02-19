package com.openclassrooms.mddapi;

import org.junit.jupiter.api.Test;

class MddApiApplicationMainTest {

    @Test
    void run_starts_and_closes_context() {
        String key = "mdd.exitAfterStartup";
        String previous = System.getProperty(key);
        System.setProperty(key, "true");
        try {
            MddApiApplication.main(new String[] { "--server.port=0" });
        } finally {
            if (previous == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        }
    }
}
