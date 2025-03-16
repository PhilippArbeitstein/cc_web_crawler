# Web Crawler 🕸️
## Clean Code - Alpen Adria Universität Klagenfurt 

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


### 2. Build the Project
You can use Maven to build the project:

```bash
mvn clean install


### 3. Running the Application
To run the crawler, navigate to the project directory and use the following command:

```bash
mvn exec:java -Dexec.args="http://example.com 2 example.com,example.org"

This will start the crawler with:
- URL: http://example.com
- Depth: 2
- Domains: example.com,example.org


