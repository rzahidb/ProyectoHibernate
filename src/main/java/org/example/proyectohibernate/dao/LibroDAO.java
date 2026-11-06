package org.example.proyectohibernate.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.proyectohibernate.database.JPAutil;
import org.example.proyectohibernate.model.Libro;

import java.util.List;

public class LibroDAO {

    public void guardar(Libro libro) {

        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Decirle a Hibernate que prepare la persistencia del objeto
            em.persist(libro);

            // Confirmar los cambios en la base de datos
            tx.commit();

        } catch (Exception ex) {

            if (tx.isActive()) {
                tx.rollback();
            }

            throw new RuntimeException("Error al guardar el libro", ex);

        } finally {
            em.close();
        }
    }

    public List<Libro> listar (){
        EntityManager em = JPAutil.getEntityManager();
        try {
            return em.createQuery("SELECT l FROM Libro l ORDER BY l.id", Libro.class).getResultList();        }
        finally {
            em.close();
        }
    }
    public void actualizar(Libro libro) {
        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Libro existente = em.find(Libro.class, libro.getId());

            if (existente == null) {
                throw new IllegalArgumentException("El libro no existe.");
            }

            existente.setTitulo(libro.getTitulo());
            existente.setAutor(libro.getAutor());
            existente.setAnioPublicacion(libro.getAnioPublicacion());
            existente.setPrecio(libro.getPrecio());

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