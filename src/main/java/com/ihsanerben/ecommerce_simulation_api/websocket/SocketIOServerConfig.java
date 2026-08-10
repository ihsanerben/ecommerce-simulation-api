package com.ihsanerben.ecommerce_simulation_api.websocket;

import com.corundumstudio.socketio.SocketIOServer;
import com.ihsanerben.ecommerce_simulation_api.service.ApplicationConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.ihsanerben.ecommerce_simulation_api.config.ApplicationConfigKeys.SECURITY_ALLOWED_ORIGIN;

@Configuration
@RequiredArgsConstructor
public class SocketIOServerConfig {

    private final ApplicationConfigService applicationConfigService;

    @Value("${app.socket-io.port:9094}")
    private int port;

    @Bean(destroyMethod = "stop")
    SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration =
                new com.corundumstudio.socketio.Configuration();
        configuration.setHostname("0.0.0.0");
        configuration.setPort(port);
        configuration.setOrigin(applicationConfigService.getValue(SECURITY_ALLOWED_ORIGIN));
        return new SocketIOServer(configuration);
    }

    @Bean
    @Profile("!test")
    ApplicationRunner socketIOServerRunner(SocketIOServer socketIOServer) {
        return arguments -> socketIOServer.start();
    }
}
