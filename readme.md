# Microservices

**Spring Boot 2.0, Eureka, Feign y Spring Cloud Config**

El objetivo de este repositorio es mostrar el patron de arquitectura basado en Microservicios, para ello se usó:  

**Spring Boot 2.0** que nos brinda todo lo necesario para levantar una aplicación con un servidor Tomcat embebido.  
**Eureka** como descubridor de servicios.  
**Open Feign** en conjunto con Eureka para hacer la comunicación entre servicios y balanceador de cargas del lado de cliente ya que Open Feign tiene como base el balanceador llamado Ribbon, ademas ofrece una forma mas limpia de comunicarlos con los demas servicios.
**Spring Cloud Config** como servidor central para obtener las configuraciones de nuestros servicios.


Tenemos 4 servicios en total  
**config-service** servicio Spring Cloud Config donde estaran almacenados los archivos **yml** de configuración de los demas servicios.
**discovery-service** descubridor de servicios Eureka  
**school-service** servicio donde obtenemos informacion de escuelas  
**student-service** servicio para obtener la informacion de los alumnos.

Para crear la base de nuestros servicios podemos usar Spring Initializr  
https://start.spring.io/

En este repositorio se usó **Java 10**

Si usamos Java 10 nos saldra el siguiente error **java.lang.ClassNotFoundException: javax.xml.bind.JAXBContext** esto ocurre porque desde Java 9 la dependencia JAXB fue removida, la solucion es bastante sencilla basta con agregar la siguiente dependencia al pom del servicio que nos muestre el error.

```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
     <artifactId>jaxb-api</artifactId>
 </dependency>
```
<br>
##Servicios
####config-service

Primero hay que crear nuestro servicio de configuraciones porque los demas servicios obtendran de el sus propiedades.

En el pom.xml de nuestro servicio tendremos que agregar la dependencia **spring-cloud-config-server**  o podemos agregarla desde Spring Initializr

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

Para habilitar **Spring Cloud Config** bastara con añadir la siguiente anotacion a nuestra clase principal **@EnableConfigServer**

```java
package com.ga5par.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServiceApplication.class, args);
	}
}
```
El archivo bootstrap.yml quedara de la siguiente forma

```yml
server:
 port: 9000
 
spring:
 profiles:
  active: native
```
Cuando levantamos el servicio estara disponible en http://localhost:9000

Hay que destacar el uso del perfil **native**, por defecto Spring Cloud Config espera obtener las propiedades de los servicios de un repositorio git. Si hacemos este cambio de perfil podemos obtenerlos de forma local, el servicio tomara los archivos de configuración de la siguiente ruta  **src/main/resources/config**.

Si uno de los servicios se llama **example-service** como a continuación

```yml
spring:
 application:
  name: example-service
```
El archivo de propiedades que guardaremos en el directorio **config** debe tener el mismo nombre, en este caso seria **example-service.yml**

##discovery-service

Para hacer nuestro servicio necesitamos Eureka agregamos la dependencia **spring-cloud-starter-netflix-eureka-server**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```
Como tenemos un servidor de configuraciones debemos agregar tambien la dependecia **spring-cloud-starter-config** para obtener las propiedades del servicio.

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```
Para que nuestro servicio Eureka pueda iniciar debemos agregar la siguiente anotacion **@EnableEurekaServer** en nuestra clase principal

```java
package com.ga5par.discoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class DiscoveryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServiceApplication.class, args);
	}
}
```
El archivo de propiedades sera un **boostrap.yml** y no un application.yml porque tenemos un servidor de configuraciones a donde iremos a consultar las propiedades del servicio.

La diferencia entre  **boostrap.yml** y **application.yml** es que
**boostrap.yml** es cargado por un ***ApplicationContext** padre de Spring*. Este ***ApplicationContext** padre* es cargado antes de el ***ApplicationContext*** que carga a **application.yml** o en nuestro caso **discovery-server.yml**


Entonces **boostrap.yml** contendra el nombre del servicio y el uri del servicio de configuraciones

```yml
spring:
 application:
  name: discovery-service
 config:
  cloud:
   uri: http://localhost:9000
```
El archivo alojado en el servicio de configuraciones sera el siguiente
**discovery-service.yml**
```yml
server:
 port: 8500
 
spring:
 application:
  name: discovery-service
 
eureka:
 instance:
  hostname: localhost
 client:
  registerWithEureka: false
  fetchRegistry: false
  serviceUrl:
   defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka
```


###school-service

Usaremos las dependencias **JPA,WEB,H2,Lombok,Cloud Config,EurekaClient y Open Feign**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<scope>runtime</scope>
</dependency>

<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<optional>true</optional>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<dependency>
	<groupId>javax.xml.bind</groupId>
	<artifactId>jaxb-api</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```


**Utilizando Feign**

Creamos una interfaz, agregamos la anotacion **@FeignClient**, en su atributo **name** pondremos el nombre del servicio al que haremos la comunicacion.
Ademas Feign creará un balanceador de cargas Ribbon con ese nombre.

**StudentProxy.java**
```java
@FeignClient(name="student-service")
public interface StudentProxy {
	
	@GetMapping("/api/student/school/{id}")
	public List<Student> findByIdSchool(@PathVariable Long id);

}
```


**SchoolController.java** 

Ahora solo tendremos que inyectar **StudentProxy** y podremos utlizarlo

Ademas utilizarmos **environment** para obtener el puerto del servicio y asignarlo a la propiedad del objeto, esto sirve para realizar pruebas y ver el funcionamiento del balanceador de carga.

El siguiente endpoint respondera con la informacion de una escuela y su lista de estudiantes que consultara a **student-service**
**Endpoint:** http://localhost:8000/api/school/{id}/with-students


```java
@Autowired
private SchoolRepository schoolRepository;

@Autowired
private Environment environment;

@Autowired
private StudentProxy studentProxy;

@GetMapping("/{id}/with-students")
public School withStudents(@PathVariable Long id) {

	School school = schoolRepository.findById(id).get();
	if (!school.equals(null)) {
		Integer port = Integer.parseInt(environment.getProperty("local.server.port"));
		school.setPortSchool(port);
		List<Student> studentList = studentProxy.findByIdSchool(id);
		school.setStudents(studentList);
		return school;
	}
	return null;
}
```

Ahora en la clase principal hay que agregar la anotacion **@EnableFeignClients** 

```java
package com.ga5par.schoolservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SchoolServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchoolServiceApplication.class, args);
	}
}
```

**Registrado servicio en Eureka**

Se usa la dependencia 
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

En versiones anteriores a **Edgware.RC1** de spring.cloud era necesario agregar la anotacion **@EnableDiscoveryClient** en la clase principal para que el servicio fuera capaz de registrarse en Eureka
Pero como usamos la version **Finchley.SR1** ya no es necesario agregarlo.

El archivo **bootstrap.yml** sera el siguiente

```yml
spring:
 application:
  name: school-service
 cloud:
  config:
   uri: http://localhost:8000
```

 El archivo alojado en el servicio de configuraciones sera el siguiente
**school-service.yml**
```yml
server:
 port: 8000
 
jpa:
show-sql: true
  
datasource:
 driver-class-name: org.h2.Driver
 url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
 username: sa
 password:
  
h2:
 console:
 enabled: true
 path: /h2console
   
eureka:
 client:
  serviceUrl:
   defaultZone: http://localhost:8500/eureka
```
Tambien existe otro **yml** con el nombre de **school-service-instance2.yml** es la configuracion para levantar otra instancia solo cambiamos el port a **8001**. Para ejecutarlo hay que mandar el argumento **spring.profiles.active=instace2**  al momento de levantar el servicio.


###student-service

Usaremos las mismas dependencias que school-service  **JPA,WEB,H2,Lombok,Cloud Config,EurekaClient y Open Feign**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<scope>runtime</scope>
</dependency>

<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
	<optional>true</optional>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<dependency>
	<groupId>javax.xml.bind</groupId>
	<artifactId>jaxb-api</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```


**Utilizando Feign**

Creamos una interfaz, agregamos la anotacion **@FeignClient**, en su atributo **name** pondremos el nombre del servicio al que haremos la comunicacion.
Ademas Feign creara un balanceador de cargas Ribbon con ese nombre.

**SchoolProxy.java**
```java
@FeignClient(name="school-server")
public interface SchoolProxy {
	
	@GetMapping("/api/school/{id}")
	public School findById(@PathVariable Long id);

}
```

**StudentController.java** 

Ahora solo tendremos que inyectar **SchoolProxy** y podremos utlizarlo

Ademas utilizarmos **environment** para obtener el puerto del servicio y asigarlo a la propiedad del objeto, esto sirve para realizar pruebas y ver el funcionamiento del balanceador de carga.

El siguiente endpoint respondera con la informacion de un estudiate y la informacion de su escuela que sera consultada a **school-service**
**Endpoint:** http://localhost:8100/api/student/{id}/with-school

```java
@Autowired
private StudentRepository studentRepository;

@Autowired
private SchoolProxy schoolProxy;

@Autowired
Environment environment;

@GetMapping("/{id}/with-school")
public Student withSchool(@PathVariable Long id) {
	Student student = studentRepository.findById(id).get();
	if (!student.equals(null)) {
		Integer port = Integer.parseInt(environment.getProperty("local.server.port"));
		student.setPortStudent(port);
		School school = schoolProxy.findById(student.getIdSchool());
		student.setSchool(school);
		return student;
	}
	return null;
}
```

Ahora en la clase principal hay que agregar la anotacion **@EnableFeignClients** 

```java
package com.ga5par.studentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StudentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentServiceApplication.class, args);
	}
}

```

**Registrado servicio en Eureka**

Se usa la dependencia 
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

El archivo **bootstrap.yml** sera el siguiente

```yml
spring:
 application:
  name: student-service
 cloud:
  config:
   uri: http://localhost:8100
```

 El archivo alojado en el servicio de configuraciones sera el siguiente
**student-service.yml**
```yml
server:
 port: 8100
 
jpa:
 show-sql: true
  
datasource:
 driver-class-name: org.h2.Driver
 url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
 username: sa
 password:
  
h2:
 console:
 enabled: true
 path: /h2console
   
eureka:
 client:
  serviceUrl:
   defaultZone: http://localhost:8500/eureka
```
Tambien existe otro **yml** con el nombre de **student-service-instance2.yml** es la configuracion para levantar otra instancia solo cambiamos el port a **8101**. Para ejecutarlo hay que mandar el argumento **spring.profiles.active=instace2**  al momento de levantar el servicio.

##Iniciar Servicios

Primero deberemos iniciar **config-service** que es servicio que se encargara de distribuir las propiedades de los demas servicios, ahora debemos iniciar Eureka nuestro **discovery-service**, este se conectara a **config-service** para obtener sus propiedades, al igual ahora deberiamos iniciar **school-service y student-service** que tambien iran a **config-service** para obtener sus propiedades.


Al consultar http://localhost:8100/api/student/{id}/with-school veremos a **Ribbon** en accion al mostrar como **portSchool** cambia entre los puertos **8000** y **8001** que son nuestras 2 instancias de **school-service**
