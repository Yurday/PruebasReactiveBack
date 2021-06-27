package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;

@SpringBootTest

public class ServicioTest {
    @Autowired
    Servicio servicio;

    @Test
    void testMono(){
        Mono<String> uno = servicio.buscarUno();
        StepVerifier.create(uno).expectNext("Pedro").verifyComplete();
    }
    @Test
    void testVarios(){
        Flux<String> uno = servicio.buscarTodos();
        StepVerifier.create(uno).expectNext("Pedro").expectNext("María").expectNext("Jesús").expectNext("Carmen").verifyComplete();
    }

    @Test
    void testVariosLento(){
        Flux<String> uno = servicio.buscarTodosLento();
        StepVerifier.create(uno)
                .expectNext("Pedro")
                .thenAwait(Duration.ofSeconds(1))
                .expectNext("María")
                .thenAwait(Duration.ofSeconds(1))
                .expectNext("Jesús")
                .thenAwait(Duration.ofSeconds(1))
                .expectNext("Carmen")
                .thenAwait(Duration.ofSeconds(1)).verifyComplete();
    }
    
    @Test

    void testBuscarTodosFiltro() {
        Flux<String> source = servicio.buscarTodosFiltro();
        StepVerifier
                .create(source)
                .expectNext("JOHN")
                .expectNextMatches(name -> name.startsWith("MA"))
                .expectNext("CLOE", "CATE")
                .expectComplete()
                .verify();
    }

    @Test
    void testBuscarTodosFiltroError(){
        Flux<String> source = servicio.buscarTodosFiltro();
        StepVerifier
                .create(source)
                .expectNextCount(4)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Mensaje de error")
                ).verify();
    }


    @Test

    void testPublisher(){
        Flux <Integer> publisher = servicio.publisher();
        StepVerifier.create(publisher)
                .expectNext(2)
                .expectComplete()
                .verifyThenAssertThat()
                .hasDropped(4)
                .tookLessThan(Duration.ofMillis(1050));
    }

    final TestPublisher <String> testPublisher = TestPublisher.create();
    @Test
    void testUpperCaseConverter(){
        Servicio.UppercaseConverter uppercaseConverter = new Servicio.UppercaseConverter(testPublisher.flux());
        StepVerifier.create(uppercaseConverter.getUpperCase())
                .then(() -> testPublisher.emit("datos", "GeNeRaDoS", "Sofka"))
                .expectNext("DATOS", "GENERADOS", "SOFKA")
                .verifyComplete();
    }
}