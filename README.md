# CS122B Project 2
## Instructions
### Deployment
Run `mvn package` in the directory where pom.xml is located.<br>Then run `cp ./target/*.war /var/lib/tomcat/webapps/` to copy the war file into tomcat/webapps.
### Demo
**URL:** `https://youtu.be/tFqOc9hKA5c`
### AWS
**URL:** `http://54.215.234.136:8080/cs122b-project2/`
### TomCat
**Username:** `admin`<br>**Password:** `mypassword`
### MySQL
**Username:** `mytestuser`<br>**Password:** `My6$Password`<br>**Create Database File:** `create_table.sql`
## Additional Notes
### Substring Matching Design
  - %AB%: For a query 'AB', it will return all strings the contain the pattern 'AB' in the results
  - LIKE '%AB%'
## Contributions
### Vanessa
  - Login Page (JS)
  - Login Filter/Login Servlet
  - Main Page (JS)
  - Main Page Servlet
  - Confirmation Page (HTML/JS)
  - Confirmation Page Servlet
  - Payment Page (HTML/CSS/JS)
  - Payment Page Servlet
  - Cart Page (HTML/JS)
  - Cart Page Servlet
  - Pagenation/Sorting
  - Modifying SQL Queries
  - Making Revisions/Style Changes to Other Pages
  - Debugging
### Haver
  - Login Page (HTML/CSS)
  - Main Page (HTML/CSS)
  - Main Page Servlet
  - Confirmation Page Servlet
  - Payment Page Servlet
  - Cart Page Servlet
  - Genre Results Servlet
  - Start Title Servlet
  - Search Result Servlet
  - Modifying SQL Queries
  - Making Revisions/Style Changes to Other Pages
  - Debugging
