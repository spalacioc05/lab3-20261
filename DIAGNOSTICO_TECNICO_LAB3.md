# Diagnostico tecnico del proyecto lab3-20261

## 1. Veredicto ejecutivo

Estado general: NO CUMPLE la guia completa del laboratorio en su estado actual.

Si cumple parcialmente en estos puntos:
- Existe una aplicacion Spring Boot con paquete base consistente.
- Hay una entidad `Flight`, un repositorio, un servicio y un controlador REST manual.
- Hay dependencias para JPA, Web, Validation, HATEOAS, Actuator y Swagger/OpenAPI.
- El proyecto empaqueta correctamente un JAR ejecutable llamado `vuelokbt-0.0.1-SNAPSHOT.jar`.

No cumple o cumple de forma incompleta en estos puntos criticos:
- La aplicacion no paso la validacion ejecutable `./mvnw.cmd test` porque falla la conexion a MySQL local con `Access denied for user 'root'@'localhost'`.
- No hay implementacion real de HATEOAS/HAL en el codigo propio.
- No hay Dockerfile.
- No hay manifiestos Kubernetes o Minikube.
- No hay manifiestos GitOps/ArgoCD.
- No hay pruebas funcionales automatizadas ni coleccion Postman en el repositorio.
- El manejo de errores HTTP es incompleto e inconsistente.
- Hay riesgos de seguridad por credenciales quemadas y exposicion total de Actuator.

## 2. Evidencia verificada

### 2.1 Resultado de ejecucion real
Se ejecuto:

```powershell
.\mvnw.cmd test
```

Resultado real:
- La compilacion Maven avanza.
- El contexto Spring falla al inicializar JPA.
- La causa raiz observada es: `java.sql.SQLException: Access denied for user 'root'@'localhost' (using password: YES)`.

Tambien se ejecuto:

```powershell
.\mvnw.cmd -DskipTests package
```

Resultado real:
- `BUILD SUCCESS`
- Artefacto generado: `target/vuelokbt-0.0.1-SNAPSHOT.jar`

### 2.2 Archivos realmente presentes
Se verificaron estos artefactos del proyecto:
- `pom.xml`
- `README.md`
- `src/main/resources/application.properties`
- `src/main/java/com/udea/vuelokbt/VuelokbtApplication.java`
- `src/main/java/com/udea/vuelokbt/OpenApiConfig.java`
- `src/main/java/com/udea/vuelokbt/model/Flight.java`
- `src/main/java/com/udea/vuelokbt/dao/IFlightDAO.java`
- `src/main/java/com/udea/vuelokbt/service/FlightService.java`
- `src/main/java/com/udea/vuelokbt/controller/FlightController.java`
- `src/main/java/com/udea/vuelokbt/exception/ModelNotFoundException.java`
- `src/main/java/com/udea/vuelokbt/exception/FlightNotFoundException.java`
- `src/main/java/com/udea/vuelokbt/exception/InvalidRating.java`
- `src/test/java/com/udea/vuelokbt/VuelokbtApplicationTests.java`

No se encontraron estos artefactos exigidos por la guia:
- `Dockerfile`
- manifiestos `.yml` o `.yaml` de Kubernetes
- coleccion Postman exportada
- manifiestos de ArgoCD
- configuracion de Prometheus o Grafana

## 3. Diagnostico archivo por archivo

### 3.1 `pom.xml`
Hallazgos:
- Spring Boot `2.7.17`: correcto y estable para Java 11.
- Java `11`: compatible con Spring Boot 2.7.x.
- `javax.persistence` y `javax.validation` son coherentes con Spring Boot 2.7.x. No hay problema actual de compatibilidad Jakarta, pero si migras a Spring Boot 3 tendras que cambiar a `jakarta.*`.
- Dependencias presentes y utiles:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-data-rest`
  - `spring-boot-starter-hateoas`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-actuator`
  - `spring-data-rest-hal-explorer`
  - `springdoc-openapi-ui:1.7.0`
  - `mysql-connector-j`
  - `lombok`
- Lombok esta bien cableado en compilacion y annotation processing.

Problemas y observaciones:
- `spring-boot-starter-data-rest` esta incluido, pero el proyecto ya implementa un controlador manual. Esto puede exponer endpoints REST automáticos de repositorio ademas de los manuales, generando duplicidad funcional y confusion en Swagger/HAL.
- `spring-data-rest-hal-explorer` tampoco se aprovecha en el codigo propio porque no hay ensambles HATEOAS manuales ni definicion de `RepositoryRestResource`.
- `springdoc-openapi-ui:1.7.0` es compatible con Spring Boot 2.7.x; no es un error, aunque hoy no es la linea mas moderna.

Conclusiones sobre `pom.xml`:
- El archivo es valido y compatible.
- La mayor mejora aqui no es de version, sino de coherencia arquitectonica: elegir entre `Spring Data REST` automatico o controladores REST manuales. No conviene mezclar ambos sin una decision explicita.

### 3.2 `README.md`
Hallazgos:
- Solo contiene el titulo del repositorio.

Problemas:
- No documenta prerequisitos.
- No explica variables de entorno.
- No describe endpoints.
- No documenta como levantar MySQL, Docker, Kubernetes o Minikube.
- No documenta Swagger ni Postman.

Conclusiones:
- Para un laboratorio evaluable, el README esta incompleto.

### 3.3 `src/main/resources/application.properties`
Configuracion actual:
- `spring.application.name=vuelokbt`
- `server.port=8089`
- `spring.datasource.url=jdbc:mysql://localhost:3306/vuelokbt`
- `spring.datasource.username=root`
- `spring.datasource.password=root`
- `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver`
- `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect`
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.jpa.show-sql=true`
- `management.endpoints.web.exposure.include=*`
- `management.endpoint.health.show-details=always`

Problemas:
- Hay credenciales quemadas en codigo fuente.
- La conexion esta fija a `localhost`, por lo que no sirve tal cual para Kubernetes. Dentro del cluster deberia apuntar al nombre del Service MySQL, por ejemplo `mysql`.
- El dialecto `MySQL5Dialect` funciona, pero para MySQL 8 es preferible `org.hibernate.dialect.MySQL8Dialect` o incluso omitirlo si Hibernate lo detecta correctamente.
- `ddl-auto=update` sirve para desarrollo, pero en ambientes evaluables o de despliegue es riesgoso. Para laboratorio puede aceptarse en dev; en Kubernetes es mejor controlarlo via perfil.
- Exponer `management.endpoints.web.exposure.include=*` y `health.show-details=always` es riesgoso en ambientes reales.

Conclusiones:
- La configuracion local existe, pero no esta preparada para perfiles `local`, `docker`, `k8s`.
- El principal bloqueo actual es la autenticacion fallida hacia MySQL.

Recomendacion:
- Parametrizar con variables de entorno:

```properties
server.port=${SERVER_PORT:8089}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/vuelokbt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when_authorized
```

### 3.4 `src/main/java/com/udea/vuelokbt/VuelokbtApplication.java`
Hallazgos:
- La clase main esta correcta.
- El paquete base `com.udea.vuelokbt` es consistente con el resto del proyecto.

Observacion:
- No hay problema aqui.

### 3.5 `src/main/java/com/udea/vuelokbt/OpenApiConfig.java`
Hallazgos:
- Existe una configuracion minima de OpenAPI con titulo, version y descripcion.

Problemas o limites:
- No define `servers`, contacto, licencia, tags ni informacion de ambiente.
- La UI de Swagger no pudo verificarse en ejecucion porque la aplicacion no arranco por el fallo de base de datos.

Conclusiones:
- La configuracion existe y es valida.
- La verificacion funcional de `/swagger-ui/index.html` queda pendiente hasta corregir la conectividad a MySQL o desacoplar las pruebas del datasource real.

### 3.6 `src/main/java/com/udea/vuelokbt/model/Flight.java`
Campos encontrados:
- `idFlight : Long`
- `nombreAvion : String`
- `numeroVuelo : String`
- `origen : String`
- `destino : String`
- `capacidad : int`
- `rating : int`
- `planvuelo : long`
- `cumplido : Boolean`

Problemas tecnicos:
- Mezcla Lombok (`@Data`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor`) con getters y setters manuales. Es redundante y aumenta ruido.
- Usa `@NonNull` de Lombok en vez de validaciones Bean Validation para API REST. Eso no sustituye `@NotBlank`, `@NotNull`, `@Positive`, `@Min`, `@Max`.
- Solo `rating` tiene validacion explicita. Faltan validaciones para casi todos los demas campos.
- Los mensajes de `@Min` y `@Max` dicen `id should...`, pero se aplican a `rating`; el mensaje es incorrecto.
- `capacidad` y `planvuelo` tienen `length = 80`, pero `length` no aplica de forma util a tipos numericos.
- `planvuelo` como `long` es semantica pobre. Si representa fecha/hora o plan textual, deberia ser `LocalDateTime`, `String` o un objeto propio.
- `cumplido` es `Boolean` nullable. Eso permite `null` y vuelve ambigua la logica de negocio. Si el dominio solo admite si/no, conviene `boolean` o `@NotNull`.
- Importa `javax.persistence.Entity`, `GeneratedValue`, `Id` y ademas `javax.persistence.*`; esto es redundante.

Riesgo funcional:
- La entidad probablemente persiste, pero la API no esta modelando bien la validacion de entrada.

JSON de prueba coherente con el modelo actual:

```json
{
  "nombreAvion": "Airbus A320",
  "numeroVuelo": "AV123",
  "origen": "Medellin",
  "destino": "Bogota",
  "capacidad": 180,
  "rating": 5,
  "planvuelo": 202605210830,
  "cumplido": true
}
```

Modelo recomendado:
- `@NotBlank` en `nombreAvion`, `numeroVuelo`, `origen`, `destino`
- `@Positive` o `@Min(1)` en `capacidad`
- `@Min(1)` y `@Max(5)` en `rating`
- `@NotNull` en `cumplido` si se mantiene `Boolean`
- Evaluar reemplazar `planvuelo` por `LocalDateTime` o `String`

### 3.7 `src/main/java/com/udea/vuelokbt/dao/IFlightDAO.java`
Hallazgos:
- Extiende `CrudRepository<Flight, Long>`.
- Tiene consulta JPQL `from Flight f where f.rating>=4 AND f.cumplido=true`.

Problemas o mejoras:
- La query es valida, pero para una lista de “mejores vuelos” falta orden. Si no hay `ORDER BY`, el resultado no garantiza ranking.
- `CrudRepository` funciona, pero `JpaRepository` daria mejores utilidades para paginacion y ordenamiento.
- El nombre `viewBestFlight()` deberia estar en plural o describir busqueda, por ejemplo `findTopFlights()`.

Recomendacion:

```java
List<Flight> findByRatingGreaterThanEqualAndCumplidoTrueOrderByRatingDesc(Long minRating);
```

o bien:

```java
@Query("select f from Flight f where f.rating >= 4 and f.cumplido = true order by f.rating desc")
List<Flight> findTopFlights();
```

### 3.8 `src/main/java/com/udea/vuelokbt/service/FlightService.java`
Hallazgos:
- Implementa `save`, `delete`, `list`, `listId`, `update`, `viewBestFlight`.

Problemas criticos:
- `update()` hace `dao.findById(...).orElse(null)` y luego invoca setters sobre `existingFlight`. Si el ID no existe, produce `NullPointerException`.
- `delete()` llama `dao.deleteById(id)` sin verificar existencia. Si no existe, puede lanzar `EmptyResultDataAccessException` y terminar en 500.
- `listId()` retorna `Optional`, pero la responsabilidad de decidir error HTTP queda en el controlador. Esto es aceptable, aunque seria mas limpio lanzar excepcion de dominio desde servicio.
- No hay validacion de `idFlight` nulo en `update()`.
- `viewBestFlight()` lanza `FlightNotFoundException` cuando la lista esta vacia. Desde una optica REST, devolver lista vacia con 200 puede ser preferible a lanzar 404.

Conclusiones:
- La capa servicio existe, pero no protege bien los casos borde.

Implementacion recomendada:
- `update()` debe lanzar `ModelNotFoundException` o `FlightNotFoundException` si el vuelo no existe.
- `delete()` debe verificar existencia y retornar `204 No Content`.
- `viewBestFlight()` puede devolver lista vacia y dejar la semantica REST mas simple.

### 3.9 `src/main/java/com/udea/vuelokbt/controller/FlightController.java`
Endpoints actuales detectados:
- `POST /flight/save`
- `GET /flight/listAll`
- `GET /flight/list/{id}`
- `GET /flight/topFlights`
- `PUT /flight`
- `DELETE /flight/{id}`

Problemas tecnicos y REST:
- `save()` devuelve solo `Long`, y por defecto respondera 200, no 201.
- `save()` no usa `@Valid`.
- `save()` solo valida manualmente `rating > 5`; no cubre `rating < 1` ni campos vacios.
- `InvalidRating` no tiene `@ResponseStatus` ni `@ExceptionHandler`, por lo que puede terminar en 500.
- `listAllFlights()` devuelve `Iterable<Flight>` plano, sin HATEOAS.
- `listFlightById()` devuelve entidad plana, sin HATEOAS.
- `viewBestFlights()` devuelve `202 Accepted`; para una consulta GET completada correctamente deberia ser `200 OK`.
- `updateFlight()` no usa `@Valid` ni manejo robusto de errores.
- `deleteFlight()` devuelve `String`; para REST suele ser mejor `204 No Content`.
- `@CrossOrigin("*")` es demasiado abierto.
- Hay un import no usado: `ChangeSetPersister`.

Evaluacion HATEOAS:
- Actualmente NO implementa HATEOAS real.
- Tener la dependencia `spring-boot-starter-hateoas` no equivale a exponer HAL.
- No hay `EntityModel`, `CollectionModel`, `RepresentationModelAssembler` ni `WebMvcLinkBuilder`.

Como deberia agregarse HATEOAS:

```java
@Component
public class FlightModelAssembler implements RepresentationModelAssembler<Flight, EntityModel<Flight>> {
    @Override
    public EntityModel<Flight> toModel(Flight flight) {
        return EntityModel.of(
            flight,
            linkTo(methodOn(FlightController.class).listFlightById(flight.getIdFlight())).withSelfRel(),
            linkTo(methodOn(FlightController.class).listAllFlights()).withRel("flights"),
            linkTo(methodOn(FlightController.class).viewBestFlights()).withRel("topFlights")
        );
    }
}
```

Y en el controlador:

```java
@GetMapping("/list/{id}")
public EntityModel<Flight> listFlightById(@PathVariable Long id) {
    Flight flight = flightService.findByIdOrThrow(id);
    return assembler.toModel(flight);
}

@GetMapping("/listAll")
public CollectionModel<EntityModel<Flight>> listAllFlights() {
    List<EntityModel<Flight>> flights = StreamSupport
        .stream(flightService.list().spliterator(), false)
        .map(assembler::toModel)
        .toList();

    return CollectionModel.of(
        flights,
        linkTo(methodOn(FlightController.class).listAllFlights()).withSelfRel(),
        linkTo(methodOn(FlightController.class).viewBestFlights()).withRel("topFlights")
    );
}
```

Codigos HTTP recomendados:
- `POST /flight/save` -> `201 Created`
- `GET /flight/listAll` -> `200 OK`
- `GET /flight/list/{id}` -> `200 OK`
- `GET /flight/topFlights` -> `200 OK`
- `PUT /flight` -> `200 OK`
- `DELETE /flight/{id}` -> `204 No Content`
- rating invalido o body invalido -> `400 Bad Request`
- id inexistente -> `404 Not Found`

### 3.10 Excepciones
Archivos encontrados:
- `ModelNotFoundException` con `404`
- `FlightNotFoundException` con `404`
- `InvalidRating` sin anotacion HTTP

Problemas:
- `InvalidRating` no define `@ResponseStatus(HttpStatus.BAD_REQUEST)`.
- No existe `@ControllerAdvice` global para serializar errores de forma consistente.
- No existe estructura JSON de error con timestamp, status, error, message y path.
- Dos excepciones de “not found” para el mismo dominio pueden ser redundantes.

Recomendacion:
- Mantener una sola excepcion de dominio para vuelo no encontrado.
- Agregar un `GlobalExceptionHandler`.

Ejemplo recomendado:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRating.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRating(InvalidRating ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(Map.of(
            "status", 400,
            "error", "Bad Request",
            "message", ex.getMessage(),
            "path", request.getRequestURI()
        ));
    }
}
```

### 3.11 `src/test/java/com/udea/vuelokbt/VuelokbtApplicationTests.java`
Hallazgos:
- Solo existe `contextLoads()`.

Problemas:
- La prueba depende del datasource real configurado en `application.properties`.
- No hay perfil de test.
- No hay H2 para pruebas.
- No hay pruebas unitarias ni pruebas de controlador.

Conclusiones:
- El proyecto no tiene una estrategia minima de testing.

## 4. Coherencia general del proyecto

### 4.1 Paquetes y capas
La organizacion base existe y es entendible:
- `controller`
- `service`
- `dao`
- `model`
- `exception`
- configuracion en paquete base

Problemas:
- La capa `model` actua como entidad JPA, no hay DTOs de entrada/salida.
- Falta una capa `assembler` o `hateoas`.
- Falta `advice` o `handler` para errores globales.
- Si el laboratorio exige HAL, el codigo actual no lo implementa.

### 4.2 Identidad del proyecto
Situacion actual:
- Carpeta/repo: `lab3-20261`
- artifactId: `vuelokbt`
- application.name: `vuelokbt`
- base package: `com.udea.vuelokbt`
- clase main: `VuelokbtApplication`

Evaluacion:
- Internamente el nombre `vuelokbt` es consistente.
- Externamente el repo `lab3-20261` y el README no explican esa identidad.
- Como no hay Dockerfile ni YAML, no se puede verificar coherencia con imagen Docker o nombres Kubernetes.

## 5. Swagger / OpenAPI

Estado actual:
- Dependencia presente.
- Configuracion `OpenApiConfig` presente.
- La URL esperada en Springdoc 1.7 es `/swagger-ui/index.html`.

Lo que no se pudo verificar:
- Que el backend arranque correctamente.
- Que Swagger liste todos los endpoints.
- Que los modelos tengan esquema correcto.

Capturas recomendadas para el informe cuando funcione:
- Swagger UI completo abierto en `/swagger-ui/index.html`.
- Seccion del endpoint `POST /flight/save` desplegada.
- Seccion del endpoint `GET /flight/topFlights` desplegada.
- Respuesta de ejemplo de un endpoint ejecutado desde Swagger.

## 6. Pruebas funcionales recomendadas en Postman

Base URL local esperada:
- `http://localhost:8089`

### 6.1 Crear vuelo valido
Request:

```http
POST /flight/save
Content-Type: application/json
```

Body:

```json
{
  "nombreAvion": "Airbus A320",
  "numeroVuelo": "AV123",
  "origen": "Medellin",
  "destino": "Bogota",
  "capacidad": 180,
  "rating": 5,
  "planvuelo": 202605210830,
  "cumplido": true
}
```

Esperado recomendado:
- HTTP `201 Created`
- Respuesta con el vuelo creado o al menos su `idFlight`

Comportamiento probable actual:
- HTTP `200 OK`
- retorna `Long`

Captura sugerida:
- URL, body, status y respuesta.

### 6.2 Crear vuelo con rating invalido
Body sugerido:

```json
{
  "nombreAvion": "Boeing 737",
  "numeroVuelo": "LA456",
  "origen": "Bogota",
  "destino": "Cali",
  "capacidad": 160,
  "rating": 6,
  "planvuelo": 202605211030,
  "cumplido": true
}
```

Esperado recomendado:
- HTTP `400 Bad Request`
- mensaje claro: `Rating should be between 1 and 5`

Comportamiento probable actual:
- Puede terminar en `500` porque `InvalidRating` no esta mapeada a `400`.

Captura sugerida:
- body enviado, status y payload de error.

### 6.3 Listar todos
Request:

```http
GET /flight/listAll
```

Esperado recomendado:
- HTTP `200 OK`
- Lista de vuelos o `[]`

Captura sugerida:
- JSON completo.

### 6.4 Consultar por ID existente
Request:

```http
GET /flight/list/1
```

Esperado recomendado:
- HTTP `200 OK`
- Vuelo encontrado

Captura sugerida:
- request y respuesta.

### 6.5 Consultar por ID inexistente
Request:

```http
GET /flight/list/9999
```

Esperado recomendado:
- HTTP `404 Not Found`
- mensaje JSON estructurado

Comportamiento actual probable:
- `404`, pero con respuesta no estandarizada.

### 6.6 Obtener topFlights
Request:

```http
GET /flight/topFlights
```

Esperado recomendado:
- HTTP `200 OK`
- Lista ordenada descendentemente por rating y con `cumplido=true`

Comportamiento actual:
- devuelve `202 Accepted`
- no garantiza orden
- si lista vacia, lanza `404`

### 6.7 Actualizar
Request:

```http
PUT /flight
Content-Type: application/json
```

Body:

```json
{
  "idFlight": 1,
  "nombreAvion": "Airbus A321",
  "numeroVuelo": "AV123",
  "origen": "Medellin",
  "destino": "Cartagena",
  "capacidad": 185,
  "rating": 4,
  "planvuelo": 202605211100,
  "cumplido": true
}
```

Esperado recomendado:
- HTTP `200 OK`
- recurso actualizado

Comportamiento actual probable:
- si el ID no existe: `500` por `NullPointerException`

### 6.8 Eliminar
Request:

```http
DELETE /flight/1
```

Esperado recomendado:
- HTTP `204 No Content`

Comportamiento actual:
- HTTP `200 OK`
- body `Flight deleted`

Captura sugerida:
- status y luego evidencia de que el recurso ya no existe.

## 7. Docker

Estado actual del repositorio:
- No existe `Dockerfile`.

Impacto:
- No se puede construir ni probar la imagen del laboratorio desde este repositorio.

Dockerfile minimo recomendado:

```dockerfile
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/vuelokbt-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Comando de construccion recomendado:

```powershell
docker build -t vuelokbt:1.0 .
```

Errores comunes a evitar:
- copiar un JAR con nombre incorrecto
- olvidar `mvn package` antes del build
- exponer puerto distinto a `8089`
- no usar `imagePullPolicy: Never` en Minikube si la imagen se construye localmente dentro de su daemon Docker

Capturas requeridas:
- `mvnw.cmd -DskipTests package` exitoso
- `docker build -t vuelokbt:1.0 .` exitoso
- `docker images`
- opcional: `docker run -p 8089:8089 vuelokbt:1.0`

## 8. Kubernetes y Minikube

Estado actual del repositorio:
- No existen `mysql-configmap.yml`, `mysql-deployment.yml`, `app-deployment.yml`, `restoct-deployment.yml` ni `kbt-deployment.yml`.

Conclusiones:
- No se puede validar despliegue Kubernetes porque los manifiestos no existen.
- No se puede verificar `labels`, `selectors`, `imagePullPolicy`, `replicas`, `Service`, `env vars` ni coherencia JDBC.

Manifiestos recomendados:
- `k8s/mysql-configmap.yml`
- `k8s/mysql-secret.yml`
- `k8s/mysql-deployment.yml`
- `k8s/mysql-service.yml`
- `k8s/app-deployment.yml`
- `k8s/app-service.yml`

Valores recomendados:
- MySQL Service: `mysql`
- JDBC URL desde app:

```properties
jdbc:mysql://mysql:3306/vuelokbt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

- `imagePullPolicy: Never` para Minikube
- puerto app: `8089`
- puerto mysql: `3306`
- replicas app: al menos `2`

Plantilla minima conceptual para app Deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: vuelokbt-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: vuelokbt-app
  template:
    metadata:
      labels:
        app: vuelokbt-app
    spec:
      containers:
        - name: vuelokbt-app
          image: vuelokbt:1.0
          imagePullPolicy: Never
          ports:
            - containerPort: 8089
          env:
            - name: SPRING_DATASOURCE_URL
              value: jdbc:mysql://mysql:3306/vuelokbt?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: username
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: password
```

## 9. Secuencia exacta de comandos para Minikube

Precondiciones:
- Docker Desktop o Docker Engine funcional
- Minikube instalado
- kubectl instalado
- proyecto empaquetando correctamente
- manifiestos Kubernetes creados

Secuencia recomendada:

```powershell
minikube start --driver=docker
```
Captura: salida de inicio exitoso.

```powershell
minikube status
```
Captura: estado `Running`.

```powershell
minikube docker-env --shell powershell | Invoke-Expression
```
Captura: opcional, evidencia de variables del daemon Docker de Minikube.

```powershell
.\mvnw.cmd -DskipTests package
```
Captura: `BUILD SUCCESS`.

```powershell
docker build -t vuelokbt:1.0 .
```
Captura: build exitoso.

```powershell
docker images
```
Captura: imagen `vuelokbt:1.0` listada.

```powershell
kubectl apply -f k8s/mysql-configmap.yml
kubectl apply -f k8s/mysql-secret.yml
kubectl apply -f k8s/mysql-deployment.yml
kubectl apply -f k8s/mysql-service.yml
kubectl apply -f k8s/app-deployment.yml
kubectl apply -f k8s/app-service.yml
```
Captura: recursos `created` o `configured`.

```powershell
kubectl get pods -o wide
```
Captura: pods MySQL y app en `Running`.

```powershell
kubectl get svc
```
Captura: servicios MySQL y app.

```powershell
minikube service vuelokbt-service --url
```
Captura: URL expuesta.

```powershell
kubectl logs deployment/vuelokbt-app
```
Captura: app iniciando correctamente.

```powershell
kubectl exec -it deployment/mysql -- mysql -uroot -p
```
Captura: ingreso a consola MySQL.

Luego en consola MySQL:

```sql
SHOW DATABASES;
USE vuelokbt;
SHOW TABLES;
SELECT * FROM flight;
```
Captura: base, tabla y datos.

## 10. ArgoCD / GitOps

Estado actual:
- No hay manifiestos ni estructura GitOps en el repo.

Estructura recomendada del repositorio para ArgoCD:

```text
lab3-20261/
  src/
  Dockerfile
  k8s/
    mysql-configmap.yml
    mysql-secret.yml
    mysql-deployment.yml
    mysql-service.yml
    app-deployment.yml
    app-service.yml
  argocd/
    application.yml
  postman/
    Vuelokbt.postman_collection.json
  README.md
```

Manifiesto ArgoCD minimo esperado:
- Un `Application` apuntando al repo, rama y carpeta `k8s/`.

Capturas requeridas:
- instalacion o acceso a UI ArgoCD
- aplicacion creada en ArgoCD
- estado `Synced`
- estado `Healthy`
- pods creados via sincronizacion

## 11. Escalabilidad y prueba de carga

Estado actual:
- No hay Deployment Kubernetes, por tanto no hay replicas configuradas.

Recomendacion de replicas:
- Empezar con `replicas: 2`
- Escalar a `4` para la prueba de carga

Comandos para escalar:

```powershell
kubectl scale deployment/vuelokbt-app --replicas=4
kubectl get deployment vuelokbt-app
kubectl get pods -l app=vuelokbt-app
```

Prueba de carga con JMeter:
- Thread Group: 50, 100 y 200 usuarios
- Ramp-up: 10 a 30 segundos
- Loop count: 5 a 20
- Probar principalmente:
  - `GET /flight/listAll`
  - `GET /flight/topFlights`
  - `POST /flight/save`

Evidencias a capturar:
- numero de replicas antes y despues
- pods activos
- tiempo promedio de respuesta
- throughput
- porcentaje de error
- respuesta estable durante el escalado

## 12. Problemas encontrados y correcciones necesarias

### 12.1 Errores y brechas confirmadas
1. La aplicacion no pasa la validacion `mvn test` por credenciales o permisos MySQL incorrectos.
2. No existe Dockerfile.
3. No existen manifiestos Kubernetes.
4. No existe configuracion ArgoCD.
5. No existe implementacion HATEOAS real.
6. `FlightController` usa codigos HTTP no RESTful en varios endpoints.
7. `InvalidRating` no esta mapeada a `400`.
8. `FlightService.update()` puede fallar con `NullPointerException`.
9. `FlightService.delete()` no maneja inexistencia.
10. La entidad `Flight` tiene validaciones incompletas y anotaciones redundantes.
11. Se exponen credenciales y Actuator en forma insegura.
12. No hay pruebas funcionales automatizadas ni perfil de test.
13. `README.md` esta practicamente vacio.
14. `spring-boot-starter-data-rest` puede exponer endpoints no deseados y crear duplicidad con el controlador manual.

### 12.2 Archivos que deberian corregirse o crearse
Modificar:
- `pom.xml`
- `README.md`
- `src/main/resources/application.properties`
- `src/main/java/com/udea/vuelokbt/model/Flight.java`
- `src/main/java/com/udea/vuelokbt/service/FlightService.java`
- `src/main/java/com/udea/vuelokbt/controller/FlightController.java`
- `src/main/java/com/udea/vuelokbt/exception/InvalidRating.java`
- `src/test/java/com/udea/vuelokbt/VuelokbtApplicationTests.java`

Crear:
- `src/main/java/com/udea/vuelokbt/exception/GlobalExceptionHandler.java`
- `src/main/java/com/udea/vuelokbt/hateoas/FlightModelAssembler.java`
- `Dockerfile`
- `k8s/mysql-configmap.yml`
- `k8s/mysql-secret.yml`
- `k8s/mysql-deployment.yml`
- `k8s/mysql-service.yml`
- `k8s/app-deployment.yml`
- `k8s/app-service.yml`
- `argocd/application.yml`
- `postman/Vuelokbt.postman_collection.json`

## 13. Secuencia exacta de implementacion recomendada

1. Corregir `application.properties` para usar variables de entorno y separar perfil local/test/k8s.
2. Corregir la entidad `Flight` con validaciones Bean Validation reales y eliminar redundancias Lombok/manuales.
3. Corregir `FlightService` para no usar `orElse(null)` en `update`.
4. Corregir `FlightController` para usar `@Valid`, `ResponseEntity` y codigos HTTP correctos.
5. Crear `GlobalExceptionHandler`.
6. Implementar `FlightModelAssembler` y devolver `EntityModel` y `CollectionModel`.
7. Decidir si mantener o eliminar `spring-boot-starter-data-rest`; si se mantienen controladores manuales, es mejor quitarlo o desexportar el repositorio.
8. Crear perfil de test con H2 o Testcontainers para que `mvn test` no dependa de MySQL local.
9. Crear `Dockerfile` y validar imagen.
10. Crear manifiestos Kubernetes y probar en Minikube.
11. Crear manifiesto ArgoCD para sincronizar carpeta `k8s/`.
12. Crear coleccion Postman exportada y documentar respuestas esperadas.
13. Actualizar `README.md` con todo el flujo.
14. Ejecutar pruebas funcionales y tomar capturas.
15. Ejecutar prueba de carga y documentar evidencia.

## 14. Checklist de capturas para el informe

### Desarrollo local
- `mvnw.cmd -DskipTests package` exitoso
- arranque de MySQL local o contenedor MySQL
- aplicacion levantada en `http://localhost:8089`

### Swagger
- `/swagger-ui/index.html`
- endpoint `POST /flight/save`
- endpoint `GET /flight/topFlights`
- respuesta de una operacion desde Swagger

### Postman
- crear vuelo valido
- error por rating invalido
- listar todos
- consultar por ID existente
- consultar por ID inexistente
- topFlights
- actualizar
- eliminar

### Docker
- `docker build`
- `docker images`
- opcional: `docker ps` o respuesta del contenedor

### Kubernetes
- `kubectl apply` exitoso
- `kubectl get pods`
- `kubectl get svc`
- URL de `minikube service`
- logs de la app
- ingreso al pod MySQL
- `SHOW DATABASES`
- `SHOW TABLES`

### ArgoCD
- UI de ArgoCD
- aplicacion registrada
- estado `Synced`
- estado `Healthy`
- recursos creados

### Escalabilidad
- replicas iniciales
- replicas escaladas
- pods activos
- resultados JMeter

## 15. Estructura profesional del informe final

1. Portada
2. Objetivos
3. Marco teorico breve
4. Arquitectura de la solucion
5. Explicacion del proyecto
6. Modelo de datos
7. Endpoints REST y HATEOAS
8. Configuracion local y conexion a MySQL
9. Swagger/OpenAPI
10. Pruebas funcionales con Postman
11. Dockerizacion
12. Despliegue en Kubernetes/Minikube
13. Validacion de MySQL dentro del cluster
14. ArgoCD y GitOps
15. Escalabilidad horizontal y prueba de carga
16. Problemas encontrados y soluciones aplicadas
17. Conclusiones
18. Anexos con comandos y capturas

## 16. Riesgos y recomendaciones tecnicas

Riesgos:
- Fallo de despliegue por credenciales fijas o URL JDBC incorrecta.
- Inconsistencias REST por mezclar Spring Data REST y controlador manual.
- Errores 500 por validacion incompleta y manejo deficiente de excepciones.
- Dificultad para aprobar el laboratorio por ausencia total de Docker/Kubernetes/GitOps.
- Swagger inaccesible mientras la app no levante por base de datos.

Recomendaciones:
- Separar configuracion por perfiles.
- Usar `ResponseEntity` en todos los endpoints.
- Implementar HATEOAS real, no solo declarar la dependencia.
- Proteger credenciales con `Secret` en Kubernetes.
- Limitar Actuator a endpoints necesarios.
- Añadir pruebas con H2/Testcontainers.
- Documentar todo en README y exportar Postman.

## 17. Confirmacion final de cumplimiento

Confirmacion actual:
- El proyecto NO cumple la guia completa del laboratorio.

Razon:
- Cumple solo una base parcial del backend Spring Boot.
- Faltan entregables esenciales del laboratorio: HATEOAS real, Docker, Kubernetes, Minikube funcional, GitOps con ArgoCD, escalabilidad y evidencia de pruebas.
- Ademas, la ejecucion validada falla por acceso a MySQL.

## 18. Que falta validar despues de corregir

Estos puntos no pueden confirmarse sin ejecutar nuevamente el proyecto con infraestructura lista:
- apertura real de `/swagger-ui/index.html`
- persistencia correcta contra MySQL
- creacion real de tabla `flight`
- respuesta real de los endpoints con datos
- despliegue Docker
- despliegue en Minikube
- sincronizacion en ArgoCD
- escalado horizontal bajo carga

Una vez corrijas esos puntos, la siguiente validacion minima deberia ser:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
docker build -t vuelokbt:1.0 .
kubectl apply -f k8s/
kubectl get pods
kubectl get svc
```
