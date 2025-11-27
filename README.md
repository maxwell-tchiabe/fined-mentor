<p align="center">
    <h1 align="center">🎓 FinEd Mentor 🎓</h1>
    <h3 align="center">AI-Powered Financial Education Platform</h3>
</p>

<p align="center">
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5-green" />
    <img alt="Angular" src="https://img.shields.io/badge/Angular-18-red" />
    <img alt="Docker" src="https://img.shields.io/badge/Docker-Enabled-blue" />
    <img alt="Kubernetes" src="https://img.shields.io/badge/Kubernetes-Ready-326ce5" />
</p>

## Table of Contents

- [Table of Contents](#table-of-contents)
- [Course Overview](#course-overview)
- [Who is this project for?](#who-is-this-project-for)
- [What you'll learn](#what-youll-learn)
- [Getting started](#getting-started)
- [Course syllabus](#course-syllabus)
- [The tech stack](#the-tech-stack)
- [Contributors](#contributors)
- [License](#license)

## Course Overview

**FinEd Mentor** is an intelligent learning platform that combines modern web technologies with Generative AI to provide personalized education in Finance, Real Estate, and Investment.

By the end of this project, you'll have built a complete Fullstack application capable of:

*   Engaging in realistic conversations about finance using **Google GenAI** 🧠
*   Generating personalized quizzes on the fly 📝
*   Managing secure user authentication with **JWT** 🔐
*   Deploying to a production-grade **Kubernetes** cluster ☁️

> You can think of it as your personal AI financial advisor, available 24/7.

---

## Who is this project for?

This project is for Software Engineers, Fullstack Developers, and DevOps enthusiasts who want to master the art of building scalable, AI-integrated applications. It covers everything from code to cloud.

## What you'll learn

*   Build a robust Backend with **Spring Boot 3**
*   Create a dynamic Frontend with **Angular 18** and **Signals**
*   Integrate **Google Gemini** for AI chat and content generation
*   Secure your API with **Spring Security** and **JWT**
*   Containerize applications with **Docker**
*   Orchestrate microservices with **Kubernetes**
*   Implement a GitOps pipeline with **Jenkins** and **Argo CD**

## Getting started

To get started with FinEd Mentor locally:

1.  **Clone the repository**
2.  **Backend**: Navigate to `backend/` and run `mvn spring-boot:run`
3.  **Frontend**: Navigate to `frontend/` and run `npm start`
4.  **Infrastructure**: Check the `k8s/` directory for deployment manifests.

## Course syllabus

| Step | Topic | Description |
|:---:|---|---|
| <div align="center">1</div> | **Vue d'ensemble (High-Level Overview)** | Understand the project architecture, the problem it solves, and the high-level design. |
| <div align="center">2</div> | **Authentification Spring Security** | Implement secure authentication using **JWT** and email validation with **OTP**. |
| <div align="center">3</div> | **API Chat et Quiz (Spring AI)** | Integrate **Google GenAI** to power the chat interface and generate dynamic quizzes. |
| <div align="center">4</div> | **Gestion de l'État (State Management)** | Master **Angular Signals** for reactive and efficient state management in the frontend. |
| <div align="center">5</div> | **Modules Principaux** | Deep dive into the core modules: **Authentification**, **Chat**, and **Quiz**. |
| <div align="center">6</div> | **Dockeriser le Backend** | Create optimized Docker images for the **OpenJDK** Spring Boot application. |
| <div align="center">7</div> | **Dockeriser le Frontend** | Containerize the Angular app using **Nginx** as a high-performance web server. |
| <div align="center">8</div> | **Orchestration : Kubernetes** | Deploy the full stack to a **Kubernetes** cluster with Ingress, Services, and Pods. |
| <div align="center">9</div> | **CI/CD : Jenkins & Argo CD** | Automate the pipeline: **Jenkins** for CI (Build/Test) and **Argo CD** for CD (GitOps). |

## The tech stack

<table>
  <tr>
    <th>Technology</th>
    <th>Description</th>
  </tr>
  <tr>
    <td><img src="https://upload.wikimedia.org/wikipedia/commons/4/44/Spring_Framework_Logo_2018.svg" width="100" alt="Spring Boot"/></td>
    <td>**Spring Boot 3.5**: The backbone of our backend, providing a robust and scalable REST API.</td>
  </tr>
  <tr>
    <td><img src="https://angular.io/assets/images/logos/angular/angular.svg" width="100" alt="Angular"/></td>
    <td>**Angular 18**: A modern frontend framework using Signals for reactive UI components.</td>
  </tr>
  <tr>
    <td><img src="https://webimages.mongodb.com/_com_assets/cms/kuyjf3vea2hg347wt-Logos_Lockup_Full%20Color_rgb.svg" width="100" alt="MongoDB"/></td>
    <td>**MongoDB**: Flexible NoSQL database for storing users, chats, and quizzes.</td>
  </tr>
  <tr>
    <td><img src="https://www.docker.com/wp-content/uploads/2022/03/vertical-logo-monochromatic.png" width="100" alt="Docker"/></td>
    <td>**Docker**: Containerization for consistent environments across dev and prod.</td>
  </tr>
  <tr>
    <td><img src="https://upload.wikimedia.org/wikipedia/commons/3/39/Kubernetes_logo_without_workmark.svg" width="100" alt="Kubernetes"/></td>
    <td>**Kubernetes**: Orchestration platform for managing our microservices at scale.</td>
  </tr>
  <tr>
    <td><img src="https://upload.wikimedia.org/wikipedia/commons/e/e9/Jenkins_logo.svg" width="80" alt="Jenkins"/></td>
    <td>**Jenkins**: Automation server for building and testing our code (CI).</td>
  </tr>
  <tr>
    <td><img src="https://argo-cd.readthedocs.io/en/stable/assets/logo.png" width="100" alt="Argo CD"/></td>
    <td>**Argo CD**: GitOps continuous delivery tool for Kubernetes.</td>
  </tr>
</table>

## Contributors

<table>
  <tr>
    <td align="center"><img src="https://github.com/github.png" width="100" style="border-radius:50%;"/></td>
    <td>
      <strong>Maxwell Tchiabe</strong><br />
      <i>Fullstack Engineer & DevOps Enthusiast</i><br /><br />
      <a href="#">LinkedIn</a><br />
      <a href="#">GitHub</a>
    </td>
  </tr>
</table>

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
