# 🌿 ApiPlant - API REST para gestión de información sobre plantas

**ApiPlant** es una API desarrollada en Java con Spring Boot que permite gestionar información sobre plantas (categorias, cuidados, plagas y consejos). 
Está diseñada como backend.


## Tecnologías

- Java 17  
- Spring Boot  
- Spring Data JPA  
- H2 Database (entorno local / pruebas)  

**Estructura del proyecto:**
- Planta
- Categoría 
- Cuidado
- Plaga
- Consejo

**Relaciones entre clases:**
- Planta - Cuidados - 1:1
- Planta - Categoría - 1:1
- Planta - Plaga - N:N
- Planta - Consejo - N:N

**Endpoints:**
- GET / GetById / POST / PUT / DELETE de cada una de las clases.

**Gestión de errores:**
- 200 - Ok
- 400 - HttpMessageNotReadable, HttpMessageNotReadable (Json o parámetro incorrectos)
- 404 - Not Found
- 409 - Conflict
- 500 - Server Error

**Autoría:**
Mari Gigi
GitHub: @Mari-Gigi
