package com.montanha.idolada;

import com.montanha.config.Configuracoes;
import com.montanha.factory.UsuarioDataFactory;
import com.montanha.factory.ViagemDataFactory;
import com.montanha.pojo.Usuario;
import com.montanha.pojo.Viagem;
import io.restassured.http.ContentType;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;

public class ViagensTest {

    private String token;
    private String tokenUsuario;

    @Before
    public void setUp(){
        //Configurações Rest-Assured
        Configuracoes configuracoes = ConfigFactory.create(Configuracoes.class);

        baseURI = configuracoes.baseURI();
        port = configuracoes.port();
        basePath = configuracoes.basePath();

        Usuario usuarioAdministrador = UsuarioDataFactory.criarUsuarioAdministrador();

        this.token = given()
            .contentType(ContentType.JSON)
            .body(usuarioAdministrador)
        .when()
            .post("/v1/auth")
        .then()
                //.log()
                //.all()
            .extract()
                .path("data.token");

        Usuario usuarioComum = UsuarioDataFactory.criarUsuarioComum();
        this.tokenUsuario = given()
                .contentType(ContentType.JSON)
                .body(usuarioComum)
                .when()
                .post("/v1/auth")
                .then()
                //.log()
                //.all()
                .extract()
                .path("data.token");
    }

    @Test
    public void testCadastroDeViagemValidaRetornaSucesso() throws IOException {
        Viagem viagemValida = ViagemDataFactory.criarViagemValida();

        given()
                .contentType(ContentType.JSON)
                .body(viagemValida)
                .header("Authorization", token)
        .when()
                .post("/v1/viagens")
        .then()
                .assertThat()
                    .statusCode(201)
                    .body("data.localDeDestino",equalTo("Pará"))
                    .body("data.acompanhante", equalToIgnoringCase("nelson"));
    }

    @Test
    public void testCadastroDeViagemSemLocalDeDestino() throws IOException {
        Viagem viagemSemLocalDeDestino = ViagemDataFactory.criarViagemSemLocalDeDestino();

        given()
                .contentType(ContentType.JSON)
                .body(viagemSemLocalDeDestino)
                .header("Authorization", token)
        .when()
                .post("/v1/viagens")
        .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    public void testCadastroDeViagemValidaContrato() throws IOException {
        Viagem viagemValida = ViagemDataFactory.criarViagemValida();

        given()
            .contentType(ContentType.JSON)
            .body(viagemValida)
            .header("Authorization", token)
        .when()
            .post("/v1/viagens")
        .then()
            .assertThat()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/postV1ViagensViagemValida.json"));
    }

    @Test
    public void testRetornaUmaViagemPossuiStatusCode200EMostraLocalDeDestino(){
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", tokenUsuario)
        .when()
            .get("/v1/viagens/1")
        .then()
            .assertThat()
                .statusCode(200)
                .body("data.localDeDestino",equalTo("Osasco"));
    }

    @Test
    public void testViagemProcessaCorretamenteORetornoDaAPIDoTempo(){
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", tokenUsuario)
                .when()
                .get("/v1/viagens/1")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.temperatura",equalTo(35.5f));
    }

}
