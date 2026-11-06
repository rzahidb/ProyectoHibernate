
module org.example.proyectohibernate {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    exports org.example.proyectohibernate;

    opens org.example.proyectohibernate to javafx.fxml;

    opens org.example.proyectohibernate.controller to javafx.fxml;

    opens org.example.proyectohibernate.model
            to org.hibernate.orm.core, javafx.base;
}