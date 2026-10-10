# Online Photo Gallery (pure HTML version)

Web application for managing personal photo albums, built as an individual project for the **Web Information Technologies** course (Tecnologie Informatiche per il Web) at **Politecnico di Milano**.

This is the **server-side rendered** version: every page is generated on the server with Thymeleaf and each user action reloads the page. A second version of the same application, built as a single-page Rich Internet Application in JavaScript, is available in [TIW_RIA](https://github.com/riccardozonnopolimi/TIW_RIA).

## Features

- User registration and login, with server-side input validation and session management
- Image upload with title and description, image download and deletion of your own images
- Album creation and management: add your images to an album and browse it 5 images at a time with previous/next navigation
- Browse the albums created by other users
- Image detail page with comments from other users
- Access control through servlet filters: protected pages are reachable only by logged-in users, and login/registration pages only by guests

## Tech stack

| Layer | Technologies |
|---|---|
| Backend | Java 17, Java Servlets, Apache Tomcat 9 |
| Templating | Thymeleaf 3 |
| Database | MySQL, accessed through JDBC with prepared statements |
| Frontend | HTML, CSS |

## Architecture

The code follows a layered structure that keeps responsibilities separate:

```
src/main/java/it/polimi/tiw/
├── controllers/   Servlets: one per user action (Login, CreateAlbum, UploadPhoto, ...)
├── dao/           Data Access Objects: all SQL queries, one DAO per table
├── beans/         Plain Java objects for users, albums, images and comments
├── filters/       Access control (logged / not logged user)
└── utils/         Database connection and Thymeleaf template setup
src/main/webapp/
├── WEB-INF/       Thymeleaf templates (not directly reachable by the browser)
└── css/           Stylesheets
```

The database contains the tables `user`, `album`, `immagine` (images), `image_album` (which images belong to which album, and in what position) and `commento` (comments).

## Running locally

1. Install **Java 17**, **Apache Tomcat 9** and **MySQL**.
2. Create a MySQL database named `Galleria` with the tables listed above.
3. Set the database URL, user and password in `src/main/webapp/WEB-INF/web.xml`.
4. Import the project into Eclipse (or IntelliJ) as a dynamic web project and run it on Tomcat.
5. Open `http://localhost:8080/<project-name>/` in the browser.

## Possible improvements

- Store passwords as salted hashes (e.g. bcrypt) instead of plain text
- Move database credentials out of `web.xml` into environment variables
- Add a SQL script to create the database schema automatically

## Author

**Riccardo Zonno**, Computer Engineering, Politecnico di Milano
