package org.example.proyectohibernate.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.proyectohibernate.database.JPAutil;
import org.example.proyectohibernate.model.Categoria;

import java.util.List;

public class CategoriaDAO {

    // Crear una nueva categoría
    public void guardar(Categoria categoria) {
        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(categoria);
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

    // Consultar todas las categorías
    public List<Categoria> listar() {
        EntityManager em = JPAutil.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT c FROM Categoria c ORDER BY c.nombre",
                    Categoria.class
            ).getResultList();

        } finally {
            em.close();
        }
    }

    // Buscar una categoría por su ID
    public Categoria buscarPorId(Long id) {
        EntityManager em = JPAutil.getEntityManager();

        try {
            return em.find(Categoria.class, id);
        } finally {
            em.close();
        }
    }

    // Actualizar una categoría existente
    public void actualizar(Categoria categoria) {
        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Categoria existente = em.find(
                    Categoria.class, categoria.getId()
            );

            if (existente == null) {
                throw new IllegalArgumentException(
                        "La categoría no existe."
                );
            }

            existente.setNombre(categoria.getNombre());
            existente.setDescripcion(categoria.getDescripcion());

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

    // Eliminar una categoría por su ID
    public boolean eliminar(Long id) {
        EntityManager em = JPAutil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Categoria categoria = em.find(Categoria.class, id);

            if (categoria == null) {
                tx.rollback();
                return false;
            }

            em.remove(categoria);
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