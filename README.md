# 🌿 ApiPlant - API para gestión de información sobre plantas

**ApiPlant** es una API desarrollada en Java con Spring Boot que permite gestionar información sobre plantas (categorias, cuidados, plagas y consejos). 
Está diseñada como backend.

---

## 🔗 Repositorio

GitHub: [https://github.com/Mari-Gigi/ApiPlant](https://github.com/Mari-Gigi/ApiPlant)

---

## 🌐 Acceso a la Base de Datos (H2)

H2 Console: [http://localhost:8080/h2-console](http://localhost:80/h2-console)

> **Nota:** Asegúrate de que la consola H2 esté habilitada en `application.properties` y que la aplicación esté corriendo.


---

## 🧪 Pruebas de la API

Existe una colección en **Hoppscotch** (alternativa ligera a Postman) para probar todos los endpoints de la API.

🔗 [Acceder a la colección en Hoppscotch](https://hoppscotch.io)  

---

## ⚙️ Tecnologías

- Java 17  
- Spring Boot  
- Spring Data JPA  
- H2 Database (entorno local / pruebas) -> puerto 8080

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
- 400 - HttpMessageNotReadable, HttpMessageNotReadable (Json o parámetro incorrectos)
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


---

✍️ Autoría
Mari Gigi
GitHub: @Mari-Gigi
