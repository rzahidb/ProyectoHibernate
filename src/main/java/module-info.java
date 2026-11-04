module org.example.proyectohibernate {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Base de datos
    requires java.sql;
    requires org.postgresql.jdbc;

    // Jakarta Persistence
    requires jakarta.persistence;

    // Hibernate
    requires org.hibernate.orm.core;

    // Exportar paquete principal
    exports org.example.proyectohibernate;

    // Permitir acceso a los controladores JavaFX
    opens org.example.proyectohibernate to javafx.fxml;

    // Permitir que Hibernate acceda a las entidades
    opens org.example.proyectohibernate.model
            to org.hibernate.orm.core, javafx.base;