# Web Crawler 🕸️
## Team Members
- Philipp Arbeitstein [12205666]
- Philipp Kaiser [12203588]

---

## Overview 🌐
This project implements a **web crawler** in Java that generates a compact overview of a given website and its linked pages. The crawler collects information such as headings and URLs from a website, ensuring that only the specified **depth** and **domains** are crawled.

- Broken links are **highlighted**.
- The results are saved in a **markdown file**.
- **Unit tests** are included to ensure proper functionality. 

---

## How to Build, Run, and Test ⚙️

### Prerequisites 
- **Java 11** or higher installed 
- **Maven** installed (for dependency management and building the project) 
- **JUnit** for unit testing 
- **jsoup** library for parsing HTML (included in `pom.xml`) 
- A modern IDE (Eclipse, IntelliJ IDEA, Visual Studio Code, etc.)


### 1. Clone the Repository 
Clone the project from GitHub (or GitLab/BitBucket):

```bash
git clone https://github.com/yourusername/web-crawler.git
cd web-crawler
```

### 2. Build the Project
You can use Maven to build the project:

```bash
mvn clean install
```

### 3. Running the Application
To run the crawler, navigate to the project directory and use the following command:

```bash
mvn exec:java -Dexec.args="http://example.com 2 example.com,example.org"
```

This will start the crawler with:
- URL: http://example.com
- Depth: 2
- Domains: example.com,example.org

The crawler will crawl up to the specified depth and collect information from the listed domains.


### 4. Running Unit Tests
To run unit tests, use Maven:

```bash
mvn test
```

Unit tests will be automatically executed, and you will receive a report on the test outcomes.


---
## To-Do List Checklist ✅
Ensure the following requirements are met in the code:
- [x] Meaningful Names: All classes, methods, and variables have clear and descriptive names.
- [x] Functions: Code is modularized into reusable functions.
- [x] Unit Tests: Automated tests are written for each key feature using JUnit.
- [x] Comments and Formatting: Code is well-documented with comments, and properly formatted according to Java conventions.
- [~] Objects, Data Structures, and Classes: Appropriate use of objects, classes, and data structures to represent crawled data.
- [~] Error Handling and Boundaries: Proper error handling (e.g., invalid URLs, broken links) is in place.
- [ ] Systems and Emergence: The system behaves as expected in different scenarios, including handling large numbers of URLs.
- [ ] Concurrency: The crawler supports crawling multiple URLs in parallel, improving performance without compromising accuracy.

---
## Libraries and Tools Used 📚
- **jsoup**: An HTML parsing library used to extract headings and links from web pages.
- **JUnit**: A testing framework for writing and running automated tests.
- **Maven**: A build tool for managing dependencies, building, and testing the project.

