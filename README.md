# 📱 AppTiendas - Prototipo 2

**Instituto Profesional Santo Tomás** 🎓

**Integrantes:**
* Tomás Ignacio Otáegui Cerda
* Jorge Barrientos.

---

## 📝 Resumen del Proyecto
AppTiendas es una aplicación Android desarrollada para gestionar y compartir información de tiendas locales. Este segundo prototipo se enfoca en la implementación de una navegación fluida mediante Intents, integrando interacciones internas y funciones nativas del sistema operativo. El desarrollo incluye un fuerte enfoque en el manejo de excepciones (bloques `try-catch`) y validación de nulos para garantizar la estabilidad técnica de la aplicación.

⚙️ **Entorno de Desarrollo:**
* **Versión de Android Studio:** Jellyfish 2023.3.1 *(o la versión exacta que usaron)*
* **Android Gradle Plugin (AGP):** 8.4.0 *(o la versión exacta)*
* **SDK Mínimo:** API 24

---

## 🚀 Funcionalidades e Intents Implementados

A continuación se detallan los 8 eventos implementados.

### 📂 Intents Explícitos (Navegación Interna)
1. **Ver Resumen de Tienda (`MainActivity` -> `DetalleActivity`)**
   * **Descripción:** Navega a la vista de resumen enviando información inyectada mediante `putExtra` (Nombre y Web).
   * **Prueba:** Presionar el botón "Ver Resumen de Tienda". Verifica que la pantalla cargue la vista detallada con los datos transferidos sin errores.
2. **Ajustes de Calidad (`MainActivity` -> `ConfigActivity`)**
   * **Descripción:** Simula una vista de configuración interna. Implementa el botón "Atrás" nativo de la Toolbar y un botón delineado físico.
   * **Prueba:** Presionar "Ajustes de Calidad". Validar la navegación y retornar a la pantalla inicial presionando la flecha superior o el botón inferior.
3. **Visor de Fotografía (`MainActivity` -> `PhotoActivity`)**
   * **Descripción:** Envía y recepciona la URI de una imagen capturada validando el dato para evitar excepciones nulas.
   * **Prueba:** Presionar "Mostrar Foto Capturada". El sistema validará la URI temporal y mostrará la carga exitosa.

### 🌍 Intents Implícitos (Acciones del Sistema)
4. **Abrir Mapa (Ubicación)**
   * **Descripción:** Ejecuta una búsqueda de coordenadas geográficas (`geo:lat,lng`).
   * **Prueba:** Presionar el botón de ubicación en la tienda; el sistema lanzará Google Maps o la app de GPS predeterminada.
5. **Visitar Web de la Tienda (`ACTION_VIEW`)**
   * **Descripción:** Abre el enlace web de la tienda en el navegador móvil.
   * **Prueba:** Ejecutar la acción sobre el enlace web.
6. **Llamar a la Tienda (`ACTION_DIAL`)**
   * **Descripción:** Traspasa el número telefónico al marcador del dispositivo sin iniciar la llamada automáticamente por seguridad.
   * **Prueba:** Presionar el botón de llamada.
7. **Compartir por Mensajería (`ACTION_SENDTO`)**
   * **Descripción:** Abre las opciones para enviar un SMS o mensaje de WhatsApp con la información del resumen.
   * **Prueba:** Presionar "Compartir por SMS / WhatsApp" en `DetalleActivity`.
8. **Activar Cámara (`ACTION_IMAGE_CAPTURE`)**
   * **Descripción:** Solicita permiso de hardware y activa la cámara para capturar la fotografía de la tienda.
   * **Prueba:** Presionar "Tomar Fotografía" en el menú de funciones externas.

---

## 📸 Capturas de Pantalla

1. ![Menú Principal](Menu%20Principal%20AppTienda.png)
   *Vista del Menú Principal*
2. ![Configuración](Ajustes%20de%20Fotografia%20AppTienda.png)
   *Pantalla de Ajustes de Fotografía*
3. ![Resumen y Datos](Resumen%20del%20Contenido%20AppTienda.png)
   *Recepción de datos extra*
4. ![Uso de Intent Implícito](Uso%20de%20Intent%20AppTienda.png)
   *Activación de un recurso del sistema (Ej. Cámara/Mapa)*

---

## 📦 Compilación y Ejecución (APK)

El archivo ejecutable para pruebas (debug) ha sido generado y se encuentra disponible directamente en la siguiente ruta de este repositorio:
📁 `app/build/outputs/apk/debug/app-debug.apk`

**Instrucciones de ejecución local:**
1. Clonar este repositorio.
2. Posicionarse en la rama de desarrollo: `git checkout feature/intents`
3. Abrir el proyecto en Android Studio.
4. Sincronizar Gradle y compilar en un dispositivo físico o emulador.
