package uhk.fim.smap.corona_app_server.service;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import java.util.concurrent.TimeUnit;

@Service
public class CustomWebClientService {

    private final WebClient webClient;

    public CustomWebClientService(WebClient.Builder webClientBuilder) {
        // snížení timeoutu
        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                });
        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 100000)).build();
        // init instance WebClienta
        this.webClient = webClientBuilder
                .baseUrl("https://onemocneni-aktualne.mzcr.cz/api/v2/covid-19")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .exchangeStrategies(strategies)
                .build();
    }

    // GET metoda pro získání aktuálních dat (zjednodušené, nevyhodnocuje se response code, ukládá se do paměti atd.)
    public DataBuffer getData() {
        return this.webClient
                .get()
                .uri("/kraj-okres-nakazeni-vyleceni-umrti.csv")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.parseMediaType("text/csv;charset=utf-8").getType())
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(DataBuffer.class)
                .block();
    }
}
