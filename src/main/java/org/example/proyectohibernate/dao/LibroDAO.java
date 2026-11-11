
package org.example.proyectohibernate.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import org.example.proyectohibernate.database.JPAutil;
import org.example.proyectohibernate.model.Libro;
import org.example.proyectohibernate.model.Categoria;

import java.util.List;

public class LibroDAO {

    // Guardar libro
    public void guardar(Libro libro) {

        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (libro.getCategoria() == null ||
                    libro.getCategoria().getId() == null) {
                throw new IllegalArgumentException(
                        "Debe seleccionar una categoría."
                );
            }

            Categoria categoria = em.find(
                    Categoria.class,
                    libro.getCategoria().getId()
            );

            if (categoria == null) {
                throw new IllegalArgumentException(
                        "La categoría seleccionada no existe."
                );
            }

            libro.setCategoria(categoria);

            em.persist(libro);
            tx.commit();

        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    // Listar libros
    public List<Libro> listar() {

        EntityManager em = JPAutil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT l FROM Libro l " +
                            "JOIN FETCH l.categoria " +
                            "ORDER BY l.id",
                    Libro.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // Actualizar libro
    public void actualizar(Libro libro) {

        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Libro existente = em.find(
                    Libro.class, libro.getId()
            );

            if (existente == null) {
                throw new IllegalArgumentException(
                        "El libro no existe."
                );
            }

            if (libro.getCategoria() == null ||
                    libro.getCategoria().getId() == null) {
                throw new IllegalArgumentException(
                        "Debe seleccionar una categoría."
                );
            }

            Categoria categoria = em.find(
                    Categoria.class,
                    libro.getCategoria().getId()
            );

            if (categoria == null) {
                throw new IllegalArgumentException(
                        "La categoría seleccionada no existe."
                );
            }

            existente.setTitulo(libro.getTitulo());
            existente.setAutor(libro.getAutor());
            existente.setAnioPublicacion(
                    libro.getAnioPublicacion()
            );
            existente.setPrecio(libro.getPrecio());

            // Actualizar también la relación
            existente.setCategoria(categoria);

            tx.commit();

        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    // Eliminar libro
    public boolean eliminar(Long id) {

        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Libro libro = em.find(Libro.class, id);

            if (libro == null) {
                tx.rollback();
                return false;
            }

            em.remove(libro);
            tx.commit();

            return true;

        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
