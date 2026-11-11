
package org.example.proyectohibernate.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JPAutil {

    private static EntityManagerFactory entityManagerFactory;

    public static synchronized void iniciar() {

        if (entityManagerFactory == null) {

            // Obtener contraseña desde las variables de entorno
            String password = System.getenv("DB_PASSWORD");

            if (password == null || password.isBlank()) {
                throw new RuntimeException(
                        "DB_PASSWORD environment variable is empty"
                );
            }

            // PRUEBA DE CONEXIÓN DIRECTA CON POSTGRESQL
            System.out.println("Verificando conexión con PostgreSQL...");

            try (Connection conexion = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/bdbiblioteca",
                    "postgres",
                    password
            )) {

                if (conexion.isValid(5)) {
                    System.out.println("CONEXIÓN JDBC EXITOSA");
                    System.out.println(
                            "Base de datos: " + conexion.getCatalog()
                    );
                } else {
                    throw new SQLException(
                            "La conexión no superó la validación."
                    );
                }

            } catch (SQLException ex) {

                System.err.println("ERROR DE CONEXIÓN JDBC");
                System.err.println("Mensaje: " + ex.getMessage());
                System.err.println("SQLState: " + ex.getSQLState());
                System.err.println("Código: " + ex.getErrorCode());

                throw new RuntimeException(
                        "No se pudo conectar a PostgreSQL", ex
                );
            }

            // Configurar propiedades de Hibernate
            Map<String, Object> propiedades = new HashMap<>();

            propiedades.put(
                    "jakarta.persistence.jdbc.password",
                    password
            );

            // Crear EntityManagerFactory
            try {

                entityManagerFactory =
                        Persistence.createEntityManagerFactory(
                                "bibliotecaPU",
                                propiedades
                        );

                System.out.println(
                        "HIBERNATE INICIALIZADO CORRECTAMENTE"
                );

            } catch (RuntimeException ex) {

                System.err.println(
                        "ERROR AL INICIALIZAR HIBERNATE"
                );

                ex.printStackTrace();

                throw ex;
            }
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        iniciar();
        return entityManagerFactory;
    }

    public static EntityManager getEntityManager() {
        iniciar();
        return entityManagerFactory.createEntityManager();
    }

    public static synchronized void cerrar() {

        if (entityManagerFactory != null) {

            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }

            entityManagerFactory = null;
        }
    }
}
