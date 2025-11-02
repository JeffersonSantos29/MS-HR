# Projeto de Microsserviços de RH (MS-HR)

Este repositório contém um projeto acadêmico focado na implementação de uma arquitetura de microsserviços (MSA) utilizando o ecossistema Spring (Spring Boot e Spring Cloud).

O objetivo é simular um sistema simples de Recursos Humanos, onde diferentes responsabilidades são separadas em serviços independentes que se comunicam para realizar as operações de consulta de funcionários e cálculo de folha de pagamento.

## Arquitetura e Módulos do Projeto

O sistema é composto pelos seguintes microsserviços:

- **`hr-eureka-server`**:
    - **Função**: Service Discovery (Registro de Serviços).
    - Atua como o "catálogo telefônico" da arquitetura. Todos os outros serviços se registram nele, permitindo que se encontrem dinamicamente pelo nome da aplicação, sem precisar saber o IP ou a porta exata.
- **`hr-config-server`**:
    - **Função**: Configuration Server (Servidor de Configuração).
    - Centraliza as configurações de todos os outros microsserviços. Em vez de cada serviço ter seu próprio `application.properties`, eles buscam suas configurações neste servidor, que por sua vez as lê de um repositório Git.
- **`hr-api-gateway`**:
    - **Função**: API Gateway (Roteador).
    - É a porta de entrada única para o sistema. O *front-end* ou qualquer cliente externo só precisa conhecer este endereço. O Gateway é responsável por rotear as requisições para os serviços corretos (`hr-worker`, `hr-payroll`, etc.) e atua como um balanceador de carga.
- **`hr-worker`**:
    - **Função**: Serviço de Domínio (Funcionários).
    - Responsável por manter os dados cadastrais dos trabalhadores (nome, valor por dia, etc.). Possui seu próprio banco de dados (H2, neste caso).
- **`hr-payroll`**:
    - **Função**: Serviço de Domínio (Folha de Pagamento).
    - Responsável por calcular o pagamento de um funcionário. Este serviço **não** tem banco de dados próprio; ele consome os dados do `hr-worker` para realizar o cálculo.
- **`hr-user`**:
    - **Função**: Serviço de Domínio (Usuários).
    - Gerencia os usuários (com login e senha) que podem acessar o sistema.
- **`hr-oauth`**:
    - **Função**: Servidor de Autorização (OAuth2).
    - Protege a API, validando as credenciais dos usuários (fornecidas pelo `hr-user`) e emitindo tokens de acesso (JWT) que devem ser usados para consumir os outros serviços.

## Tecnologias Utilizadas

Este projeto demonstra um conjunto robusto de tecnologias do ecossistema Spring:

- **Java 11+**
- **Spring Boot**: Framework principal para criação dos serviços.
- **Spring Cloud**:
    - **Eureka**: Para Service Discovery.
    - **Spring Cloud Gateway**: Para roteamento e API Gateway.
    - **Spring Cloud Config**: Para configuração centralizada.
    - **OpenFeign**: Para comunicação síncrona declarativa entre serviços (ex: `hr-payroll` chamando `hr-worker`).
    - **Ribbon/Spring Cloud LoadBalancer**: Para balanceamento de carga no lado do cliente.
- **Spring Data JPA**: Para persistência de dados no `hr-worker` e `hr-user`.
- **H2 Database**: Banco de dados em memória para fins de desenvolvimento.
- **Spring Security & OAuth2**: Para autenticação e autorização.
- **Maven**: Gerenciador de dependências e build.

---

## Exemplos de Código e Padrões

Abaixo estão alguns trechos de código que ilustram os principais padrões de microsserviços implementados no projeto.

### 1. Service Discovery (Eureka)

Para transformar uma aplicação Spring Boot em um servidor Eureka, basta uma anotação na classe principal.

**Arquivo: `hr-eureka-server/.../HrEurekaServerApplication.java`**

Java

`@EnableEurekaServer
@SpringBootApplication
public class HrEurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrEurekaServerApplication.class, args);
	}
}`

### 2. API Gateway (Spring Cloud Gateway)

O Gateway roteia requisições com base em padrões de URL (predicates). Note o uso do `lb://` (Load Balancer) para que o Gateway encontre os serviços através do Eureka, sem *hardcode* de endereços.

**Arquivo: `hr-api-gateway-spring-cloud/src/main/resources/application.yaml`**

YAML

`spring:
  application:
    name: hr-api-gateway
  cloud:
    gateway:
      routes:
        - id: hr-worker
          uri: lb://hr-worker
          predicates:
            - Path=/hr-worker/**

        - id: hr-payroll
          uri: lb://hr-payroll
          predicates:
            - Path=/hr-payroll/**

        # ... (rotas para user e oauth)`

### 3. Comunicação entre Serviços (Feign Client)

O `hr-payroll` precisa dos dados do `hr-worker`. Em vez de usar um `RestTemplate`, usamos o Feign para criar um cliente declarativo. O Feign se integra automaticamente ao Eureka e ao Load Balancer.

**Arquivo: `hr-payroll/src/main/java/com/mshr/hrpayroll/feignclients/WorkerFeignClient.java`**

Java

`@Component
@FeignClient(name = "hr-worker", path = "/workers")
public interface WorkerFeignClient {

	@GetMapping(value = "/{id}")
	ResponseEntity<Worker> findById(@PathVariable Long id);
}`

- `name = "hr-worker"`: Este é o nome que o serviço usa para se registrar no Eureka.
- `path = "/workers"`: O path base dos endpoints no serviço `hr-worker`.

### 4. Configuração de Cliente (Service)

Qualquer serviço que **não** seja o Eureka Server ou o Config Server atua como um "cliente". Ele precisa se registrar no Eureka e, muitas vezes, é configurado para rodar em uma porta aleatória (`server.port=0`), permitindo que várias instâncias do mesmo serviço sejam executadas simultaneamente.

**Arquivo: `hr-payroll/src/main/resources/application.properties`**

Properties

`# Define uma porta aleatória para permitir múltiplas instâncias
server.port=0

# Nome que este serviço usará para se registrar no Eureka
spring.application.name=hr-payroll

# Endereço do Servidor Eureka
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/`
