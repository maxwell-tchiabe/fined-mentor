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

<table style="border-collapse: collapse; border: none;">
  <tr style="border: none;">
    <td width="20%" style="border: none;">
      <a href="https://maxwelltbtech.substack.com/" aria-label="The Neural Maze">
        <img src="https://substackcdn.com/image/fetch/$s_!O-aM!,w_176,c_limit,f_webp,q_auto:good,fl_progressive:steep/https%3A%2F%2Fsubstack-post-media.s3.amazonaws.com%2Fpublic%2Fimages%2F250dfa6e-5a19-462e-afac-3cedcb07547a_500x500.png" alt=" Maxwell tb tech Logo" width="150"/>
      </a>
    </td>
    <td width="80%" style="border: none;">
      <div>
        <h2>📬 Stay Updated</h2>
        <p><b><a href="https://maxwelltbtech.substack.com/">Join  Maxwell TB Tech</a></b> and Build the future: Full-stack development in the age of AI, directly to your inbox. Don't miss out!</p>
      </div>
    </td>
  </tr>
</table>

<p align="center">
  <a href="https://maxwelltbtech.substack.com/">
    <img src="https://img.shields.io/static/v1?label&logo=substack&message=Subscribe%20Now&style=for-the-badge&color=black&scale=2" alt="Subscribe Now" height="40">
  </a>
</p>


## Getting started

To get started with FinEd Mentor locally:

1.  **Clone the repository**
2.  **Backend**: Navigate to `backend/` and run `mvn spring-boot:run`
3.  **Frontend**: Navigate to `frontend/` and run `npm start`
4.  **Infrastructure**: Check the `k8s/` directory for deployment manifests.

## Course syllabus

| Lesson Number | Written Lesson | Video Lesson | Description |
|---------------|----------------|--------------|-------------|
| <div align="center">1</div> | [Project overview](https://maxwelltbtech.substack.com/p/system-design-and-architecture-fined) | <a href="https://youtu.be/Ar6GB81qmEo"><img src="img/Youtube_Thumbnail_1.png" alt="Thumbnail 1" width="400"></a> | Understand the project architecture, the problem it solves, and the high-level design. |
| <div align="center">2</div> | [securisez-votre-api-tuto-complet](https://maxwelltbtech.substack.com/p/securisez-votre-api-tuto-complet) | <a href="https://youtu.be/vMFdDQqFUqU"><img src="img/Youtube_Thumbnail_2.png" alt="Thumbnail 2" width="400"></a> | Implement secure authentication using **JWT** and email validation with **OTP**. |
| <div align="center">3</div> | [Comprendre l'Architecture en Couches Spring Boot ](https://maxwelltbtech.substack.com/p/comprendre-larchitecture-en-couches)| <a href="https://youtu.be/PDxFdmfDD9c"><img src="img/Youtube Thumbnail_3.png" alt="Thumbnail 3" width="400"></a>  | Integrate **Google GenAI** to power the chat interface and generate dynamic quizzes. |
| <div align="center">4</div> | [Deep dive frontend Angular ](https://maxwelltbtech.substack.com/p/deep-dive-frontend-angular)| <a href="https://youtu.be/e2YiwnT5MlU"><img src="img/Youtube-Thumbnail-jour-4.png" alt="Thumbnail 4" width="400"></a> | Master **Angular Signals** for reactive and efficient state management in the frontend. |
| <div align="center">5</div> | [Dockeriser proprement une appli full‑stack — principes et recette pratique](https://open.substack.com/pub/maxwelltbtech/p/dockeriser-proprement-une-appli-fullstack?r=69je8w&utm_campaign=post&utm_medium=web&showWelcomeOnShare=true)| <a href="https://youtu.be/fNnt2r6RLN0"><img src="img/Youtube_Thumbnail_5.png" alt="Thumbnail 5" width="400"></a> | Dockerfile multi‑stage + Nginx — Dockeriser Backend & Frontend |
| <div align="center">6</div> | [Maîtrisez l'Orchestre : Tuto Complet Kubernetes, Minikube & Déploiement Local](https://maxwelltbtech.substack.com/p/maitrisez-lorchestre-tuto-complet)| <a href="https://youtu.be/1DG6g-EsyLE"><img src="img/Black and Red Simple Tips How To Be A Programmer Youtube Thumbnail (4).png" alt="Thumbnail 6" width="400"></a> | Deploy the full stack to a **Kubernetes** cluster with deploy, Services, and Pods. |
| <div align="center">7</div> |  [L’API Gateway Kubernetes : la régie scénique de ton orchestre applicatif](https://maxwelltbtech.substack.com/p/lapi-gateway-kubernetes-la-regie)| <a href="https://youtu.be/HA35s3dJSQ4"><img src="img/Youtube-Thumbnail-jour-7.png" alt="Thumbnail 7" width="400"></a> | Guide Pratique : Mettre en Scène une Gateway avec Traefik |
| <div align="center">8</div> | coming soon..| coming soon.. | Automate the pipeline: **Jenkins** for CI (Build/Test) and **Argo CD** for CD (GitOps).  |

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
    <td><img src="https://tse2.mm.bing.net/th/id/OIP.QJnvahq_EBdUGjYQUYrhvAHaDt?pid=Api&P=0&h=180" width="100" alt="MongoDB"/></td>
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
    <td align="center"><img src="https://avatars.githubusercontent.com/u/79485395?v=4" width="100" style="border-radius:50%;"/></td>
    <td>
      <strong>Loic Maxwell Tchiabe | Passionierter Full Stack Developer | Spezialist für Angular, Java & Python </strong><br />
      <i>Cloud & AI Systems Enthusiast.</i><br /><br />
      <a href="https://www.linkedin.com/in/loic-maxwell-tchiabe-softwareentwickler-cloud-ai-java-python-angular/">LinkedIn</a><br />
      <a href="www.youtube.com/@MaxwellTBTech">Youtube</a><br />
      <a href="https://maxwelltbtech.substack.com/">Maxwell TB Tech Newsletter</a><br />
    </td>
  </tr>
</table>

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
