# AppVeterinaria 🐾

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20/%20Offline--First-0052CC?style=flat-square)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

Aplicación Android nativa diseñada bajo el enfoque **Offline-First** para la gestión integral de clínicas veterinarias. Permite administrar de forma eficiente los flujos críticos de la práctica médica diaria mediante una interfaz moderna, limpia y reactiva.

## 🚀 Características Principales

- **Dashboard Predictivo Inteligente:** La pantalla de inicio analiza la persistencia local y despliega de forma dinámica los próximos 2 pacientes del día ordenados cronológicamente para el personal médico.
- **Buscador Predictivo en Tiempo Real:** Filtrado instantáneo mediante flujos asíncronos (`StateFlow` + `combine`) en los listados de Dueños y Mascotas a medida que se escribe.
- **Módulo de Ficha Clínica Avanzada:** Gestión detallada de consultas con entradas expandidas para diagnósticos de texto largo y tratamientos/recetas.
- **Generación Nativa de PDFs:** Renderizado local de recetas médicas en formato PDF (`PdfDocument`) listas para ser compartidas instantáneamente por correo o WhatsApp mediante `FileProvider`.
- **Integración Local de Comunicaciones:** Accesos directos en tarjetas para iniciar llamadas telefónicas o chats de WhatsApp con los propietarios con un solo toque utilizando Intents nativos.
- **Diseño Limpio Basado en Diálogos:** Formularios ocultos mediante botones flotantes (**FAB**) que despliegan ventanas emergentes de Material 3, maximizando el espacio de visualización de los datos.

## 🛠️ Stack Técnico

| Capa | Componente Tecnológico |
|---|---|
| **UI / Presentación** | Jetpack Compose, Material 3, Floating Action Buttons (FAB), Navigation Compose |
| **Arquitectura** | MVVM (Model-View-ViewModel), Patrón de Repositorio Unificado, Kotlin Coroutines (Flows) |
| **Persistencia Local** | **Room Database** (Esquema relacional con UUIDs descentralizados), **DataStore Preferences** |
| **Automatización / CI** | **GitHub Actions** (Compilación remota automatizada y entrega de artefactos APK) |

---

## 📂 Estructura del Proyecto

```text
AppVeterinaria/
├── .github/workflows/ # Automatización de integración continua (CI)
├── app/               # Módulo de UI (Screens, ViewModels, Components)
├── data/              # Capa de Datos (Room DB, DAOs, Mappers, Repositorio)
└── util/              # Módulo de Utilidades (PdfGenerator, Validadores)
```

---
