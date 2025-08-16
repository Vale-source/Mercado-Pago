# Integración de Mercado Pago con Java (Spring Boot)

Este proyecto demuestra cómo integrar pagos con **Mercado Pago** en un backend Java 21 con Spring Boot, usando dos modalidades distintas:

- **Checkout Pro** → redirección al sitio de Mercado Pago.  
- **API Orders** → creación y gestión de órdenes de pago desde tu backend.  

---

## Explicación

Este sistema permite que un negocio online reciba pagos con tarjetas de crédito o débito.  
Existen dos caminos principales:

1. **Checkout Pro**  
   - El cliente hace clic en un botón de pago.  
   - Es llevado a la página oficial de Mercado Pago.  
   - Allí completa los datos de tarjeta de forma segura.  
   - Luego vuelve al sitio con el resultado del pago.  


2. **API Orders**  
   - El sistema crea un “pedido” directamente en la API de Mercado Pago.  
   - El backend controla todo el proceso de pago.  
   - Permite integrar reglas propias, dividir pagos o personalizar la experiencia.  


---

## Guia de implementacion

### Requisitos
- Java 21  
- Maven o Gradle  
- Credenciales de **Mercado Pago** (PUBLIC_KEY y ACCESS_TOKEN)  

### Instalación y Ejecución
```bash
# Clonar el repositorio
git clone https://github.com/Vale-source/Mercado-Pago.git
cd Mercado-Pago

# Para ejecutar directamente con Maven
mvn spring-boot:run o ejecutar desde el IDE

# En caso de utilizar docker, solamente compila el proyecto
mvn clean package -DskipTests
```

## Cuándo usar cada integración

| Integración     | Uso recomendado |
|-----------------|-----------------|
| ✅ Checkout Pro | Pagos con dinero en cuenta o en efectivo **(sin split payment)** |
| 🔧 API Orders   | Pagos con **tarjetas de crédito/débito (sin split payment)** |
| ✅ Checkout Pro | **Todos los casos con split payment** |

