# 🌿 ApiPlant - API para gestión de información sobre plantas

**ApiPlant** es una API desarrollada en Java con Spring Boot que permite gestionar información sobre plantas (categorias, cuidados, plagas y consejos). 


---

## 🔗 Repositorio

GitHub: [https://github.com/Mari-Gigi/ApiPlant](https://github.com/Mari-Gigi/ApiPlant)

---

## 🌐 Acceso a la Base de Datos (H2)

H2 Console: [http://localhost:8080/h2-console](http://localhost:80/h2-console)

> **Nota:** Asegúrate de que la consola H2 esté habilitada en `application.properties` y que la aplicación esté corriendo.


---

## 🧪 Pruebas de la API

Existe una colección en **Hoppscotch** para probar todos los endpoints de la API.

🔗 [Acceder a la colección en Hoppscotch](https://hoppscotch.io)  

---

## ⚙️ Tecnologías

- Java 17  
- Spring Boot  
- Spring Data JPA  
- H2 Database (entorno local / pruebas) -> puerto 8080
- WireMock (Mock API para pruebas)
- Hoppscotch (colección de tests de los casos de uso)
- JUnit 5 + Spring Boot Test (tests unitarios e integración)

---

## 🧩 Proyecto

**Estructura ApiPlant:**
- Config
- Controller
- Domain (dtos) -> Planta, Cuidado, Categoria, Plaga, Consejo
- Exception
- Repository
- Service

**Endpoints:**
- GET / GetById / POST / PUT / DELETE de cada una de las clases.

**Relaciones entre clases:**
- Planta - Cuidados - 1:1
- Planta - Categoría - 1:1
- Planta - Plaga - N:N
- Planta - Consejo - N:N
- 
**Gestión de errores:**
- 200 - Ok
- 201 - Created
- 204 - No content
- 400 - Bad Request
- 404 - Not Found
- 409 - Conflict
- 500 - Server Error

---

## 🚀 Arrancar ApiPlant

**Arrancar ApiPlant:**

~~~  
  mvn spring-boot:run
~~~    

La primera vez se instalarán todas las dependencias especificadas en el pom.xml  
Si alguna dependencia no se ha instalado bien o algo no funciona correctamente con este comando se fuerza a recompilar:  

~~~  
  mvn clean install
~~~

## 🧪 Testing

En ApiPlant se han implementado tres capas de pruebas:

---

### 🔹 1. Tests Unitarios
- Realizados con **JUnit 5 y Mockito**.
- Aíslan y validan la lógica de los servicios y controladores.
- Ejemplo: comprobar que `PlantaService` devuelve la planta correcta al invocar `findById`.


### 🔹 2. Tests de Integración
- Usan `@SpringBootTest`.
- Validan la interacción entre capas (**Controller + Service + Repository**).
- Utilizan la base de datos en memoria **H2**.

Para ejecutarlos:
~~~  
  mvn test
~~~

---

### 🔹 3. Mock API con WireMock
Se incluye una **MockAPI** para simular las respuestas de la API y poder validar los **casos de uso** sin necesidad de levantar toda la aplicación.

**Arrancar WireMock:**

~~~  
  java -jar wiremock-jre8-standalone-2.32.0.jar
~~~

### 🔹 4. Casos de uso + Tests en Hoppscotch

Se ha preparado una **colección de Hoppscotch** para probar los endpoints reales y mockeados.

**Ejemplos de tests incluidos en cada request:**
- Validación de códigos de estado (`200`, `201`, `204`, `400`, `404`).
- Validación de cabeceras (ej: `Content-Type = application/json`).
- Validación de propiedades del body JSON (ej: `id_planta` es numérico).
- Respuestas vacías en `DELETE` (código `204` y body vacío).

**La colección permite:**
- Ejecutar los **casos de uso principales** en cada una de las clases de la API.
- Validar automáticamente mediante scripts en **JavaScript embebido** que la API responde según lo esperado.

---


✍️ Autoría
Mari Gigi
GitHub: @Mari-Gigi
