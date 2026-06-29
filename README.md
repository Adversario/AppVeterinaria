# AppVeterinaria 🐾

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20/%20Offline--First-0052CC?style=flat-square)

Aplicación Android nativa diseñada bajo el enfoque **Offline-First** para la gestión integral de clínicas veterinarias. Permite administrar de forma eficiente los flujos críticos de dueños, mascotas, consultas médicas y agendas de citas mediante un control de accesos basado en roles.

## 🚀 Características Principales

- **Autenticación por Roles (RBAC):**
  - **STAFF:** Control total del ecosistema (CRUD de Dueños, Mascotas, Consultas Médicas y Agenda).
  - **OWNER (Cliente):** Vista optimizada y protegida (Acceso exclusivo a *Mis Mascotas* y *Mis Consultas* asociadas).
- **Gestión Clínica Completa:** Control y seguimiento de historiales de consultas, diagnósticos y tratamientos.
- **Módulo de Agenda Inteligente:** Programación de citas veterinarias con alertas de recordatorios locales implementadas mediante **WorkManager**.
- **Registro de Actividad:** Log localizado de eventos críticos del sistema para auditorías internas.
- **Arquitectura Robusta:** Modularización limpia dividida en componentes específicos (`:app`, `:data`, `:util`).

## 🛠️ Stack Técnico

| Capa | Componente Tecnológico |
|---|---|
| **UI / Presentación** | Jetpack Compose, Material 3, Navigation Compose |
| **Arquitectura** | MVVM (Model-View-ViewModel), Patrón de Repositorio Unificado |
| **Persistencia Local** | **Room Database** (SQLite adaptado con Flows reactivos), **DataStore Preferences** |
| **Tareas en Background**| WorkManager (Alertas y Notificaciones de Citas) |
| **Inyección / Estructura**| Componentes Modulares Gradle (Kotlin DSL) |

---

## 📂 Estructura del Proyecto

```text
AppVeterinaria/
├── app/          # Módulo de UI (Screens Compose, ViewModels, Navegación)
├── data/         # Capa de Datos (Room DB, DAOs, Entidades, Repositorio Único)
└── util/         # Módulo de Utilidades (Validadores de negocio, formateadores)